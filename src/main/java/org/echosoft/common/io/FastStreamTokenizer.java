package org.echosoft.common.io;

import org.echosoft.common.collections.ReadAheadIterator;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Осуществляет разбор содержимого символьного потока на токены.
 * В качестве разделителя токенов используется некоторый фиксированный символ, задаваемый в конструкторе данного парсера.<br/>
 *
 * @author Anton Sharapov
 */
public final class FastStreamTokenizer implements ReadAheadIterator<String> {

    public static final int DEFAULT_BUF_SIZE = 4096;

    private final Reader reader;        // Поток символов который требуется разобрать на токены.
    private final char delimiter;       // Символ, отделяющий один токен от другого.
    private final boolean closeOnEOF;   // Следует ли автоматически закрывать входной поток при достижении его окончания.
    private final char[] charbuf;       // Буфер в который подчитываются данные из потока.
    private int length;                 // Количество символов реально загруженных в буфер. Всегда варьируется от 0 (буфер пуст) до емкости буфера. Имеет значение -1 после полного исчерпания входного потока.
    private int pos;                    // Текущая позиция сканера в буфере. Всегда варьируется от 0 до кол-ва символов загруженных в буфер.
    private int tokenNumber;            // Порядковый номер последнего разобранного токена.
    private String nextToken;           // Предварительно отсканированный токен, который будет возвращен при последующем вызове метода {@link #nextToken()}.
    private StringBuilder buf;          // Временный буффер, где аккумулируются символы сканируемого в настоящий момент токена.

    public FastStreamTokenizer(final Reader reader, final char delimiter) throws IOException {
        this.delimiter = delimiter;
        this.closeOnEOF = false;
        this.charbuf = new char[DEFAULT_BUF_SIZE];
        this.reader = reader;
        this.length = 0;
        this.pos = 0;
        this.tokenNumber = 0;
        this.buf = new StringBuilder();
        this.nextToken = scanToken();
    }

    @Deprecated
    public FastStreamTokenizer(final Reader reader, final char delimiter, final boolean closeOnEOF) throws IOException {
        this.delimiter = delimiter;
        this.closeOnEOF = closeOnEOF;
        this.charbuf = new char[DEFAULT_BUF_SIZE];
        this.reader = reader;
        this.length = 0;
        this.pos = 0;
        this.tokenNumber = 0;
        this.buf = new StringBuilder();
        this.nextToken = scanToken();
    }

    @Override
    public boolean hasNext() {
        return nextToken != null;
    }

    @Override
    public String next() {
        return nextToken();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Method remove() was not supported in the given class");
    }

    @Override
    public String readAhead() {
        if (nextToken == null)
            throw new NoSuchElementException();
        return nextToken;
    }


    public String nextToken() {
        final String result = nextToken;
        if (nextToken != null) {
            nextToken = scanToken();
            tokenNumber++;
        }
        return result;
    }

    public int getTokenNumber() {
        return tokenNumber;
    }

    private String scanToken() {
        try {
            buf.setLength(0);
            for (char c = readChar(); c != 0; c = readChar()) {
                if (c == delimiter) {
                    return buf.toString();
                }
                buf.append(c);
            }
            return buf.length() > 0 ? buf.toString() : null;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
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
                if (closeOnEOF) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        /* do nothing */
                    }
                }
                return (char) 0;
            }
            pos = 0;
        }
        return charbuf[pos++];
    }

}
