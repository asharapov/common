package org.echosoft.common.query;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.echosoft.common.utils.BeanUtil;

/**
 * Implementation of the {@link Row} interface.
 * @author Anton Sharapov
 */
public final class BeanRow<T> implements Row {

    private final T bean;
    private BeanMetaData metadata;

    public BeanRow(final T bean) {
        if (bean==null)
            throw new NullPointerException("Bean must be specified");
        this.bean = bean;
    }

    public BeanRow(final BeanMetaData metadata, final T bean) {
        if (bean==null)
            throw new NullPointerException("Bean must be specified");
        this.bean = bean;
        this.metadata = metadata;
    }


    /**
     * Returns information about supported fields in row (common for all rows in the rowset).
     * @return an instance {@link BeanMetaData}.
     */
    public BeanMetaData getMetaData() {
        if (metadata==null) {
            final FieldMetaData[] fields;
            if (bean==null) {
                fields = FieldMetaData.EMPTY_ARRAY;
            } else
            if (bean instanceof Map) {
                final Map<String,Object> map = (Map)bean;
                fields = new FieldMetaData[map.size()];
                int i=0;
                for (Map.Entry<String,Object> entry : map.entrySet()) {
                    fields[i++] = new FieldMetaData(entry.getKey(), entry.getValue()!=null ? entry.getValue().getClass() : String.class);
                }
            } else {
                final PropertyDescriptor desc[] = BeanUtil.getPropertyDescriptors(bean.getClass());
                fields = new FieldMetaData[desc.length];
                for (int i=0; i<desc.length; i++) {
                    fields[i] = new FieldMetaData(desc[i].getName(), desc[i].getPropertyType());
                }
            }
            metadata = new BeanMetaData(fields);
        }
        return metadata;
    }


    /**
     * Returns a value of the specified field.
     * @param name  field's name
     * @return specified field's value.
     * @throws Exception if specified field can't be accessed from this row
     */
    public Object getField(final String name) throws Exception {
        return BeanUtil.getProperty(bean, name);
    }

    /**
     * Set a value for specified field name.
     * @param name  field's name
     * @param value  new value of this field
     * @throws IllegalArgumentException if specified field can't be accessed from this row or value has incorrect type
     */
    public void setField(final String name, final Object value) throws Exception {
        BeanUtil.setProperty(bean, name, value);
    }


    /**
     * Converts row to map.
     * @return Map representation of this data.
     * @throws Exception if any errors occurs.
     */
    public Map<String,Object> toMap() throws Exception {
        final FieldMetaData[] fields = getMetaData().getFields();
        final HashMap<String,Object> result = new HashMap(fields.length);
        for (int i=0; i<fields.length; i++) {
            final String name = fields[i].getFieldName();
            result.put(name, getField(name));
        }
        return result;
    }

    /**
     * Retrieves bean.
     * @return  bean instance.
     */
    public Object getBean() {
        return bean;
    }


    public int hashCode() {
        return bean.hashCode();
    }

    public boolean equals(Object obj) {
        return (obj instanceof BeanRow)  &&  bean.equals(((BeanRow)obj).bean);
    }

    public String toString() {
        return "[Row{"+bean+"}]";
    }
}
