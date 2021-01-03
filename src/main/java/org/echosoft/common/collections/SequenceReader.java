package org.echosoft.common.collections;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Anton Sharapov
 */
public class SequenceReader extends Reader {

    private final Reader[] readers;
    private int pos;
    private Reader in;

    public SequenceReader(final Reader... readers) {
        this.readers = readers != null ? readers : new Reader[0];
        this.pos = 0;
        this.in = this.readers.length > 0 ? this.readers[0] : null;
    }


    protected void nextReader() throws IOException {
        if (in != null) {
            in.close();
            readers[pos] = null;
        }
        pos++;
        in = readers.length > pos ? readers[pos] : null;
    }

    @Override
    public int read() throws IOException {
        if (in == null) {
            return -1;
        }
        int c = in.read();
        if (c == -1) {
            nextReader();
            return read();
        }
        return c;
    }

    @Override
    public int read(final char buf[], final int off, final int len) throws IOException {
        if (in == null) {
            return -1;
        } else
        if (len == 0) {
            return 0;
        }

        int n = in.read(buf, off, len);
        if (n <= 0) {
            nextReader();
            return read(buf, off, len);
        }
        return n;
    }

    @Override
    public boolean ready() throws IOException {
        return in != null && in.ready();
    }

    @Override
    public long skip(long n) throws IOException {
        throw new IOException("not supported");
    }

    @Override
    public void close() throws IOException {
        in = null;
        while (readers.length > pos) {
            try {
                readers[pos].close();
                readers[pos] = null;
            } catch (Exception e) { /* do nothing */ }
            pos++;
        }
    }

}
