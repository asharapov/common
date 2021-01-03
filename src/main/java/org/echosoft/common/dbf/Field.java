package org.echosoft.common.dbf;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anton Sharapov
 */
public abstract class Field implements Serializable {

    protected final FieldDescriptor descriptor;

    public Field(final FieldDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public String getName() {
        return descriptor.getName();
    }

    public FieldType getType() {
        return descriptor.getType();
    }

    public int getSize() {
        return descriptor.getSize();
    }

    public int getPrecision() {
        return descriptor.getPrecision();
    }

    protected String read(final byte[] recordBuf) throws DBFException {
        return new String(recordBuf, descriptor.getFieldOffset(), descriptor.getSize(), descriptor.getCharset()).trim();
    }

    protected abstract Object getAsObject(final byte[] recordBuf) throws DBFException;

    protected abstract String getAsString(final byte[] recordBuf) throws DBFException;

    protected abstract Boolean getAsBoolean(final byte[] recordBuf) throws DBFException;

    protected abstract Integer getAsInteger(final byte[] recordBuf) throws DBFException;

    protected abstract Long getAsLong(final byte[] recordBuf) throws DBFException;

    protected abstract BigDecimal getAsBigDecimal(final byte[] recordBuf) throws DBFException;

    protected abstract Date getAsDate(final byte[] recordBuf) throws DBFException;

    public String toString() {
        return "[DBF.Field{name:" + descriptor.getName() + ", type:" + descriptor.getType() + ", size:" + descriptor.getSize() + ", precision:" + descriptor.getPrecision() + ", offset:" + descriptor.getFieldOffset()+"}]";
    }
}
