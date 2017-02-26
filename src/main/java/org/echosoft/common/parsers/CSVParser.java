package org.echosoft.common.parsers;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;

/**
 * Парсер данных в формате CSV (в соответствии с <a href="https://tools.ietf.org/html/rfc4180">RFC 4180</a>).<br/>
 * Пример использования:<br/>
 * <pre>
 *  Reader stream = ...;
 *  CSVReader parser = new CSVParser(stream, ',');
 *  parser.skipLines(1);    // если требуется просто пропустить заданное количество строк.
 *  while (parser.nextLine()) {
 *      while (parser.hasNextToken()) {
 *          String token = parser.nextToken();
 *      }
 *  }
 *  parser.close();
 * </pre>
 *
 * @author Anton Sharapov
 */
public class CSVParser implements Closeable, AutoCloseable {

    public static final char TOKENS_DELIMITER = ',';
    public static final char TOKENS_PAYLOAD_WRAPPER = '"';

    private static enum State {
        BOF, UNKNOWN, NEXT_TOKEN, LAST_TOKEN_AT_LINE, LAST_TOKEN_AT_FILE, EOL, EOF
    }

    private final Reader reader;            // входной поток который требуется разобрать на записи и их атрибуты.
    private final char delimiter;           // символ-разделитель между токенами
    private final char wrapper;             // символ в который может быть "завернуто" значение токена (используется если в значениях могут быть пробелы, переводы строк и прочие подобные символы).
    private int lineNum;                    // порядковый номер (начиная с 1) текущей обрабатываемой строки (для которой был возвращен последний токен)
    private int lastTokenNum;               // порядковый номер (начиная с 1) последнего возвращенного токена в строке .
    private State state;                    // определяет состояние в котором находится парсер.
    private String nextToken;               // значение очередного токена который будет возвращен следующим вызовом метода {@link #nextToken()}. Актуален когда state in (NEXT_TOKEN, LAST_TOKEN_AT_LINE, LAST_TOKEN_AT_FILE)
    private StringBuilder buf;              // буфер для накопления содержимого текущего обрабатываемого токена.
    private int unreadChar;

    public CSVParser(final Reader reader) {
        this(reader, TOKENS_DELIMITER, TOKENS_PAYLOAD_WRAPPER);
    }

    public CSVParser(final Reader reader, final char delimiter) {
        this(reader, delimiter, TOKENS_PAYLOAD_WRAPPER);
    }

    public CSVParser(final Reader reader, final char delimiter, final char wrapper) {
        this.reader = reader;
        this.delimiter = delimiter;
        this.wrapper = wrapper;
        this.lineNum = 0;
        this.lastTokenNum = 0;
        this.state = State.BOF;
        this.buf = new StringBuilder();
        this.unreadChar = -1;
    }

    /**
     * Возвращает порядковый номер текущей обрабатываемой строки (начиная с 1).
     *
     * @return номер текущей обрабатываемой строки.
     */
    public int getLineNum() {
        return lineNum;
    }

    /**
     * Возвращает номер последнего прочитанного методом {@link #nextToken()} токена в текущей строке (начиная с 1).
     *
     * @return номер последнего прочитанного токена в строке.
     */
    public int getLastTokenNum() {
        return lastTokenNum;
    }

    /**
     * Возвращает <code>true</code> если текущая строка содержит еще токены для чтения. Для перехода на следующую строку следует использовать вызов {@link #nextLine()}.
     *
     * @return <code>true</code> если текущая строка содержит еще токены для чтения.
     * @throws IOException в случае проблем с чтением содержимого исходного потока.
     * @throws IllegalStateException в случае если в текущей строке больше нет токенов.
     */
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

    /**
     * Возвращает следующий токен в текущей строке.
     * Строка в которой выбираются токены должна быть предварительно выбрана методом {@link #nextLine}.<br/>
     * Для проверки если ли в текущей строке еще токены для чтения следует использовать метод {@link #hasNextToken()}.<br/>
     *
     * @return Строка со значением следующего токена.
     * @throws IOException в случае проблем с чтением содержимого исходного потока.
     * @throws IllegalStateException в случае если в текущей строке больше нет токенов.
     */
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

    /**
     * Пропускает указанное количество строк.<br/>
     *
     * @param linesCnt количество строк которые должны быть пропущены.
     * @return <code>true</code> если после перевода строки курсор указывает на существующую строку (то есть конец файла еще не достигнут).
     */
    public boolean nextLine(final int linesCnt) throws IOException {
        for (int i = 0; state != State.EOF && i < linesCnt; i++) {
            nextLine();
        }
        return state != State.EOF;
    }

    /**
     * Переводит курсор на следующую строку.
     * Если в текущей строке были непрочитанные токены то они игнорируются.<br/>
     * Все пустые строки (не содержащие ни одного токена) игнорируются и пролистываются автоматически с соответствующим автоувеличением счетчика прочитанных строк.
     *
     * @return <code>true</code> если после перевода строки курсор указывает на существующую строку (то есть конец файла еще не достигнут).
     */
    public boolean nextLine() throws IOException {
        if (state == State.UNKNOWN || state == State.NEXT_TOKEN) {
            // С учетом возможности наличия токенов содержащих в себе символ перевода строки (токены обрамленные кавычками)
            // нам все равно придется делать синтаксический разбор до конца текущей записи
            skipLine();
        }
        switch (state) {
            case BOF:
            case LAST_TOKEN_AT_LINE:
            case EOL: {
                final int b = readChar();
                if (b >= 0) {
                    unreadChar((char)b);
                    lastTokenNum = 0;
                    lineNum++;
                    state = State.UNKNOWN;
                    return true;
                } else {
                    state = State.EOF;
                    return false;
                }
            }
            case LAST_TOKEN_AT_FILE:
                state = State.EOF;
            case EOF:
                return false;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Пропускает текущую обрабатываемую строку и переводит курсор на следующую строку потока.
     *
     * @return <code>true</code> если был достигнут символ конца строки, <code>false</code> - если был достигнут конец файла.
     */
    protected boolean skipLine() throws IOException {
        int b, state = 0;
        while ( (b=readChar()) >= 0 ) {
            final char c = (char)b;
            switch (state) {
                case 0: {
                    // обрабатываем содержимое потока не заключенное в кавычки ...
                    if (c == '\n') {
                        if (this.state == State.UNKNOWN || this.state == State.NEXT_TOKEN)
                        this.state = State.EOL;
                        return true;
                    }
                    if (c == wrapper)
                        state = 1;
                    break;
                }
                case 1: {
                    // обрабатываем содержимое потока которое было заключено в кавычки в ожидании закрывающей кавычки ...
                    if (c == wrapper)
                        state = 2;
                    break;
                }
                case 2: {
                    // обрабатываем символ следующий за возможно "закрывающей" кавычкой токена.
                    // если этот символ - тоже кавычка значит токен продолжается.
                    if (c == '\n') {
                        this.state = State.EOL;
                        return true;
                    }
                    state = (c == wrapper) ? 1 : 0;
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
        this.state = State.EOF;
        return false;
    }

    protected void ensureCurrentStateResolved() throws IOException {
        if (state == State.UNKNOWN)
            state = scanToken();
    }


    private static final int WAIT_TOKEN_START = 0;
    private static final int PROCESS_TOKEN_PAYLOAD_MASKED = 1;
    private static final int PROCESS_TOKEN_PAYLOAD_UNMASKED = 2;
    private static final int WAIT_TOKEN_END = 3;

    protected State scanToken() throws IOException {
        int state = WAIT_TOKEN_START;
        int b;
        boolean hasEmptyChars = false;
        boolean cw = false;
        while ( (b = readChar()) >= 0 ) {
            final char c = (char)b;
            switch (state) {
                case WAIT_TOKEN_START: {
                    // В ожидании начала произвольного токена ...
                    if (c == delimiter) {
                        nextToken = "";
                        return State.NEXT_TOKEN;
                    } else
                    if (c == '\n') {
                        if (hasEmptyChars) {
                            nextToken = "";
                            return State.LAST_TOKEN_AT_LINE;
                        } else {
                            nextToken = null;
                            return State.EOL;
                        }
                    } else
                    if (c == wrapper) {
                        state = PROCESS_TOKEN_PAYLOAD_MASKED;
                    } else
                    if (c > ' ') {
                        state = PROCESS_TOKEN_PAYLOAD_UNMASKED;
                        buf.append(c);
                    }
                    if (c != '\r')
                        hasEmptyChars = true;
                    break;
                }
                case PROCESS_TOKEN_PAYLOAD_MASKED: {
                    // обработка полезного содержимого обрамленного кавычками токена ...
                    if (cw) {
                        if (c == wrapper) {
                            cw = false;
                            buf.append(c);
                        } else {
                            state = WAIT_TOKEN_END;
                            unreadChar(c);
                        }
                    } else {
                        if (c == wrapper) {
                            cw = true;
                        } else {
                            buf.append(c);
                        }
                    }
                    break;
                }
                case PROCESS_TOKEN_PAYLOAD_UNMASKED: {
                    // обработка неэкранированного токена ...
                    if (c == delimiter) {
                        nextToken = buf.toString();
                        buf.setLength(0);
                        return State.NEXT_TOKEN;
                    } else
                    if (c > ' ') {
                        buf.append(c);
                        break;
                    } else
                    if (c == '\n') {
                        nextToken = buf.toString();
                        buf.setLength(0);
                        return State.LAST_TOKEN_AT_LINE;
                    } else {
                        state = WAIT_TOKEN_END;
                    }
                }
                case WAIT_TOKEN_END: {
                    // в ожидании завершения токена (ждем разделитель токенов или символ конца строки) ...
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
            }
        }

        switch (state) {
            case WAIT_TOKEN_START:
                nextToken = "";
                break;
            case WAIT_TOKEN_END:
            case PROCESS_TOKEN_PAYLOAD_UNMASKED:
                nextToken = buf.toString();
                buf.setLength(0);
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
