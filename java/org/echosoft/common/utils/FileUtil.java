package org.echosoft.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Набор утилитарных методов для работы с файловой подсистемой.
 * @author Anton Sharapov
 */
public class FileUtil {

    private static final int BUF_SIZE = 16 * 1024 * 1024;

    /**
     * Копирует файл в указанное место файловой системы.
     * @param srcFile файл который должен быть скопирован.
     * @param dst определяет куда будет скопирован файл. Задается либо полный путь к файлу либо просто каталог куда должен быть скопирован файл.
     * @throws IOException в случае каких-либо проблем при копировании.
     */
    public static void copy(final File srcFile, final File dst) throws IOException {
        final File destFile = dst.isDirectory() ? new File(dst, srcFile.getName()) : dst;

        final FileChannel in = new FileInputStream(srcFile).getChannel();
        try {
            final FileChannel out = new FileOutputStream(destFile).getChannel();
            try {
                final long size = in.size();
                for (long pos = 0; pos < size; ) {
                    final long rest = size - pos;
                    final long count = rest > BUF_SIZE ? BUF_SIZE : rest;
                    pos += out.transferFrom(in, pos, count);
                }
                if (destFile.length() != size) {
                    throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'.");
                }
                if (!destFile.setLastModified(srcFile.lastModified())) {
                    throw new IOException("Can't set last modification time for target path");
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

}
