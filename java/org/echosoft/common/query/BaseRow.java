package org.echosoft.common.query;

import java.util.Map;

/**
 * Base implementation of the {@link Row} interface.
 * @author Anton Sharapov
 */
public final class BaseRow implements Row {

    private final Map<String,Object> data;
    private BeanMetaData metadata;

    /**
     * Creates new row and initializes it values from specified map.
     * In this constructor rows metadata was not specified, so metadata recalculates based on data values.
     * @param data  contains fields
     * @throws NullPointerException if no data specified
     */
    public BaseRow(Map<String,Object> data) {
        this.data = data;
    }

    /**
     * Creates new row and initializes it metadata and values from specified map.
     * @param metadata predefined row's metadata
     * @param data  contains fields
     * @throws NullPointerException if no data specified
     */
    public BaseRow(BeanMetaData metadata, Map<String,Object> data) {
        if (data==null)
            throw new NullPointerException("No data specified");
        this.metadata = metadata;
        this.data = data;
    }


    /**
     * Returns fields description, which available in this row.
     * @return fields metadata.
     */
    public BeanMetaData getMetaData() {
        if (metadata==null) {
            final FieldMetaData fields[] = new FieldMetaData[data.size()];
            int cursor = 0;
            for (Map.Entry<String,Object> entry : data.entrySet()) {
                final Object value = entry.getValue();
                fields[cursor++] = new FieldMetaData(entry.getKey(), value != null ? value.getClass() : null);
            }
            metadata = new BeanMetaData(fields);
        }
        return metadata;
    }

    /**
     * Returns a value of the specified field.
     * If specified field was not found returns <code>null</code>.
     * @param name  field's name
     * @return specified field's value.
     */
    public Object getField(final String name) {
        return data.get(name);
    }

    /**
     * Set a value for specified field name.
     * @param name  field's name
     * @param value  new value of this field
     */
    public void setField(final String name, final Object value) {
        data.put(name, value);
    }


    /**
     * @return Map representation of this data.
     */
    public Map<String,Object> toMap() {
        return data;
    }

    public int hashCode() {
        return data.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BaseRow))
            return false;
        final BaseRow other = (BaseRow)obj;
        return data.equals(other.data);
    }

    public String toString() {
        return "[Row{"+data+"}]";
    }
}
