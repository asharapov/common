package org.echosoft.common.dbf;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anton Sharapov
 */
public class DBFReader {

    private final InputStream stream;
    private final TableDescriptor descriptor;
    private final Field[] fields;
    private final Map<String, Field> fieldsMap;
    private byte[] recordBuf;
    private int currentRecord;
    private boolean positioned;
    private Charset effectiveCharset;

    public DBFReader(final InputStream stream) throws IOException, DBFException {
        this(stream, Charset.defaultCharset());
    }

    public DBFReader(final InputStream stream, final Charset defaultCharset) throws IOException, DBFException {
        this.stream = stream;
        final byte[] data1 = new byte[32];
        if (stream.read(data1) < data1.length)
            throw new DBFException("Premature end of stream: can't read DBF header.");
        this.descriptor = new TableDescriptor(data1);

        if (descriptor.getHeaderSize() - 33 <= 0 || ((descriptor.getHeaderSize() - 1) % 32) != 0)
            throw new DBFException("Inconsistent dbf file format: incorrect header size.", 8);
        this.effectiveCharset = descriptor.getLanguageDriver() != LanguageDriver.UNKNOWN ? descriptor.getLanguageDriver().getCharset() : defaultCharset;

        this.fields = new Field[(descriptor.getHeaderSize() - 33) / 32];
        final byte[] data2 = new byte[descriptor.getHeaderSize() - 32];
        if (stream.read(data2) < data2.length)
            throw new DBFException("Premature end of stream: can't read DBF fields headers.");
        int offset = 0, fieldOffset = 1;
        for (int i = 0, cnt = fields.length; i < cnt; i++) {
            final FieldDescriptor fd = new FieldDescriptor(data2, offset, effectiveCharset, fieldOffset);
            fieldOffset += fd.getSize();
            switch (fd.getType()) {
                case CHAR: {
                    fields[i] = new TextField(fd);
                    break;
                }
                case DATE: {
                    fields[i] = new DateField(fd);
                    break;
                }
                case LOGICAL: {
                    fields[i] = new LogicalField(fd);
                    break;
                }
                case FLOAT:
                case NUMERIC: {
                    fields[i] = new NumericField(fd);
                    break;
                }
                default: {
                    throw new DBFException("Unsupported field type: ' " + fd.getType().getCode() + "'.");
                }
            }
            offset += 32;
        }
        if (data2[offset] != 0xD)
            throw new DBFException("Inconsistent DBF file format", 32 + offset);

        this.fieldsMap = new HashMap<String, Field>();
        for (Field field : fields) {
            fieldsMap.put(field.getName(), field);
        }
        recordBuf = new byte[descriptor.getRecordSize()];
        currentRecord = 0;
        positioned = false;
    }

    public Field[] getFields() {
        return fields;
    }

    public Charset getCharset() {
        return effectiveCharset;
    }

    public int getRecordsCount() {
        return descriptor.getRecordsCount();
    }

    public int getCurrentRecord() {
        return currentRecord;
    }


    public boolean next() throws IOException, DBFException {
        if (currentRecord >= descriptor.getRecordsCount())
            return false;
        currentRecord++;
        positioned = false;
        final int readed = stream.read(recordBuf);
        if (readed == 1 && recordBuf[0] == 0x1A) {
            throw new DBFException("Premature end of stream: wrong information about total records count");
//            return false;
        }
        if (readed != recordBuf.length)
            throw new DBFException("Premature end of stream: can't read next record.");
        positioned = true;
        return true;
    }

    public boolean isRecordDeleted() {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        return recordBuf[0] == 0x2A;
    }

    public Object getObject(final String fieldName) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsObject(recordBuf);
    }

    public Object getObject(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsObject(recordBuf);
    }

    public String getString(final String fieldName) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsString(recordBuf);
    }

    public String getString(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsString(recordBuf);
    }

    public Boolean getBoolean(final String fieldName) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsBoolean(recordBuf);
    }

    public Boolean getBoolean(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsBoolean(recordBuf);
    }

    public Integer getInteger(final String fieldName) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsInteger(recordBuf);
    }

    public Integer getInteger(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsInteger(recordBuf);
    }

    public Long getLong(final String fieldName) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsLong(recordBuf);
    }

    public Long getLong(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsLong(recordBuf);
    }

    public BigDecimal getBigDecimal(final String fieldName) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsBigDecimal(recordBuf);
    }

    public BigDecimal getBigDecimal(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsBigDecimal(recordBuf);
    }

    public Date getDate(final String fieldName) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsDate(recordBuf);
    }

    public Date getDate(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new IllegalStateException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsDate(recordBuf);
    }
}
