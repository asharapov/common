package org.echosoft.common.dbf;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import org.echosoft.common.utils.StringUtil;

/**
 * @author Anton Sharapov
 */
class TextField extends Field {

    public TextField(final FieldDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public Object getAsObject(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        return value.isEmpty() ? null : value;
    }

    @Override
    protected String getAsString(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        return value.isEmpty() ? null : value;
    }

    @Override
    protected Boolean getAsBoolean(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        if (value.isEmpty())
            return null;
        if (value.length() == 1) {
            final char c = value.charAt(0);
            return 'T' == c || 't' == c || 'Y' == c || 'y' == c || '1' == c;
        } else {
            return Boolean.valueOf(value);
        }
    }

    @Override
    protected Integer getAsInteger(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        return value.isEmpty() ? null : new Integer(value);
    }

    @Override
    protected Long getAsLong(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        return value.isEmpty() ? null : new Long(value);
    }

    @Override
    protected BigDecimal getAsBigDecimal(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        return value.isEmpty() ? null : new BigDecimal(value);
    }

    @Override
    protected Date getAsDate(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        if (value.isEmpty())
            return null;
        try {
            return StringUtil.parseDate(value);
        } catch (ParseException e) {
            throw new DBFException("Can't convert string '" + value + "' to date", e);
        }
    }
}
