package org.echosoft.common.query;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.echosoft.common.utils.BeanUtil;

/**
 * Describes fields in the bean.
 *
 * @author Anton Sharapov
 */
public class BeanMetaData implements Serializable {

    public static BeanMetaData resolveMetaData(final Object bean) {
        final FieldMetaData[] fields;
        if (bean==null) {
            fields = FieldMetaData.EMPTY_ARRAY;
        } else
        if (bean instanceof Map) {
            final Map map = (Map)bean;
            fields = new FieldMetaData[map.size()];
            int i=0;
            for (Iterator it=map.entrySet().iterator(); it.hasNext(); ) {
                final Map.Entry entry = (Map.Entry)it.next();
                if (!(entry.getKey() instanceof String))
                    continue;
                fields[i++] = new FieldMetaData((String)entry.getKey(), entry.getValue()!=null ? entry.getValue().getClass() : String.class);
            }
        } else {
            final PropertyDescriptor desc[] = BeanUtil.getPropertyDescriptors(bean.getClass());
            fields = new FieldMetaData[desc.length];
            for (int i=0; i<desc.length; i++) {
                final Class cls = desc[i].getPropertyType();
                fields[i] = new FieldMetaData(desc[i].getName(), cls!=null ? cls : Object.class);
            }
        }
        return new BeanMetaData(fields);
    }


    private final FieldMetaData[] fields;

    public BeanMetaData(FieldMetaData[] fields) {
        if (fields==null)
            throw new IllegalArgumentException("Fields metadata must be specified");
        this.fields = fields;
    }

    public FieldMetaData[] getFields() {
        return fields;
    }

    public FieldMetaData getField(String name) {
        for (int i=0; i<fields.length; i++) {
            if (fields[i].getFieldName().equals(name))
                return fields[i];
        }
        return null;
    }

    public boolean containsField(String name) {
        for (int i=0; i<fields.length; i++) {
            if (fields[i].getFieldName().equals(name))
                return true;
        }
        return false;
    }


    public int hashCode() {
        return fields.length;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BeanMetaData))
            return false;
        final FieldMetaData other[] = ((BeanMetaData)obj).fields;
        for (int i=0; i<fields.length; i++) {
            if (!fields[i].equals(other[i]))
                return false;
        }
        return true;
    }
}
