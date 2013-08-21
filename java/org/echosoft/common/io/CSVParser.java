package org.echosoft.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;

/**
 * Парсер данных в формате CSV (в соответствии с RFC 4180)
 *
 * @author Anton Sharapov
 */
public class CSVParser implements Closeable, AutoCloseable {

    public static final char TOKENS_DELIMITER = ',';
    public static final char TOKENS_ESCAPE_CHAR = '"';
    public static final boolean TOKENS_ESCAPE = true;

    private static enum State {
        BOF, UNKNOWN, NEXT_TOKEN, LAST_TOKEN_AT_LINE, LAST_TOKEN_AT_FILE, EOL, EOF
    }

    private final Reader reader;            // входной поток который требуется разобрать на записи и их атрибуты.
    private final char delimiter;           // символ-разделитель между токенами
    private final boolean wrapped;          // определяет следует ли по умолчанию ожидать что все разбираемые токены должны быть обрамлены в кавычки.
    private int lineNum;                    // порядковый номер (начиная с 1) текущей обрабатываемой строки (для которой был возвращен последний токен)
    private int lastTokenNum;               // порядковый номер (начиная с 1) последнего возвращенного токена в строке .
    private State state;                    // определяет состояние в котором находится парсер.
    private String nextToken;               // значение очередного токена который будет возвращен следующим вызовом метода {@link #nextToken()}. Актуален когда state = SCANNED.
    private StringBuilder buf;              // буфер для накопления содержимого текущего обрабатываемого токена.
    private int unreadChar;

    public CSVParser(final Reader reader) {
        this(reader, TOKENS_DELIMITER, TOKENS_ESCAPE);
    }

    public CSVParser(final Reader reader, final char delimiter, final boolean wrapped) {
        this.reader = reader;
        this.delimiter = delimiter;
        this.wrapped = wrapped;
        this.lineNum = 0;
        this.lastTokenNum = 0;
        this.state = State.BOF;
        this.buf = new StringBuilder();
        this.unreadChar = -1;
    }

    public int getLineNum() {
        return lineNum;
    }

    public int getLastTokenNum() {
        return lastTokenNum;
    }

    public boolean hasNextToken() throws IOException {
        ensureCurrentStateResolved();
        switch (state) {
            case BOF:
            case UNKNOWN:
                throw new IllegalStateException();
            case NEXT_TOKEN:
            case LAST_TOKEN_AT_LINE:
            case LAST_TOKEN_AT_FILE:
                return true;
            default:
                return false;
        }
    }

    public String nextToken() throws IOException {
        ensureCurrentStateResolved();
        switch (state) {
            case BOF:
            case UNKNOWN:
                throw new IllegalStateException();
            case NEXT_TOKEN:
                state = State.UNKNOWN;
                break;
            case LAST_TOKEN_AT_LINE:
                state = State.EOL;
                break;
            case LAST_TOKEN_AT_FILE:
                state = State.EOF;
                break;
            default:
                throw new NoSuchElementException();
        }
        final String result = nextToken;
        nextToken = null;
        lastTokenNum++;
        return result;
    }

    public boolean nextLine() throws IOException {
        if (state == State.UNKNOWN || state == State.NEXT_TOKEN) {
            while (hasNextToken())
                nextToken();
        }
        int b;
        switch (state) {
            case BOF:
            case LAST_TOKEN_AT_LINE:
            case EOL: {
                lastTokenNum = 0;
                lineNum++;
                while ((b = readChar()) >= 0) {
                    final char c = (char) b;
                    switch (c) {
                        case '\n':
                            lineNum++;
                        case '\r':
                            break;
                        default:
                            unreadChar(c);
                            state = State.UNKNOWN;
                            return true;
                    }
                }
                state = State.EOF;
                return false;
            }
            case LAST_TOKEN_AT_FILE:
                state = State.EOF;
            case EOF:
                lastTokenNum = 0;
                return false;
            default:
                throw new IllegalStateException();
        }
    }

    protected void ensureCurrentStateResolved() throws IOException {
        if (state == State.UNKNOWN)
            state = scanToken();
    }


    /**
     * Если предполагается что отдельные токены в файле должны быть обрамлены в кавычки и другие нет
     * то этот метод должен быть перегружен.
     *
     * @param tokenNum порядковый номер токена (начиная с 1) в строке.
     * @return <code>true</code> если указанный в аргументе токен должен быть обрамлен кавычками.
     */
    protected boolean isTokenShouldBeWrapped(final int tokenNum) {
        return wrapped;
    }

    protected State scanToken() throws IOException {
        int state = isTokenShouldBeWrapped(lastTokenNum + 1) ? 0 : 3;
        int b;
        boolean cw = false;
        while ( (b = readChar()) >= 0 ) {
            final char c = (char)b;
            switch (state) {
                case 0: {
                    // В ожидании начала экранированного токена ...
                    if (c == delimiter) {
                        nextToken = null;
                        return State.NEXT_TOKEN;
                    } else
                    if (c == '\n') {
                        nextToken = null;
                        return State.LAST_TOKEN_AT_LINE;
                    } else
                    if (c == TOKENS_ESCAPE_CHAR) {
                        state = 1;
                    } else
                    if (c > ' ')
                        throw new IllegalStateException("Illegal character '" + c + "'");
                    break;
                }
                case 1: {
                    // обработка полезного содержимого экранированного токена ...
                    if (cw) {
                        if (c == TOKENS_ESCAPE_CHAR) {
                            cw = false;
                            buf.append(c);
                            break;
                        } else {
                            state = 2;
                        }
                    } else {
                        if (c == TOKENS_ESCAPE_CHAR) {
                            cw = true;
                        } else {
                            buf.append(c);
                        }
                        break;
                    }
                }
                case 2: {
                    // в ожидании завершения экранированного токена ...
                    if (c == delimiter) {
                        nextToken = buf.toString();
                        buf.setLength(0);
                        return State.NEXT_TOKEN;
                    } else
                    if (c == '\n') {
                        nextToken = buf.toString();
                        buf.setLength(0);
                        return State.LAST_TOKEN_AT_LINE;
                    } else
                    if (c > ' ')
                        throw new IllegalStateException("Illegal character '" + c + "'");
                    break;
                }
                case 3: {
                    // обработка неэкранированного токена ...
                    if (c == delimiter) {
                        nextToken = buf.toString();
                        buf.setLength(0);
                        return State.NEXT_TOKEN;
                    } else
                    if (c == '\r') {
                        break;
                    }
                    if (c == '\n') {
                        nextToken = buf.toString();
                        buf.setLength(0);
                        return State.LAST_TOKEN_AT_LINE;
                    } else {
                        buf.append(c);
                    }
                }
            }
        }

        switch (state) {
            case 0:
                nextToken = null;
                break;
            case 2:
            case 3:
                nextToken = buf.toString();
                break;
            default:
                throw new IllegalStateException();
        }
        return State.LAST_TOKEN_AT_FILE;
    }

    protected int readChar() throws IOException {
        if (unreadChar >= 0) {
            final int result = unreadChar;
            unreadChar = -1;
            return result;
        } else
            return reader.read();
    }
    protected void unreadChar(final char c) {
        if (unreadChar >= 0)
            throw new IllegalStateException();
        unreadChar = c;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
