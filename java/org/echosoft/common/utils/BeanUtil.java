package org.echosoft.common.utils;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.echosoft.common.model.Predicate;

/**
 * <p>Utility methods for using Java Reflection APIs to facilitate generic
 * property getter and setter operations on Java objects.</p>
 * <b>Note:</b> this class properly works only with public classes and only with public properties/fields. 
 * @author Anton Sharapov
 */
public class BeanUtil {

    public static final boolean USE_STRICT_CHECKS = false;

    /**
     * The delimiter that preceeds the zero-relative subscript for an
     * indexed reference.
     */
    public static final char INDEXED_DELIM = '[';

    /**
     * The delimiter that follows the zero-relative subscript for an
     * indexed reference.
     */
    public static final char INDEXED_DELIM2 = ']';

    /**
     * The delimiter that preceeds the key of a mapped property.
     */
    public static final char MAPPED_DELIM = '(';

    /**
     * The delimiter that follows the key of a mapped property.
     */
    public static final char MAPPED_DELIM2 = ')';

    /**
     * The delimiter that separates the components of a nested reference.
     */
    public static final char NESTED_DELIM = '.';


    /**
     * The cache of PropertyDescriptor arrays for beans we have already
     * introspected, keyed by the java.lang.Class of this object.
     */
    private static final HashMap<Class,PropertyDescriptor[]> descriptorsCache = new HashMap<Class,PropertyDescriptor[]>();

    /**
     * The cache of Class.getDeclaredMethods:
     */
    private static final HashMap<Class,Method[]> methodsCache = new HashMap<Class,Method[]>();

    private static final PropertyDescriptor EMPTY_DESCRIPTORS_ARRAY[] = new PropertyDescriptor[0];
    private static final Object EMPTY_OBJ_ARRAY[] = new Object[0];
    private static final Predicate<Method> NO_PARAMS_METHOD =
            new Predicate<Method>() {
                public boolean accept(final Method method) {
                    return method.getParameterTypes().length == 0;
                }
            };
    private static final Predicate<Method> ONE_PARAM_METHOD =
            new Predicate<Method>() {
                public boolean accept(final Method method) {
                    final Class params[] = method.getParameterTypes();
                    if (params.length != 1)
                        return false;
                    final Class param = params[0];
                    return String.class.equals(param) || Integer.class.equals(param) || "int".equals(param.getName());
                }
            };
    private static final Predicate<Method> TWO_PARAMS_METHOD =
            new Predicate<Method>() {
                public boolean accept(final Method method) {
                    final Class params[] = method.getParameterTypes();
                    if (params.length != 2)
                        return false;
                    final Class param = params[0];
                    return (String.class.equals(param) || Integer.class.equals(param)) &&
                           (Object.class.equals(params[1]));
                }
            };


    /**
     * Retrieve the property descriptors for the specified class, introspecting and caching
     * them the first time a particular bean class is encountered.
     * @param beanClass  class of bean.
     * @return  an array of {@link PropertyDescriptor} instances.
     */
    public static PropertyDescriptor[] getPropertyDescriptors(final Class beanClass) {
        PropertyDescriptor descriptors[] = descriptorsCache.get(beanClass);
        if (descriptors == null) {
            // Introspect the bean and cache the generated descriptors
            try {
                final BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
                descriptors = beanInfo.getPropertyDescriptors();
                if (descriptors == null) {
                    descriptors = new PropertyDescriptor[0];
                }
            } catch (IntrospectionException e) {
                descriptors = EMPTY_DESCRIPTORS_ARRAY;
            }
            descriptorsCache.put(beanClass, descriptors);
        }
        return descriptors;
    }

   /**
    * Return the value of the specified (possibly nested) property of the specified bean, no
    * matter which property reference format is used, with no type conversions.
    * @param bean Bean whose property is to be extracted
    * @param name Possibly indexed and/or nested name of the property to be extracted.
    * @return  value of the specified property or <code>null</code>.
    * @exception IllegalArgumentException if <code>bean</code> or <code>name</code> is null.
    * @exception IllegalAccessException if the caller does not have access to the property accessor method.
    * @exception InvocationTargetException if the property accessor method throws an exception.
    * @exception NoSuchMethodException if an accessor method for this propety cannot be found.
    */
    public static Object getProperty(Object bean, String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (name==null)
            throw new IllegalArgumentException("No name specified");
        if (bean==null)
            return null;

        int start = 0;
        final int length = name.length();
        for (int i=0; i<length; i++) {
            final char c = name.charAt(i);
            if (c==NESTED_DELIM) {
                bean = getSimpleProperty(bean, name.substring(start, i));
                if (bean==null)
                    return null;
                start = i+1;
            } else
            if (c==INDEXED_DELIM) {
                final int r = name.indexOf(INDEXED_DELIM2, i);
                if (r<i)
                    throw new IllegalArgumentException("Missed ']' symbol: "+name);
                final int index = Integer.parseInt( name.substring(i+1,r) );
                bean = getIndexedProperty(bean, name.substring(start,i), index);
                if (bean==null)
                    return null;
                i = (r+1<length && name.charAt(r+1)==NESTED_DELIM) ? r+1 : r;
                start = i+1;
            } else
            if (c==MAPPED_DELIM) {
                final int r = name.indexOf(MAPPED_DELIM2, i);
                if (r<i)
                    throw new IllegalArgumentException("Missed ')' symbol: "+name);
                final String key = name.substring(i+1,r);
                bean = getMappedProperty(bean, name.substring(start,i), key);
                if (bean==null)
                    return null;
                i = (r+1<length && name.charAt(r+1)==NESTED_DELIM) ? r+1 : r;
                start = i+1;
            }
        }

       return start<length
               ? getSimpleProperty(bean, name.substring(start))
               : bean;
    }


    /**
     * Set the value of the (possibly nested) property of the specified
     * name, for the specified bean, with no type conversions.
     *
     * @param bean Bean whose property is to be modified
     * @param name Possibly nested name of the property to be modified
     * @param value Value to which the property is to be set
     * @exception NullPointerException if <code>bean</code> or <code>name</code> is null.
     * @exception IllegalAccessException if the caller does not have access to the property accessor method.
     * @exception InvocationTargetException if the property accessor method throws an exception.
     * @exception NoSuchMethodException if an accessor method for this propety cannot be found.
     */
    public static void setProperty(Object bean, String name, Object value) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (name==null)
            throw new IllegalArgumentException("No name specified");

        final int pos = name.lastIndexOf(NESTED_DELIM);
        if (pos>=0) {
            bean = getProperty(bean, name.substring(0,pos));
            name = name.substring(pos+1);
        }

        if (bean==null)
            throw new NullPointerException("Bean must be specified");
        if (name.length()==0)
            throw new IllegalArgumentException("No name specified");

        final int il = name.indexOf(INDEXED_DELIM);
        final int ml = name.indexOf(MAPPED_DELIM);
        if (il>=0 && ml>=0)
            throw new IllegalArgumentException("Invalid property name: '"+name+"'");

        if (il>=0) {
            final int ir = name.indexOf(INDEXED_DELIM2, il);
            if (ir<il)
                throw new IllegalArgumentException("Missed ']' symbol: "+name);
            final int index = Integer.parseInt( name.substring(il+1,ir) );
            setIndexedProperty(bean, name.substring(0,il), index, value);
        } else
        if (ml>=0) {
            final int mr = name.indexOf(MAPPED_DELIM2, ml);
            if (mr<ml)
                throw new IllegalArgumentException("Missed ')' symbol: "+name);
            final String key = name.substring(ml+1,mr);
            setMappedProperty(bean, name.substring(0,ml), key, value);
        } else {
            setSimpleProperty(bean, name, value);
        }
    }


    private static Object getSimpleProperty(final Object bean, final String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (bean instanceof Map) {
            // special workaround for Map interface...
            return ((Map)bean).get(name);
        }
        if (bean instanceof Map.Entry) {
            // special workaround for NON PUBLIC Map.Entry interface...
            final Map.Entry entry = (Map.Entry)bean;
            if ("key".equals(name)) {
                return entry.getKey();
            } else
            if ("value".equals(name)) {
                return entry.getValue();
            } else
                throw new NoSuchMethodException("Unknown property '" +name + "' for bean: " + bean);
        }

        final PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
        if (descriptor != null) {
            final Method method = getAccessibleMethod(descriptor.getReadMethod());
            if (method == null)
                throw new NoSuchMethodException("Property '" + name + "' hasn't getter method for bean: " + bean);
            // Call the property getter and return the value
            try {
                return method.invoke(bean, EMPTY_OBJ_ARRAY);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Cannot invoke "+method.getDeclaringClass().getName()+"."+method.getName()+" - "+e.getMessage());
            }
        } else {
            final Method method = findMethod(bean.getClass(), name, NO_PARAMS_METHOD);
            if (method!=null) {
                try {
                    return method.invoke(bean, EMPTY_OBJ_ARRAY);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Cannot invoke "+method.getDeclaringClass().getName()+"."+method.getName()+" - "+e.getMessage());
                }
            }

            final Field field = findField(bean.getClass(), name);
            if (field != null) {
                return field.get(bean);
            } else
                throw new NoSuchMethodException("Unknown property '" + name + "' for bean: " + bean);
        }
    }


    private static Object getIndexedProperty(final Object bean, final String name, final int index) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
        if (descriptor == null)
            throw new NoSuchMethodException("Unknown property '" + name + "' for bean: " + bean);

        // Call the indexed getter method if there is one
        if (descriptor instanceof IndexedPropertyDescriptor) {
            final Method method = ((IndexedPropertyDescriptor)descriptor).getIndexedReadMethod();
            if (method != null) {
                try {
                    return method.invoke(bean, index);
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof ArrayIndexOutOfBoundsException) {
                        throw (ArrayIndexOutOfBoundsException)e.getTargetException();
                    } else {
                        throw e;
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Cannot invoke "+method.getDeclaringClass().getName()+"."+method.getName()+" - "+e.getMessage());
                }
            }
        }

        // Otherwise, the underlying property must be an array
        final Method method = getAccessibleMethod(descriptor.getReadMethod());
        if (method == null)
            throw new NoSuchMethodException("Property '" + name + "' hasn't getter method for bean: " + bean);
        // Call the property getter and return the value
        final Object value;
        try {
            value = method.invoke(bean, EMPTY_OBJ_ARRAY);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot invoke "+method.getDeclaringClass().getName()+"."+method.getName()+" - "+e.getMessage());
        }
        if (value==null)
            return null;
        if (value.getClass().isArray()) {
            return (Array.get(value, index));
        } else {
            if (value instanceof List) {
                return ((List)value).get(index);
            } else {
                throw new IllegalArgumentException("Property '" + name + "' is not indexed for bean: " + bean);
            }
        }
    }


    private static Object getMappedProperty(final Object bean, final String name, final String key) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (name.length()==0) {
            if (bean instanceof Map) {
                return ((Map)bean).get(key);
            } else
                throw new IllegalArgumentException("Can't invoke '("+key+")' for bean: "+bean);
        }

        final PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
        if (descriptor!=null && descriptor.getReadMethod()!=null) {
            final Method method = descriptor.getReadMethod();
            final Object value;
            try {
                value = method.invoke(bean, EMPTY_OBJ_ARRAY);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Cannot invoke "+method.getDeclaringClass().getName()+"."+method.getName()+" - "+e.getMessage());
            }
            if (value instanceof Map) {
                return ((Map)value).get(key);
            } else
                throw new IllegalArgumentException("Can't invoke '"+name+"("+key+")' for bean: "+bean);
        }

        final char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        final String methodName = "get"+new String(chars);
        Method method = findMethod(bean.getClass(), methodName, ONE_PARAM_METHOD);
        if (method==null)
            method = findMethod(bean.getClass(), name, ONE_PARAM_METHOD);
        if (method == null)
            throw new NoSuchMethodException("Property '" + name + "' hasn't mapped getter method");
        try {
            final Object[] params = new Object[1];
            params[0] = prepareParameter(method.getParameterTypes()[0], key);
            return method.invoke(bean, params);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot invoke "+method.getDeclaringClass().getName()+"."+method.getName()+" - "+e.getMessage());
        }
    }



    private static void setSimpleProperty(final Object bean, final String name, final Object value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (bean instanceof Map) {
            ((Map)bean).put(name, value);
            return;
        }

        final PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
        if (descriptor != null) {
            final Method method = getAccessibleMethod(descriptor.getWriteMethod());
            if (method == null)
                throw new NoSuchMethodException("Property '" + name + "' hasn't setter method for bean: " + bean);
            // Call the property setter and return the value
            try {
                method.invoke(bean, value);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Cannot invoke "+method.getDeclaringClass().getName()+"."+method.getName()+" - "+e.getMessage());
            }
        } else {
            final Field field = findField(bean.getClass(), name);
            if (field != null) {
                field.set(bean, value);
            } else
                throw new NoSuchMethodException("Unknown property '" +name + "' for bean: " + bean);
        }
    }


    private static void setIndexedProperty(final Object bean, final String name, final int index, final Object value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
        if (descriptor == null)
            throw new NoSuchMethodException("Unknown property '" +name + "' for bean: " + bean);

        // Call the indexed getter method if there is one
        if (descriptor instanceof IndexedPropertyDescriptor) {
            final Method method = ((IndexedPropertyDescriptor)descriptor).getIndexedWriteMethod();
            if (method != null) {
                try {
                    method.invoke(bean, index, value);
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof ArrayIndexOutOfBoundsException) {
                        throw (ArrayIndexOutOfBoundsException)e.getTargetException();
                    } else {
                        throw e;
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Cannot invoke "+method.getDeclaringClass().getName()+"."+method.getName()+" - "+e.getMessage());
                }
                return;
            }
        }

        // Otherwise, the underlying property must be an array
        final Method method = getAccessibleMethod(descriptor.getReadMethod());
        if (method == null)
            throw new NoSuchMethodException("Property '" + name + "' hasn't getter method for bean: " + bean);
        // Call the property getter and return the value
        final Object array;
        try {
            array = method.invoke(bean, EMPTY_OBJ_ARRAY);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot invoke "+method.getDeclaringClass().getName()+"."+method.getName()+" - "+e.getMessage());
        }
        if (array.getClass().isArray()) {
            Array.set(array, index, value);
        } else {
            if (array instanceof List) {
                ((List)array).set(index, value);
            } else {
                throw new IllegalArgumentException("Property '" + name + "' is not indexed for bean: " + bean);
            }
        }
    }


    private static void setMappedProperty(final Object bean, final String name, final String key, final Object value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (name.length()==0) {
            if (bean instanceof Map) {
                ((Map)bean).put(key, value);
            } else
                throw new IllegalArgumentException("Can't invoke '("+key+")' for bean: "+bean);
        }

        final PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
        if (descriptor!=null && descriptor.getReadMethod()!=null) {
            final Method method = descriptor.getReadMethod();
            final Object obj;
            try {
                obj = method.invoke(bean, EMPTY_OBJ_ARRAY);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Cannot invoke "+method.getDeclaringClass().getName()+"."+method.getName()+" - "+e.getMessage());
            }
            if (obj instanceof Map) {
                ((Map)obj).put(key, value);
                return;
            } else
                throw new IllegalArgumentException("Can't invoke '"+name+"("+key+")' for bean: "+obj);
        }

        final char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        final String methodName = "set"+new String(chars);
        final Method method = findMethod(bean.getClass(), methodName, TWO_PARAMS_METHOD);
        if (method == null)
            throw new NoSuchMethodException("Property '" + name + "' hasn't mapped setter method for bean: " + bean);
        try {
            final Class[] types = method.getParameterTypes();
            final Object[] params = new Object[2];
            params[0] = prepareParameter(types[0], key);
            params[1] = value;
            method.invoke(bean, params);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot invoke "+method.getDeclaringClass().getName()+"."+method.getName()+" - "+e.getMessage());
        }
    }





    private static Object prepareParameter(final Class paramClass, final String param) {
        if (String.class.equals(paramClass)) {
            return param;
        } else
        if (Integer.class.equals(paramClass)) {
            return Integer.valueOf(param);
        } else
        if ("int".equals(paramClass.getName())) {
            return Integer.valueOf(param);
        } else
            throw new IllegalArgumentException("Unsupported parameter type: "+paramClass);
    }

    private static PropertyDescriptor getPropertyDescriptor(final Object bean, final String name) {
        final PropertyDescriptor descriptors[] = getPropertyDescriptors(bean.getClass());
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].getName().equals(name))
                return descriptors[i];
        }
        return null;
    }

    private static Field findField(final Class clz, final String fieldName) {
        try {
            return clz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * Internal support for finding a target methodName with a given
     * parameter list on a given class.
     * @param start  class which contains given method
     * @param methodName  name of the method
     * @param predicate  describes rules for required method selection from list. Can't be <code>null</code>.
     * @return method with given parameters or <code>null</code> if such parameter was not found.
     */
    private static Method findMethod(final Class start, final String methodName, final Predicate<Method> predicate) {
        // For overriden methods we need to find the most derived version.
        // So we start with the given class and walk up the superclass chain.
        for (Class cl=start; cl!=null; cl=cl.getSuperclass()) {
            final Method methods[] = getPublicDeclaredMethods(cl);
            for (int i=0; i<methods.length; i++) {
                final Method method = methods[i];
                if (method==null || !method.getName().equals(methodName))
                    continue;
                if (predicate.accept(method))
                    return method;
            }
        }
        // Now check any inherited interfaces.  This is necessary both when
        // the argument class is itself an interface, and when the argument
        // class is an abstract class.
        final Class ifcs[] = start.getInterfaces();
        for (int i = 0; i < ifcs.length; i++) {
            final Method method = findMethod(ifcs[i], methodName, predicate);
            if (method != null) {
                return method;
            }
        }
        return null;
    }


    /**
     * Internal method to return *public* methods within a class.
     * @param clz  class that contains requested methods.
     * @return an  array of methods. Something elements of that array can be <code>null</code>.
     */
    private static synchronized Method[] getPublicDeclaredMethods(final Class clz) {
        // Looking up Class.getDeclaredMethods is relatively expensive,
        // so we cache the results.
        Method[] methods = methodsCache.get(clz);
        if (methods == null) {
            // We have to raise privilege for getDeclaredMethods
            methods = AccessController.doPrivileged(new PrivilegedAction<Method[]>() {
                        public Method[] run() {
                            try {
                                return clz.getDeclaredMethods();
                            } catch (SecurityException ex) {
                                // this means we're in a limited security environment
                                // so let's try going through the public methods
                                // and null those those that are not from the declaring
                                // class
                                final Method[] methods = clz.getMethods();
                                for (int i=0; i<methods.length; i++) {
                                    if (!(clz.equals(methods[i].getDeclaringClass()))) {
                                        methods[i] = null;
                                    }
                                }
                                return methods;
                            }
                        }
                      });
            // Null out any non-public methods.
            for (int i = 0; i < methods.length; i++) {
                if (methods[i]==null)
                    continue;
                final int mods = methods[i].getModifiers();
                if (!Modifier.isPublic(mods) || Modifier.isStatic(mods)) {
                    methods[i] = null;
                }
            }
            // Add it to the cache.
            methodsCache.put(clz, methods);
        }
        return methods;
    }


    /**
     * <p>Return an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified Method.  If no such method
     * can be found, return <code>null</code>.</p>
     *
     * @param method The method that we wish to call
     * @return  method passed from params or <code>null</code> if method is not public.
     */
    private static Method getAccessibleMethod(final Method method) {
        // Make sure we have a method to check
        if (method == null)
            return null;

        // If the requested method is not public we cannot call it
        if (!Modifier.isPublic(method.getModifiers()))
            return null;

        if (USE_STRICT_CHECKS) {
            // If the declaring class is public, we are done
            final Class clazz = method.getDeclaringClass();
            if (Modifier.isPublic(clazz.getModifiers()))
                return method;

            // Check the implemented interfaces and subinterfaces
            return getAccessibleMethodFromInterfaceNest(clazz, method.getName(), method.getParameterTypes());
        } else {
            return method;
        }
    }

    /**
     * <p>Return an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified method, by scanning through
     * all implemented interfaces and subinterfaces.  If no such method
     * can be found, return <code>null</code>.</p>
     *
     * <p> There isn't any good reason why this method must be private.
     * It is because there doesn't seem any reason why other classes should
     * call this rather than the higher level methods.</p>
     *
     * @param clazz Parent class for the interfaces to be checked
     * @param methodName Method name of the method we wish to call
     * @param parameterTypes The parameter type signatures
     * @return an appropriated method or <code>null</code>.
     */
    private static Method getAccessibleMethodFromInterfaceNest(Class clazz, String methodName, Class parameterTypes[]) {
        // Search up the superclass chain
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            // Check the implemented interfaces of the parent class
            final Class interfaces[] = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                // Is this interface public?
                if (!Modifier.isPublic(interfaces[i].getModifiers()))
                    continue;
                // Does the method exist on this interface?
                try {
                    return interfaces[i].getDeclaredMethod(methodName,parameterTypes);
                } catch (NoSuchMethodException e) {
                    // no errors ...
                }
                // Recursively check our parent interfaces
                final Method method = getAccessibleMethodFromInterfaceNest(interfaces[i], methodName, parameterTypes);
                if (method != null)
                    return method;
            }
        }
        return null;
    }

}
