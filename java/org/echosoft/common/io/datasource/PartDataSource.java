package org.echosoft.common.io.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.servlet.http.Part;

import org.echosoft.common.utils.StringUtil;

/**
 * @author Anton Sharapov
 */
public class PartDataSource implements DataSource {

    private final String fileName;
    private final Part part;

    public PartDataSource(final Part part) {
        this.part = part;

        String file = null;
        final String hcd = part.getHeader("content-disposition");
        if (hcd != null) {
            final String[] attrs = StringUtil.splitIgnoringEmpty(hcd, ';');
            for (String attr : attrs) {
                final int delim = attr.indexOf('=');
                if (delim < 0)
                    continue;
                final String name = attr.substring(0, delim).trim();
                if (!"filename".equals(name))
                    continue;
                file = attr.substring(delim + 1).trim();
                if (file.length() > 0 && file.charAt(0) == '\"')
                    file = file.substring(1);
                final int length = file.length();
                if (length > 0 && file.charAt(length - 1) == '\"')
                    file = file.substring(0, length - 1);
            }
        }
        this.fileName = file;
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return part.getContentType();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    public long getSize() {
        return part.getSize();
    }
}
