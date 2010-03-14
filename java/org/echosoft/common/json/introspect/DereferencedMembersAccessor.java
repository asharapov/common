package org.echosoft.common.json.introspect;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.echosoft.common.json.JsonWriter;

/**
 * @author Anton Sharapov
 */
public final class DereferencedMembersAccessor implements MemberAccessor {

    private final Method method;
    private final Field field;
    private final ConcurrentHashMap<Class<?>, MemberAccessor[]> cache;

    public DereferencedMembersAccessor(final Method method) {
        this.method = method;
        this.field = null;
        this.cache = new ConcurrentHashMap<Class<?>, MemberAccessor[]>();
    }

    public DereferencedMembersAccessor(final Field field) {
        this.method = null;
        this.field = field;
        this.cache = new ConcurrentHashMap<Class<?>, MemberAccessor[]>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final Object src, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
        final Object bean = method!=null ? method.invoke(src) : field.get(src);
        if (bean==null)
            return;
        final Class<?> cl = bean.getClass();
        MemberAccessor[] members = cache.get(cl);
        if (members==null) {
            members = BeanSerializer.getMembers(cl);
            cache.put(cl, members);
        }
        for (final MemberAccessor member : members) {
            member.serialize(bean, jw);
        }
    }

    @Override
    public int hashCode() {
        return method!=null ? method.hashCode() : field.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final DereferencedMembersAccessor other = (DereferencedMembersAccessor)obj;
        return method!=null ? method.equals(other.method) : field.equals(other.field);
    }

    @Override
    public String toString() {
        return "[DereferencedMembersAccessor{"+(method!=null ? "method: "+method.getName() : "field: "+field.getName())+"}]";
    }
}
