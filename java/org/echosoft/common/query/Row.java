package org.echosoft.common.query;

import java.io.Serializable;
import java.util.Map;

/**
 * Describes one record (row) in the table model.
 * @author Anton Sharapov
 */
public interface Row extends Serializable {

    public static final Row EMPTY_ARRAY[] = new Row[0];

    /**
     * Returns definitions of the fields, which available in this row.
     * Some implementations of this interface may not implement this method properly
     * @return fields metadata.
     */
    public BeanMetaData getMetaData();

    /**
     * Returns a value of the specified field.
     * @param name  field's name
     * @return specified field's value.
     * @throws Exception if specified field can't be accessed from this row
     */
    public Object getField(String name) throws Exception;

    /**
     * Set a value for specified field name.
     * @param name  field's name
     * @param value  new value of this field
     * @throws Exception if specified field can't be accessed from this row or value has incorrect type
     */
    public void setField(String name, Object value) throws Exception;

    /**
     * @return Map representation of this data.
     * @throws Exception in case of any errors.
     */
    public Map<String,Object> toMap() throws Exception;

}
