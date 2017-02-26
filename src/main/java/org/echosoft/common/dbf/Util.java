package org.echosoft.common.dbf;

import java.nio.charset.Charset;

/**
 * @author Anton Sharapov
 */
class Util {

    public static String readZeroBasedString(final byte[] buf, final int offset, final int maxSize, final Charset charset) {
        int size = 0;
        for (int i = offset; size <= maxSize && buf[i] != 0x0; i++) size++;
        return new String(buf, offset, size, charset);
    }

    public static int readInt(final byte[] buf, final int offset) {
        return ((buf[offset + 3] & 0xFF) << 24) |
                ((buf[offset + 2] & 0xFF) << 16) |
                ((buf[offset + 1] & 0xFF) << 8) |
                ((buf[offset] & 0xFF));
    }

    public static int readUnsignedShort(final byte[] buf, final int offset) {
        return ((buf[offset + 1] & 0xFF) << 8) | (buf[offset] & 0xFF);
    }
}
