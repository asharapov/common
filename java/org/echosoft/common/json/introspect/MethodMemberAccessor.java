package org.echosoft.common.json.introspect;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.echosoft.common.json.JsonWriter;

/**
 * @author Anton Sharapov
 */
public final class MethodMemberAccessor implements MemberAccessor {

    private final String name;
    private final Method method;
    private final boolean writeNulls;

    public MethodMemberAccessor(final String name, final Method method, final boolean writeNulls) {
        this.name = name;
        this.writeNulls = writeNulls;
        this.method = method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final Object bean, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
        final  Object value = method.invoke(bean);
        if (value!=null || writeNulls) {
            jw.writeProperty(name, value);
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final MethodMemberAccessor other = (MethodMemberAccessor)obj;
        return name.equals(other.name) && method.equals(other.method);
    }

    @Override
    public String toString() {
        return "[MethodMemberAccessor{name:"+name+", method:"+method+", writeNulls:"+writeNulls+"}]";
    }
}
