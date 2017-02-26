package org.echosoft.common.json.introspect;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.echosoft.common.json.JsonWriter;

/**
 * @author Anton Sharapov
 */
public final class FieldMemberAccessor implements MemberAccessor {

    private final String name;
    private final Field field;
    private final boolean writeNulls;

    public FieldMemberAccessor(final String name, final Field field, final boolean writeNulls) {
        this.name = name;
        this.field = field;
        this.writeNulls = writeNulls;
        field.setAccessible(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final Object bean, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
        final Object value = field.get(bean);
        if (value != null || writeNulls) {
            jw.writeProperty(name, value);
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;
        final FieldMemberAccessor other = (FieldMemberAccessor) obj;
        return name.equals(other.name) && field.equals(other.field);
    }

    @Override
    public String toString() {
        return "[FieldMemberAccessor{name:" + name + ", field:" + field + ", writeNulls:" + writeNulls + "}]";
    }
}