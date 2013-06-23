package org.echosoft.common.io;

import java.util.Iterator;


/**
 * Осуществляет разбор строки на токены. Поддерживаются два алгоритма токенизации:
 * <li> простой способ, аналогичный способу, используемому в {@link java.util.StringTokenizer}.
 * <li> алгоритм, способный обрабатывать токены, обрамленные некоторым определенным символом.
 *
 * @author Anton Sharapov
 */
public final class FastStringTokenizer implements Iterator<String> {

    private final char delimiter;
    private final char valueWrapper;
    private String text;
    private int length;
    private int pos;
    private int tokenNumber;
    private int lineNumber;
    private String nextToken;

    public FastStringTokenizer(final char delimiter) {
        this("", delimiter, (char) 0);
    }

    public FastStringTokenizer(final char delimiter, final char valueWrapper) {
        this("", delimiter, valueWrapper);
    }

    public FastStringTokenizer(final String text, final char delimiter, final char valueWrapper) {
        this.delimiter = delimiter;
        this.valueWrapper = valueWrapper;
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
        return valueWrapper > 0
                ? scanWrappedToken()
                : scanSimpleToken();
    }

    private String scanWrappedToken() {
        while (pos < length) {
            if (text.charAt(pos++) == valueWrapper)
                break;
        }

        if (pos + 1 >= length)
            return null;

        final StringBuilder buf = new StringBuilder();

        for (; pos < length; pos++) {
            final char c = text.charAt(pos);

            if (c == valueWrapper) {
                final int cc = pos + 1 < length ? text.charAt(pos + 1) : -1;
                if (cc != valueWrapper) {

                    for (; pos < length; pos++) {
                        if (text.charAt(pos) == delimiter)
                            break;
                    }
                    return buf.toString();
                } else
                    pos++;
            }
            buf.append(c);
        }
        return null;
    }

    private String scanSimpleToken() {
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
