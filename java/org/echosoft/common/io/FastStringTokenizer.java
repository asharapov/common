package org.echosoft.common.io;

import java.util.Iterator;


/**
 * Осуществляет разбор строки на токены. Поддерживаются два алгоритма токенизации:
 * <li> простой способ, аналогичный способу, используемому в {@link java.util.StringTokenizer}.
 *
 * <strong>Внимание!</strong> Данный класс считается устаревшим и сохраняется только ради обратной совместимости со старым кодом.
 * Во всех случаях кроме самых простых настоятельно рекомендуется использовать класс {@link CSVParser}.
 *
 * @see CSVParser
 * @author Anton Sharapov
 */
public final class FastStringTokenizer implements Iterator<String> {

    private final char delimiter;
    private String text;
    private int length;
    private int pos;
    private int tokenNumber;
    private int lineNumber;
    private String nextToken;

    public FastStringTokenizer(final char delimiter) {
        this.delimiter = delimiter;
        init("", 0);
    }

    public FastStringTokenizer(final String text, final char delimiter) {
        this.delimiter = delimiter;
        init(text, 0);
    }

    public void init(final String text, final int lineNumber) {
        this.text = text;
        this.length = text.length();
        this.pos = 0;
        this.tokenNumber = 0;
        this.lineNumber = lineNumber;
        this.nextToken = scanToken();
    }

    @Override
    public boolean hasNext() {
        return hasMoreTokens();
    }

    @Override
    public String next() {
        return nextToken();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Method remove() was not supported in the given class");
    }


    public boolean hasMoreTokens() {
        return nextToken != null;
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

    public int getLineNumber() {
        return lineNumber;
    }

    private String scanToken() {
        if (pos >= length)
            return null;

        for (int i = pos; i < length; i++) {
            final char c = text.charAt(i);
            if (c == delimiter) {
                final String result = text.substring(pos, i);
                pos = ++i;
                return result;
            } else
            if (i + 1 == length) {
                final String result = text.substring(pos);
                pos = ++i;
                return result;
            }
        }
        return null;
    }
}
