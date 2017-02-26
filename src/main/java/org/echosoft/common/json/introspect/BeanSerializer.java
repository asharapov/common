package org.echosoft.common.json.introspect;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;

import org.echosoft.common.json.JsonSerializer;
import org.echosoft.common.json.JsonUtil;
import org.echosoft.common.json.JsonWriter;
import org.echosoft.common.json.annotate.JsonField;
import org.echosoft.common.json.annotate.JsonWriteNulls;

/**
 * Сериализует объекты соответствующего java класса в JSON формат.
 *
 * @author Anton Sharapov
 */
public final class BeanSerializer implements JsonSerializer {

    public static MemberAccessor[] getMembers(final Class<?> cl) {
        final JsonWriteNulls jwna = cl.getAnnotation(JsonWriteNulls.class);
        final boolean defWriteNulls = jwna == null || jwna.value();
        String name;
        boolean writeNulls;
        boolean dereference;
        final ArrayList<MemberAccessor> list = new ArrayList<>();
        final HashSet<String> properties = new HashSet<>();
        for (final JsonUtil.NamedMethod entry : JsonUtil.findGetters(cl)) {
            final Method method = entry.method;
            final JsonField jfa = method.getAnnotation(JsonField.class);
            if (jfa != null) {
                name = jfa.name().isEmpty() ? entry.name : jfa.name();
                if (jfa.isTransient()) {
                    properties.add(name);
                    continue;
                }
                writeNulls = jfa.writeNulls();
                dereference = jfa.dereference();
            } else {
                name = entry.name;
                writeNulls = defWriteNulls;
                dereference = false;
            }
            if (dereference) {
                list.add(new DereferencedMembersAccessor(method));
            } else {
                list.add(new MethodMemberAccessor(name, method, writeNulls));
            }
            properties.add(name);
        }
        for (final Field field : cl.getFields()) {
            final int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isTransient(mod))
                continue;
            final JsonField jfa = field.getAnnotation(JsonField.class);
            if (jfa != null) {
                if (jfa.isTransient())
                    continue;
                name = jfa.name().isEmpty() ? field.getName() : jfa.name();
                writeNulls = jfa.writeNulls();
                dereference = jfa.dereference();
            } else {
                name = field.getName();
                writeNulls = defWriteNulls;
                dereference = false;
            }
            if (properties.contains(name))
                continue;
            if (dereference) {
                list.add(new DereferencedMembersAccessor(field));
            } else {
                list.add(new FieldMemberAccessor(name, field, writeNulls));
            }
        }
        return list.toArray(new MemberAccessor[list.size()]);
    }

    private final Class cl;
    private final MemberAccessor[] accessors;

    public BeanSerializer(final Class cl) {
        this.cl = cl;
        this.accessors = getMembers(cl);
    }


    @Override
    public void serialize(final Object src, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
        jw.beginObject();
        for (int i = 0; i < accessors.length; i++) {
            accessors[i].serialize(src, jw);
        }
        jw.endObject();
    }


    @Override
    public int hashCode() {
        return cl.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;
        final BeanSerializer other = (BeanSerializer) obj;
        return cl.equals(other.cl);
    }

    @Override
    public String toString() {
        return "[BeanSerializer{class:" + cl + "}]";
    }
}
