package org.echosoft.common.dbf;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.echosoft.common.utils.StringUtil;

/**
 * @author Anton Sharapov
 */
class DateField extends Field {

    private final SimpleDateFormat formatter;

    public DateField(final FieldDescriptor descriptor) {
        super(descriptor);
        this.formatter = new SimpleDateFormat("yyyyMMdd");
    }

    @Override
    protected Object getAsObject(final byte[] recordBuf) throws DBFException {
        final String str = read(recordBuf);
        try {
            return str.isEmpty() ? null : formatter.parse(str);
        } catch (ParseException e) {
            throw new DBFException("Illegal field format: " + str);
        }
    }

    @Override
    protected String getAsString(final byte[] recordBuf) throws DBFException {
        final Date value = (Date) getAsObject(recordBuf);
        return value != null ? StringUtil.formatISODate(value) : null;
    }

    @Override
    protected Boolean getAsBoolean(final byte[] recordBuf) throws DBFException {
        throw new DBFException("Can't convert type to boolean");
    }

    @Override
    protected Integer getAsInteger(final byte[] recordBuf) throws DBFException {
        throw new DBFException("Can't convert type to integer");
    }

    @Override
    protected Long getAsLong(final byte[] recordBuf) throws DBFException {
        final Date value = (Date) getAsObject(recordBuf);
        return value!=null ? value.getTime() : null;
    }

    @Override
    protected BigDecimal getAsBigDecimal(final byte[] recordBuf) throws DBFException {
        throw new DBFException("Can't convert type to big decimal");
    }

    @Override
    protected Date getAsDate(final byte[] recordBuf) throws DBFException {
        return (Date)getAsObject(recordBuf);
    }
}
