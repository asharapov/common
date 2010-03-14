package org.echosoft.common.query;

import java.sql.ResultSet;

/**
 * @author Anton Sharapov
 */
public abstract class AbstractBeanLoader<T> implements BeanLoader<T> {

    private final FieldMetaData metadata[];

    public AbstractBeanLoader() {
        metadata = init();
    }

    /**
     * Returns name of the sql query field for specified property name of the bean.
     * This method may be used for making sql queries.
     *
     * @param fieldName name of the bean's attribute
     * @return name of the sql query field
     */
    public String getMappedField(String fieldName) {
        for (FieldMetaData fm : metadata) {
            if (fm.getFieldName().equals(fieldName)) {
                return fm.getMappedName();
            }
        }
        return null;
    }

    /**
     * Returns fields metadata for the specified bean class and sql result set.
     *
     * @return fields metadata for the specified bean class and sql result set.
     */
    public FieldMetaData[] getMetadata() {
        return metadata;
    }

    /**
     * Makes and populates new instance of bean from result set.
     *
     * @param rs jdbc ResultSet.
     * @return object instance with populated from result set properties.
     * @throws Exception if any errors occurs
     */
    public abstract T load(ResultSet rs) throws Exception;

    protected abstract FieldMetaData[] init();

}
