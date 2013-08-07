package org.echosoft.common.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Упрощенная реализация {@link java.io.SequenceInputStream}, чуть лучше подходящая когда требуется "сцепить" несколько уже открытых потоков.<br/>
 *
 * @author Anton Sharapov
 */
public class SequenceInputStream extends InputStream {

    private final InputStream[] streams;
    private int pos;
    private InputStream in;

    public SequenceInputStream(final InputStream... streams) {
        this.streams = streams != null ? streams : new InputStream[0];
        this.pos = 0;
        this.in = this.streams.length > 0 ? this.streams[0] : null;
    }


    protected void nextStream() throws IOException {
        if (in != null) {
            in.close();
            streams[pos] = null;
        }
        pos++;
        in = streams.length > pos ? streams[pos] : null;
    }


    @Override
    public int read() throws IOException {
        if (in == null) {
            return -1;
        }
        int c = in.read();
        if (c == -1) {
            nextStream();
            return read();
        }
        return c;
    }

    @Override
    public int read(final byte buf[], final int off, final int len) throws IOException {
        if (in == null) {
            return -1;
        } else
        if (len == 0) {
            return 0;
        }

        int n = in.read(buf, off, len);
        if (n <= 0) {
            nextStream();
            return read(buf, off, len);
        }
        return n;
    }

    @Override
    public int available() throws IOException {
        return in != null ? in.available() : 0;
    }

    @Override
    public long skip(long n) throws IOException {
        throw new IOException("not supported");
    }

    @Override
    public void close() throws IOException {
        in = null;
        while (streams.length > pos) {
            try {
                streams[pos].close();
                streams[pos] = null;
            } catch (Exception e) { /* do nothing */ }
            pos++;
        }
    }
}
