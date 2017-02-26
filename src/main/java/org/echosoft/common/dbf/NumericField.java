package org.echosoft.common.dbf;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anton Sharapov
 */
class NumericField extends Field {

    public NumericField(final FieldDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    protected Object getAsObject(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        if (value.isEmpty())
            return null;
        if (getPrecision() == 0) {
            return descriptor.getSize() > 8
                    ? new Long(value)
                    : new Integer(value);
        } else {
            return new BigDecimal(value);
        }
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
        try {
            final int i = Integer.parseInt(value, 10);
            return i == 1;
        } catch (NumberFormatException e) {
            throw new DBFException("Can't convert type to boolean", e);
        }
    }

    @Override
    protected Integer getAsInteger(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        if (value.isEmpty())
            return null;
        try {
            return new Integer(value);
        } catch (NumberFormatException e) {
            throw new DBFException("Can't convert type to integer", e);
        }
    }

    @Override
    protected Long getAsLong(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        if (value.isEmpty())
            return null;
        try {
            return new Long(value);
        } catch (NumberFormatException e) {
            throw new DBFException("Can't convert type to long", e);
        }
    }

    @Override
    protected BigDecimal getAsBigDecimal(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        if (value.isEmpty())
            return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new DBFException("Can't convert type to bigdecimal", e);
        }
    }

    @Override
    protected Date getAsDate(final byte[] recordBuf) throws DBFException {
        final String value = read(recordBuf);
        if (value.isEmpty())
            return null;
        try {
            final long timestamp = Long.parseLong(value, 10);
            return new Date(timestamp);
        } catch (NumberFormatException e) {
            throw new DBFException("Can't convert type to date", e);
        }
    }
}
