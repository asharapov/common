package org.echosoft.common.io.datasource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;

import org.echosoft.common.utils.StreamUtil;
import org.echosoft.common.utils.StringUtil;

/**
 * Реализация интерфейса {@link DataSource} которая до достижения определенного размера хранит
 * обрабатываемые данные в памяти, а при превышении заданного предела сбрасывает их во временный файл.
 *
 * @author Anton Sharapov
 */
public class BufferedDataSource implements DataSource {

    private final int initBufferSize;
    private final int limitBufferSize;
    private final File tmpDir;
    private String name;
    private String contentType;
    private byte[] buf;
    private File file;
    private int count;
    private int modCount;
    private int openedInputStreams;
    private int openOutputStreams;

    public BufferedDataSource(final File file) {
        this.initBufferSize = 0;
        this.limitBufferSize = 0;
        this.tmpDir = null;
        this.name = file.getName();
        this.buf = null;
        this.file = file;
        this.count = 0;
        this.modCount = 0;
    }

    public BufferedDataSource(final int initBufferSize, final int limitBufferSize, final File tmpDir) {
        if (initBufferSize < 0 || limitBufferSize < 0 || initBufferSize > limitBufferSize)
            throw new IllegalArgumentException("Init buffer size is more than buffer's limit");
        this.initBufferSize = initBufferSize;
        this.limitBufferSize = limitBufferSize;
        this.tmpDir = tmpDir;
        this.buf = null;
        this.file = null;
        this.count = 0;
        this.modCount = 0;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getContentType() {
        if (contentType != null)
            return contentType;
        if (name != null)
            return FileTypeMap.getDefaultFileTypeMap().getContentType(name);
        return null;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * Возвращает общий размер сохраненных в буфере данных.
     *
     * @return общий размер сохраненных данных.
     */
    public long getSize() {
        if (buf != null)
            return count;
        if (file != null)
            return file.length();
        return 0;
    }

    public void readFrom(final InputStream in) throws IOException {
        final OutputStream out = getOutputStream();
        try {
            StreamUtil.pipeData(in, out);
        } finally {
            out.close();
        }
    }

    public void writeOut(final OutputStream out) throws IOException {
        if (buf != null) {
            out.write(buf, 0, count);
        } else
        if (file != null) {
            final InputStream in = new FileBufferInputStream();
            try {
                StreamUtil.pipeData(in, out);
            } finally {
                in.close();
            }
        }
    }

    /**
     * Сбрасывает всю информацию, записанную в данный буфер.
     *
     * @return true в случае успешной очистки всех занятых ресурсов.
     */
    public boolean close() throws IOException {
        count = 0;
        buf = null;
        boolean result = true;
        if (file != null) {
            result = file.delete();
            file = null;
        }
        return result && openedInputStreams == 0 && openOutputStreams == 0;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (buf != null)
            return new MemoryBufferInputStream();

        if (file != null)
            return new FileBufferInputStream();

        return StreamUtil.EMPTY_INPUT_STREAM;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new BufferedOutputStream();
    }


    private class MemoryBufferInputStream extends InputStream {
        private final int expectedModCount;
        private int pos;
        private int mark;
        private boolean closed;

        private MemoryBufferInputStream() {
            expectedModCount = modCount;
            pos = 0;
            closed = false;
            openedInputStreams++;
        }

        @Override
        public int read() {
            checkModification();
            return (pos < count) ? (buf[pos++] & 0xff) : -1;
        }

        @Override
        public int read(final byte b[], final int off, int len) {
            checkModification();
            if (b == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            }
            if (pos >= count) {
                return -1;
            }
            if (pos + len > count) {
                len = count - pos;
            }
            if (len <= 0) {
                return 0;
            }
            System.arraycopy(buf, pos, b, off, len);
            pos += len;
            return len;
        }

        @Override
        public long skip(long n) {
            checkModification();
            if (pos + n > count) {
                n = count - pos;
            }
            if (n < 0) {
                return 0;
            }
            pos += n;
            return n;
        }

        @Override
        public int available() {
            checkModification();
            return count - pos;
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void mark(final int readAheadLimit) {
            mark = pos;
        }

        @Override
        public void reset() {
            pos = mark;
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                openedInputStreams--;
            }
        }

        private void checkModification() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
        }
    }


    private class FileBufferInputStream extends FileInputStream {
        private final int expectedModCount;
        private boolean closed;

        private FileBufferInputStream() throws FileNotFoundException {
            super(file);
            expectedModCount = modCount;
            closed = false;
            openedInputStreams++;
        }

        @Override
        public int read() throws IOException {
            checkModification();
            return super.read();
        }

        @Override
        public int read(final byte[] b) throws IOException {
            checkModification();
            return super.read(b);
        }

        @Override
        public int read(final byte b[], final int off, final int len) throws IOException {
            checkModification();
            return super.read(b, off, len);
        }

        @Override
        public long skip(final long n) throws IOException {
            checkModification();
            return super.skip(n);
        }

        @Override
        public int available() throws IOException {
            checkModification();
            return super.available();
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                super.close();
                closed = true;
                openedInputStreams--;
            }
        }

        private void checkModification() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
        }
    }


    private class BufferedOutputStream extends OutputStream {

        private OutputStream fileStream;
        private boolean closed;

        private BufferedOutputStream() throws IOException {
            closed = false;
            openOutputStreams++;
        }

        @Override
        public void write(final int b) throws IOException {
            checkBuffer(1);
            if (buf != null) {
                buf[count++] = (byte) b;
            } else {
                fileStream.write(b);
            }
            modCount++;
        }

        @Override
        public void write(final byte b[], final int off, final int len) throws IOException {
            checkBuffer(len);
            if (buf != null) {
                if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
                    throw new IndexOutOfBoundsException();
                }
                System.arraycopy(b, off, buf, count, len);
                count += len;
            } else {
                fileStream.write(b, off, len);
            }
            modCount++;
        }

        @Override
        public void flush() throws IOException {
            if (fileStream != null) {
                fileStream.flush();
                modCount++;
            }
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                if (fileStream != null) {
                    fileStream.close();
                    modCount++;
                }
                closed = true;
                openOutputStreams--;
            }
        }

        private void checkBuffer(final int delta) throws IOException {
            if (fileStream != null) {
                // Если уже инициирован поток записи в файл то можем ничего не делать ...
                return;
            }
            final int desiredSize = count + delta;
            if (buf != null) {
                // Если ранее мы уже начали писать в буфер в памяти ...
                if (desiredSize > buf.length) {
                    if (desiredSize > limitBufferSize) {
                        final String tmpfilename = StringUtil.getNonEmpty(StringUtil.getFileName(name), "tmp");
                        file = File.createTempFile(tmpfilename, null, tmpDir);
                        fileStream = new FileOutputStream(file);
                        fileStream.write(buf, 0, count);
                        buf = null;
                        count = 0;
                    } else {
                        final int newLength = Math.min(buf.length << 1, limitBufferSize);
                        buf = Arrays.copyOf(buf, newLength);
                    }
                }
                return;
            }
            if (file != null) {
                // Когда-то (вероятно, в другом экземпляре BufferOutputStream) мы уже начинали писать данные. Сейчас мы просто продолжаем запись  ...
                fileStream = new FileOutputStream(file, true);
                return;
            }
            if (desiredSize > limitBufferSize) {
                // Если мы еще ничего не делали но сразу хотим записать порцию данных большую чем разрещено писать в буфер памяти ...
                file = File.createTempFile(name != null ? name : "tmp", null, tmpDir);
                fileStream = new FileOutputStream(file, true);
            } else {
                // Если мы еще ничего не делали и пытаемся записать небольшую порцию данных ...
                buf = new byte[Math.max(initBufferSize, desiredSize)];
                count = 0;
            }
        }
    }
}

