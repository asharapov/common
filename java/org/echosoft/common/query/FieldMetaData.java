package org.echosoft.common.query;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Map;

import org.echosoft.common.utils.BeanUtil;

/**
 * Describes one field in the row.
 * @author Anton Sharapov
 */
public class FieldMetaData implements Serializable {

    public static final FieldMetaData EMPTY_ARRAY[] = new FieldMetaData[0];

    public static FieldMetaData[] resolveMetaData(final Object bean) {
        final FieldMetaData[] fields;
        if (bean==null) {
            fields = EMPTY_ARRAY;
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
        return fields;
    }


    private final String fieldName;
    private final Class fieldClass;
    private final int fieldPrecision;
    private final int fieldScale;
    private final String mappedName;

    public FieldMetaData(String fieldName, Class fieldClass) {
        this(fieldName, fieldClass, 0, 0, fieldName);
    }

    public FieldMetaData(String fieldName, Class fieldClass, String mappedName) {
        this(fieldName, fieldClass, 0, 0, mappedName);
    }

    public FieldMetaData(String fieldName, Class fieldClass, int fieldPrecision, int fieldScale) {
        this(fieldName, fieldClass, fieldPrecision, fieldScale, fieldName);
    }

    public FieldMetaData(String fieldName, Class fieldClass, int fieldPrecision, int fieldScale, String mappedName) {
        if (fieldName==null || fieldClass==null)
            throw new NullPointerException("Name and class for field must be specified");
        this.fieldName = fieldName;
        this.fieldClass = fieldClass;
        this.fieldPrecision = fieldPrecision;
        this.fieldScale = fieldScale;
        this.mappedName = mappedName;
    }

    /**
     * Gets the name of the field.
     * @return field's name.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the Java class of the object stored for this field definition.
     * @return the class of the field.
     */
    public Class getFieldClass() {
        return fieldClass;
    }

    /**
     * Gets the number of decimal digits for this field definition.
     * @return number of decimal digits
     */
    public int getFieldPrecision() {
        return fieldPrecision;
    }

    /**
     * Gets the designated column's number of digits to right of the decimal point.
     * @return number of digits to right og the decimal point
     */
    public int getFieldScale() {
        return fieldScale;
    }

    /**
     * Gets the mapped name for given field in the persistence storage.
     * By default it same as field name.
     * @return  mapped name for given field in the persistence storage.
     * @since 0.7.6
     */
    public String getMappedName() {
        return mappedName;
    }


    public int hashCode() {
        return fieldName.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj==null || !FieldMetaData.class.equals(obj.getClass()))
            return false;
        final FieldMetaData other = (FieldMetaData)obj;
        return fieldName.equals(other.fieldName) && fieldClass.equals(other.fieldClass) &&
               fieldPrecision==other.fieldPrecision && fieldScale==other.fieldScale &&
               (mappedName!=null ? mappedName.equals(other.mappedName) : other.mappedName==null);
    }

    public String toString() {
        return "[FieldMetaData{name:"+fieldName+", class:"+fieldClass+", precision:"+fieldPrecision+", scale:"+fieldScale+", mapped:"+mappedName+"}]";
    }
}
