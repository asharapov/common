package org.echosoft.common.dbf;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.echosoft.common.utils.StreamUtil;

/**
 * Выполняет чтение содержимого .DBF файла из потока.
 *
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

    /**
     * Инициирует чтение содержимого заданного .DBF файла. Немедленно будет прочитан заголовок таблицы и получена вся информация о ее структуре.
     *
     * @param stream         поток на чтение данных из DBF файла.
     * @param defaultCharset кодировка в которой будет выполняться попытка чтения содержимого .DBF файла, в случае если программа не сможет определить кодировку файла самостоятельно.
     * @throws IOException  в случае каких-либо проблем при чтении данных.
     * @throws DBFException в случае каких-либо проблем при анализе прочтенных данных.
     */
    public DBFReader(final InputStream stream, final Charset defaultCharset) throws IOException, DBFException {
        this.stream = stream;
        final byte[] data1 = new byte[32];
        if (StreamUtil.readFromStream(stream, data1) < data1.length)
            throw new DBFException("Premature end of stream: can't read DBF header.");
        this.descriptor = new TableDescriptor(data1);

        if (descriptor.getHeaderSize() - 33 <= 0 || ((descriptor.getHeaderSize() - 1) % 32) != 0)
            throw new DBFException("Inconsistent dbf file format: incorrect header size.", 8);
        this.effectiveCharset = descriptor.getLanguageDriver() != LanguageDriver.UNKNOWN ? descriptor.getLanguageDriver().getCharset() : defaultCharset;

        this.fields = new Field[(descriptor.getHeaderSize() - 33) / 32];
        final byte[] data2 = new byte[descriptor.getHeaderSize() - 32];
        if (StreamUtil.readFromStream(stream, data2) < data2.length)
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

    /**
     * Возввращает список всех полей в том порядке в котором они определены в таблице..
     *
     * @return список полей таблицы.
     */
    public Field[] getFields() {
        return fields;
    }

    /**
     * Возвращает кодировку используемую при чтении данных из таблцы.
     *
     * @return применяемая кодировка.
     */
    public Charset getCharset() {
        return effectiveCharset;
    }

    /**
     * Возвращает общее количество строк в таблице (инфорамция получается из заголовка таблицы).
     *
     * @return общее количество строк в таблице.
     */
    public int getRecordsCount() {
        return descriptor.getRecordsCount();
    }

    /**
     * Порядковый номер текущей обрабатываемой строки. Первая строка идет с номером <code>1</code>.
     *
     * @return порядковый номер текущей обрабатываемой строки.
     */
    public int getCurrentRecord() {
        return currentRecord;
    }


    /**
     * Читает из потока содержимое следующей строки.
     *
     * @return <code>true</code> в случае когда была получена очередная строка данных,
     *         <code>false</code> после того как была прочитана последняя строка данных.
     * @throws IOException  в случае каких-либо пробелм при чтении данных из потока.
     * @throws DBFException в случае окончания данных до расчетного момента.
     */
    public boolean next() throws IOException, DBFException {
        if (currentRecord >= descriptor.getRecordsCount())
            return false;
        currentRecord++;
        positioned = false;
        final int readed = StreamUtil.readFromStream(stream, recordBuf);
        if (readed == 1 && recordBuf[0] == 0x1A) {
            throw new DBFException("Premature end of stream: wrong information about total records count");
//            return false;
        }
        if (readed != recordBuf.length)
            throw new DBFException("Premature end of stream: can't read next record.");
        positioned = true;
        return true;
    }

    /**
     * Возвращает <code>true</code> если текущая строка была помечена как удаленная.
     *
     * @return <code>true</code>  если текущая строка помечена как удаленная.
     * @throws DBFException в случае если либо не была прочитана еще ни одна строка таблицы либо уже все строки таблицы были прочитаны ранее.
     */
    public boolean isRecordDeleted() throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        return recordBuf[0] == 0x2A;
    }

    public Object getObject(final String fieldName) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsObject(recordBuf);
    }

    public Object getObject(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsObject(recordBuf);
    }

    public String getString(final String fieldName) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsString(recordBuf);
    }

    public String getString(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsString(recordBuf);
    }

    public Boolean getBoolean(final String fieldName) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsBoolean(recordBuf);
    }

    public Boolean getBoolean(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsBoolean(recordBuf);
    }

    public Integer getInteger(final String fieldName) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsInteger(recordBuf);
    }

    public Integer getInteger(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsInteger(recordBuf);
    }

    public Long getLong(final String fieldName) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsLong(recordBuf);
    }

    public Long getLong(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsLong(recordBuf);
    }

    public BigDecimal getBigDecimal(final String fieldName) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsBigDecimal(recordBuf);
    }

    public BigDecimal getBigDecimal(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsBigDecimal(recordBuf);
    }

    public Date getDate(final String fieldName) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        final Field field = fieldsMap.get(fieldName);
        if (field == null)
            throw new DBFException("Field '" + fieldName + "' not found");
        return field.getAsDate(recordBuf);
    }

    public Date getDate(final int fieldNum) throws DBFException {
        if (!positioned)
            throw new DBFException("Illegal state");
        if (fieldNum >= fields.length)
            throw new DBFException("Field with index '" + fieldNum + "' not exists");
        final Field field = fields[fieldNum];
        return field.getAsDate(recordBuf);
    }
}
