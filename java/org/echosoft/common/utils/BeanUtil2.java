package org.echosoft.common.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Anton Sharapov
 */
public class BeanUtil2 {

    public static Object getProperty(Object bean, String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (name==null)
            throw new IllegalArgumentException("No name specified");
        if (bean==null)
            return null;

        return null;
    }


    public static final class BeanMetadata {
        private Map<String,Accessor> accessors;
        private AccessorExtension extension;

        BeanMetadata(final Class<?> cl) throws IntrospectionException {
            final HashSet<String> acc = new HashSet<String>();  // список обработанных методов.
            final BeanInfo bi = Introspector.getBeanInfo(cl);
            for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
            }
        }
    }


    private static interface Accessor {
        public Object getValue(final Object bean) throws InvocationTargetException, IllegalAccessException;
        public void setValue(final Object bean, final Object value) throws InvocationTargetException, IllegalAccessException;
    }

    private static final class PropertyBeanAccessor implements Accessor {
        private final Method getter;
        private final Method setter;
        public PropertyBeanAccessor(final Method getter, final Method setter) {
            this.getter = getter;
            this.setter = setter;
        }
        public Object getValue(final Object bean) throws InvocationTargetException, IllegalAccessException {
            return bean!=null ? getter.invoke(bean) : null;
        }
        public void setValue(final Object bean, final Object value) throws InvocationTargetException, IllegalAccessException {
            setter.invoke(bean, value);
        }
    }

    private static final class FieldBeanAccessor implements Accessor {
        private final Field field;
        public FieldBeanAccessor(final Field field) {
            this.field = field;
        }
        public Object getValue(final Object bean) throws InvocationTargetException, IllegalAccessException {
            return bean!=null ? field.get(bean) : null;
        }
        public void setValue(final Object bean, final Object value) throws InvocationTargetException, IllegalAccessException {
            field.set(bean, value);
        }
    }


    private static interface AccessorExtension  {

    }

    private static final class IndexAccessorExtension implements AccessorExtension {

    }

    private static final class MapAccessorExtension implements AccessorExtension {

    }
}
