package org.echosoft.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Используется для определения типа содержимого файлов по расширению его имени.
 *
 * @author Anton Sharapov
 */
public class MimeUtil {

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private static final Properties MIME_TYPES = new Properties();
    static {
        final URL url = MimeUtil.class.getResource("mime.types");
        if (url != null) {
            try (InputStream in = url.openStream()) {
                MIME_TYPES.load(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Возвращает тип содержимого для заданного расширения имен файлов.
     *
     * @param ext             расширение имен файлов, например: "docx"
     * @param defaultMimeType тип содержимого используемый если указанное расширение имен нам не известно.
     * @return тип MIME (Multipurpose Internet Mail Extensions) соответствующий данному расширению имени файла или указанное значение по умолчанию.
     */
    public static String getMimeTypeForExtension(final String ext, final String defaultMimeType) {
        if (ext == null)
            return defaultMimeType;
        return MIME_TYPES.getProperty(ext.trim().toLowerCase(), defaultMimeType);
    }

    /**
     * По расширению имени файла вычисляет тип его предполагаемого содержимого.
     *
     * @param fileName какое-то имя файла. может включать в себя и путь.
     * @return тип MIME (Multipurpose Internet Mail Extensions) соответствующий данному расширению имени файла
     */
    public static String getMimeTypeForFile(final String fileName) {
        return getMimeTypeForFile(fileName, DEFAULT_MIME_TYPE);
    }

    /**
     * По расширению имени файла вычисляет тип его предполагаемого содержимого.
     *
     * @param fileName        какое-то имя файла. может включать в себя и путь.
     * @param defaultMimeType тип MIME  используемый по умолчанию если в базе известных расширений мы не нашли требуемого.
     * @return тип MIME (Multipurpose Internet Mail Extensions) соответствующий данному расширению имени файла или указанное значение по умолчанию.
     */
    public static String getMimeTypeForFile(final String fileName, final String defaultMimeType) {
        final int d = fileName.lastIndexOf('.');
        final String ext = d > 0 ? fileName.substring(d + 1).toLowerCase() : "";
        return MIME_TYPES.getProperty(ext, defaultMimeType);
    }

    /**
     * Возвращает одно из расширений имен файлов которые соответствуют данному типу MIME.
     *
     * @param mimeType тип MIME
     * @return расширений имени файлов соответствующее данному типу MIME.
     *  Метод возвращает <code>null</code> если для искомого типа MIME не было найдено ни одного известного расширения имени файлов.
     */
    public static String getExtensionForMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty())
            return null;
        mimeType = mimeType.toLowerCase();
        for (Map.Entry<Object, Object> en : MIME_TYPES.entrySet()) {
            if (mimeType.equalsIgnoreCase(en.getValue().toString())) {
                return en.getKey().toString();
            }
        }
        return null;
    }

    /**
     * Возвращает коллекцию типовых расширений имен файлов которые соответствуют данному типу MIME.
     *
     * @param mimeType тип MIME
     * @return коллекция расширений имен файлов соответствующая данному типу MIME.
     * Метод возвращает пустую коллекцию если для искомого типа MIME не было найдено ни одного известного расширения имен файлов.
     */
    public static Collection<String> getExtensionsForMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty())
            return Collections.emptyList();
        final ArrayList<String> result = new ArrayList<>();
        mimeType = mimeType.toLowerCase();
        for (Map.Entry<Object, Object> en : MIME_TYPES.entrySet()) {
            if (mimeType.equalsIgnoreCase(en.getValue().toString())) {
                result.add(en.getKey().toString());
            }
        }
        return result;
    }
}
