package org.echosoft.common.db;

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
                    scanRecord0();
                } while (stmt != null && stmt.toString().trim().isEmpty());
                nextStmtReaded = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void scanRecord0() throws IOException {
        stmt.setLength(0);
        int state = 0, column = 0;
        for (char c = readChar(); c != 0; c = readChar()) {
            stmt.append(c);
            column = c == '\n' ? 0 : column + 1;
            switch (state) {
                case 0: {   // находимся в процессе чтения SQL выражения (ожидаем одинарный символ '/') ...
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
                                ++this.pos;
                                state = 3;
                            }
                            break;
                        }
                        case '/': {
                            final char cc = readAheadChar();
                            if (cc == '*') {
                                stmt.append(cc);
                                ++this.pos;
                                state = 4;
                            } else
                            if (column == 1) {
                                stmt.setLength(stmt.length() - 1);
                                return;
                            }
                            break;
                        }
                        case ';': {
                            stmt.setLength(stmt.length() - 1);
                            return;
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
                            ++this.pos;
                            state = 0;
                        }
                    }
                }
            }
        }
        if (state != 0 && state != 3) {
            throw new IllegalStateException("Неожиданное завершение потока");
        }
        stmt = null;
    }

    /**
     * Осуществляем буферизированное чтение символов из потока.
     *
     * @return Очередной прочитанный символ или <code>(char)0</code> при достижении конца входного потока.
     * @throws java.io.IOException в случае каких-либо ошибок при чтении данных из потока.
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
