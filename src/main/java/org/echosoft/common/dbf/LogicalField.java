package org.echosoft.common.dbf;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anton Sharapov
 */
class LogicalField extends Field {

    public LogicalField(final FieldDescriptor fd) {
        super(fd);
    }

    @Override
    public Object getAsObject(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        if (value.isEmpty())
            return null;
        final char c = value.charAt(0);
        return 'T' == c || 't' == c || 'Y' == c || 'y' == c;
    }

    @Override
    protected String getAsString(final byte[] recordBuf) throws DBFException {
        final Boolean value = (Boolean) getAsObject(recordBuf);
        return value != null ? value.toString() : null;
    }

    @Override
    protected Boolean getAsBoolean(final byte[] recordBuf) throws DBFException {
        return (Boolean) getAsObject(recordBuf);
    }

    @Override
    protected Integer getAsInteger(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        if (value.isEmpty())
            return null;
        final char c = value.charAt(0);
        return 'T' == c || 't' == c || 'Y' == c || 'y' == c ? 1 : 0;
    }

    @Override
    protected Long getAsLong(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        if (value.isEmpty())
            return null;
        final char c = value.charAt(0);
        return 'T' == c || 't' == c || 'Y' == c || 'y' == c ? 1L : 0;
    }

    @Override
    protected BigDecimal getAsBigDecimal(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        if (value.isEmpty())
            return null;
        final char c = value.charAt(0);
        return 'T' == c || 't' == c || 'Y' == c || 'y' == c ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    @Override
    protected Date getAsDate(final byte[] recordBuf) throws DBFException {
        throw new DBFException("Can't convert boolean type to date");
    }
}
