package org.echosoft.common.io.datasource;

import javax.activation.DataSource;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Anton Sharapov
 */
public class PartDataSource implements DataSource {

    private final Part part;

    public PartDataSource(final Part part) {
        this.part = part;
    }

    @Override
    public String getName() {
        return part.getName();
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

}
