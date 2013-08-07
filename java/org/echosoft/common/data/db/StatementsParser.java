package org.echosoft.common.data.db;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Отвечает за разбор SQL скрипта на составляющие его выражения, разделенные символом '/'.
 *
 * @author Anton Sharapov
 */
public class StatementsParser implements Iterator<String> {

    private final Reader reader;        // Поток символов который требуется разобрать на токены.
    private boolean nextStmtReaded;     // Указывает был ли уже выполнен разбор очередного SQL выражения (которое должно будет отдано при последующем вызове next()) из входного потока или нет.
    private StringBuilder stmt;         // Прочитанное из потока SQL выражение ожидающее отдачи при последующем вызове метода next().
    private int stmtNum;                // Порядковый номер последней записи из потока которая была возвращена методов {@link #nextRecord()}.
    private int row;                    // Текущая обрабатываемая строка в скрипте.
    private int column;                 // Текущая обрабатываемая колонка в скрипте.
    private final char[] charbuf;       // Буфер в который подчитываются данные из потока.
    private int length;                 // Количество символов реально загруженных в буфер. Всегда варьируется от 0 (буфер пуст) до емкости буфера. Имеет значение -1 после полного исчерпания входного потока.
    private int pos;                    // Текущая позиция сканера в буфере. Всегда варьируется от 0 до кол-ва символов загруженных в буфер.

    public StatementsParser(final Reader reader) {
        this.reader = reader;
        this.nextStmtReaded = false;
        this.stmt = new StringBuilder(512);
        this.charbuf = new char[4096];
        this.pos = 0;
        this.length = 0;
        this.stmtNum = 0;
        this.row = 0;
        this.column = 0;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNext() {
        ensureNextStatementScanned();
        return stmt != null;
    }

    /**
     * Очередное SQL выражение полученное из входного потока. Все SQL выражения разделяются между собой символом '/'.
     *
     * @return Очередное SQL варажение.
     * @throws NoSuchElementException при исчерпании элементов по которым возможно дальнейшее итерирование.
     */
    @Override
    public String next() {
        ensureNextStatementScanned();
        if (stmt == null)
            throw new NoSuchElementException();
        stmtNum++;
        nextStmtReaded = false;
        return stmt.toString();
    }

    /**
     * Возвращает порядковый номер последнеего возвращенного методом {@link #next()} SQL выражения.
     *
     * @return порядковый номер (начиная с 1) последнего возвращенного данным методом SQL выражения.
     */
    public int getStatementNumber() {
        return stmtNum;
    }


    /**
     * Осуществляет чтение очередного SQL выражения из потока если оно еще не было прочитано.
     *
     * @throws IllegalStateException в случае нарушения структуры потока.
     */
    private void ensureNextStatementScanned() {
        try {
            if (!nextStmtReaded) {
                do {
                    scanStatement();
                } while (stmt != null && stmt.toString().trim().isEmpty());
                nextStmtReaded = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void scanStatement() throws IOException {
        final StringBuilder token = new StringBuilder();
        int depth = 0;
        boolean lastTokenIsEnd = false;
        stmt.setLength(0);
        int state = 0;
        for (char c = readChar(); c != 0; c = readChar()) {
            stmt.append(c);
            if (c == '\n') {
                row++;
                column = 0;
            } else {
                column++;
            }
            switch (state) {
                case 0: {   // находимся в процессе чтения SQL выражения (основной режим) ...

                    // читаем и анализируем обрабатываемый идентификатор или ключевое слово ...
                    final int tl = token.length();
                    if (tl == 0) {
                        if (Character.isJavaIdentifierStart(c)) {
                            token.append(c);
                            break;
                        } else
                        if (!Character.isWhitespace(c)) {
                            lastTokenIsEnd = false;
                        }
                    } else {
                        if (Character.isJavaIdentifierPart(c)) {
                            token.append(c);
                            break;
                        } else {
                            final String tuc = token.toString().toUpperCase();
                            boolean currentTokenIsEnd = false;
                            switch (tl) {
                                case 2:
                                    if ("IF".equals(tuc) && !lastTokenIsEnd)
                                        depth++;
                                    break;
                                case 3:
                                    if ("END".equals(tuc)) {
                                        depth--;
                                        currentTokenIsEnd = true;
                                    }
                                    break;
                                case 4:
                                    if (("CASE".equals(tuc) || "LOOP".equals(tuc)) && !lastTokenIsEnd)
                                        depth++;
                                    break;
                                case 5:
                                    if ("BEGIN".equals(tuc))
                                        depth++;
                                    break;
                            }
                            token.setLength(0);
                            lastTokenIsEnd = currentTokenIsEnd;
                        }
                    }

                    switch (c) {
                        case '\'':
                            state = 1;
                            break;
                        case '\"':
                            state = 2;
                            break;
                        case '-': {
                            final char cc = readAheadChar();
                            if (cc == '-') {
                                stmt.append(cc);
                                pos++;
                                column++;
                                state = 3;
                            }
                            break;
                        }
                        case '/': {
                            final char cc = readAheadChar();
                            if (cc == '*') {
                                stmt.append(cc);
                                pos++;
                                column++;
                                state = 4;
                            } else
                            if (column == 1) {
                                stmt.setLength(stmt.length() - 1);
                                return;
                            }
                            break;
                        }
                        case ';': {
                            if (depth == 0) {
                                stmt.setLength(stmt.length() - 1);
                                return;
                            }
                            break;
                        }
                    }
                    break;
                }
                case 1: {   // находимся в состоянии чтения строки символов ограниченных одинарной кавычкой (ожидаем символ '\'') ...
                    if (c == '\'')
                        state = 0;
                    break;
                }
                case 2: {   // находимся в состоянии чтения строки символов ограниченных двойной кавычкой (ожидаем символ '\"') ...
                    if (c == '\"')
                        state = 0;
                    break;
                }
                case 3: {   // находимся в состоянии чтения однострочного комментария (ожидаем символ '\n') ...
                    if (c == '\n')
                        state = 0;
                    break;
                }
                case 4: {   // находимся в состоянии чтения многострочного комментария (ожидаем подстроку "*/") ...
                    if (c == '*') {
                        final char cc = readAheadChar();
                        if (cc == '/') {
                            stmt.append(cc);
                            pos++;
                            column++;
                            state = 0;
                        }
                    }
                }
            }
        }
        if (depth != 0 || (state != 0 && state != 3)) {
            throw new IllegalStateException("Неожиданное завершение потока");
        }
        if (stmt.toString().trim().isEmpty())
            stmt = null;
    }

    /**
     * Осуществляем буферизированное чтение символов из потока.
     *
     * @return Очередной прочитанный символ или <code>(char)0</code> при достижении конца входного потока.
     * @throws IOException в случае каких-либо ошибок при чтении данных из потока.
     */
    private char readChar() throws IOException {
        if (pos >= length) {
            if (length < 0)
                return (char) 0;
            length = reader.read(charbuf);
            if (length < 0) {
                return (char) 0;
            }
            pos = 0;
        }
        return charbuf[pos++];
    }

    /**
     * Осуществляем буферизированное чтение символов из потока.
     *
     * @return Очередной прочитанный символ или <code>(char)0</code> при достижении конца входного потока.
     * @throws java.io.IOException в случае каких-либо ошибок при чтении данных из потока.
     */
    private char readAheadChar() throws IOException {
        if (pos >= length) {
            if (length < 0)
                return (char) 0;
            length = reader.read(charbuf);
            if (length < 0) {
                return (char) 0;
            }
            pos = 0;
        }
        return charbuf[pos];
    }
}
