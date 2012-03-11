package org.echosoft.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import org.echosoft.common.collections.ArrayIterator;
import org.echosoft.common.collections.EnumerationIterator;
import org.echosoft.common.collections.ObjectArrayIterator;

/**
 * Содержит часто используемые методы для работы с объектами произвольных классов.
 *
 * @author  Andrey Ochirov
 * @author  Anton Sharapov
 */
public class ObjectUtil {

    public static final int READ_STREAM_BUFFER_SIZE = 4096;

    private ObjectUtil() {}

    /**
     * Сериализует объект в массив байтов. Объект должен быть сериализуемым (т.е. реализовывать интерфейс {@link Serializable}).
     * @param value  объект, чье состояние должно быть сериализовано в массив байтов.
     * @return массив байтов содержащий сериализованное состояние объекта.
     * @throws RuntimeException  в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static byte[] objectToBytes(final Object value) {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Десериализует содержимое массива байтов в новый экземпляр объекта соответствующего класса.
     * @param bytes  массив байтов содержащий сериализованное состояние объекта.
     * @return  десериализованный экземпляр объекта или <code>null</code> если в аргументе вместо массива байт был передан <code>null</code>.
     * @throws RuntimeException  в случае каких-либо ошибок ввода-вывода или в случае когда JVM не может найти информацию о классе десериализуемого объекта.
     */
    public static Object bytesToObject(final byte[] bytes) {
        if (bytes == null)
            return null;
        try {
            final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            final ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Клонирует указанный в аргументе экземпляр объекта путем последовательной его сериализации и десериализации.<br/>
     * <strong>Важно!</strong> Данный способ клонирования объектов представляет собой очень дорогую по времени операцию и его следует применять с осторожностью.
     * @param  o объект который требуется клонировать.
     * @return  клон объекта.
     * @throws RuntimeException  в случае возникновения каких-либо ошибок ввода-вывода.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T clone(final T o) {
        return (T) bytesToObject(objectToBytes(o));
    }


    /**
     * Читает <u>все</u> содержимое потока в массив байт.
     * @param in  входной поток данных.
     * @return  массив байт что были прочитаны из потока.
     * @throws IOException  в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static byte[] streamToBytes(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(READ_STREAM_BUFFER_SIZE);
        final byte[] buf = new byte[READ_STREAM_BUFFER_SIZE];
        for (int size=in.read(buf); size>0; size=in.read(buf)) {
            out.write(buf, 0, size);
        }
        return out.toByteArray();
    }

    /**
     * Заполняет указанный в аргументе массив байт содержимым входного потока.
     * @param stream  входной поток.
     * @param buf буфер куда помещается прочитанные из потока байты..
     * @return реальное количество прочитанных байт. Может изменяться в диапазоне от 0 до N, где N - размер массива.
     *  Если возвращаемое методом значение меньшее чем N значит поток был исчерпан полностью,
     *  в противном случае - возможно (!) в потоке есть еще непрочитанное нами содержимое.
     * @throws IOException  в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static int readFromStream(final InputStream stream, final byte[] buf) throws IOException {
        final int length = buf.length;
        int readed = 0;
        for (; ; ) {
            final int n = stream.read(buf, readed, length - readed);
            if (n < 0)
                return readed;
            readed += n;
            if (readed >= length)
                return readed;
        }
    }


    /**
     * Читает содержимое входного потока и помещает его в выходной поток.
     * @param in  входной поток некоторых бинарных данных.
     * @param out  выходной поток некоторых бинарных данных.
     * @throws IOException  в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static void pipeData(final InputStream in, final OutputStream out) throws IOException {
        if (in == null)
            return;
        final byte[] buf = new byte[READ_STREAM_BUFFER_SIZE];
        int size;
        while ((size = in.read(buf)) > 0) {
            out.write(buf, 0, size);
        }
    }

    /**
     * Читает содержимое входного потока и помещает его в выходной поток.
     * @param in  входной поток некоторых символьных данных.
     * @param out  выходной поток некоторых символьных данных.
     * @throws IOException  в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static void pipeData(final Reader in, final Writer out) throws IOException {
        if (in == null)
            return;
        final char[] buf = new char[READ_STREAM_BUFFER_SIZE];
        int size;
        while ((size = in.read(buf)) > 0) {
            out.write(buf, 0, size);
        }
    }


    /**
     * Упаковывает переданный в аргументе массив байт.
     * @param data  массив байт который требуется упаковать.
     * @return  упакованная версия того же массива сжатая при помощи алгоритма GZIP.
     * @throws IOException  в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static byte[] zipBytes(final byte[] data) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
        final DeflaterOutputStream zos = new GZIPOutputStream(bos);
        try {
            zos.write(data);
        } finally {
            zos.close();
        }
        return bos.toByteArray();
    }

    /**
     * Распаковывает переданный в аргументе заархивированный при помощи алгоритма GZIP массив байт.
     * @param data  массив байт который требуется распаковать.
     * @return  распакованная версия переданного в аргументе массива.
     * @throws IOException  в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static byte[] unzipBytes(final byte[] data) throws IOException {
        final InflaterInputStream in = new GZIPInputStream(new ByteArrayInputStream(data));
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream(READ_STREAM_BUFFER_SIZE);
            final byte[] buf = new byte[READ_STREAM_BUFFER_SIZE];
            for (int size=in.read(buf); size>0; size=in.read(buf)) {
                out.write(buf, 0, size);
            }
            return out.toByteArray();
        } finally {
            in.close();
        }
    }

    /**
     * Сравнивает два объекта, реализующих интерфейс Comparable, каждый из которых может быть <code>null</code>.
     * @param obj1  первый объект для сравнения.
     * @param obj2  второй объект для сравнения.
     * @return
     *  <ul>
     *   <li> <code>-1</code>  если первый объект меньше второго или только первый объект равен <code>null</code>.
     *   <li> <code>0</code>  если оба объекта одинаковы или оба равны <code>null</code>.
     *   <li> <code>1</code>  если первый объект больше второго или только второй объект равен <code>null</code>.
     *  </ul>
     */
    public static <T> int compareNullableObjects(final Comparable<T> obj1, final T obj2) {
        if (obj1==null) {
            return obj2==null ? 0 : -1;
        } else {
            return obj2==null ? 1 : obj1.compareTo(obj2);
        }
    }

    /**
     * Сравнивает два объекта, реализующих интерфейс Comparable, каждый из которых может быть <code>null</code>.
     * @param obj1  первый объект для сравнения.
     * @param obj2  второй объект для сравнения.
     * @param nullsFirst <code>true</code> - соответствует режиму сортировки при котором значения <code>null</code> идут в начале списка.
     *      <code>false</code> - соответствует режиму при котором значения <code>null</code> идут в конце списка.
     * @return
     *  <ul>
     *   <li> <code>-1</code>  если первый объект меньше второго или только первый объект равен <code>null</code>.
     *   <li> <code>0</code>  если оба объекта одинаковы или оба равны <code>null</code>.
     *   <li> <code>1</code>  если первый объект больше второго или только второй объект равен <code>null</code>.
     *  </ul>
     */
    public static <T> int compareNullableObjects(final Comparable<T> obj1, final T obj2, final boolean nullsFirst) {
        final int factor = nullsFirst ? 1 : -1;
        if (obj1==null) {
            return obj2==null ? 0 : -1 * factor;
        } else {
            return obj2==null ? factor : obj1.compareTo(obj2);
        }
    }

    /**
     * Формирует строковое представление переданного в аргументе массива байт, где каждый байт представлен в шестнадцатиричном формате,
     * и сохраняет это представление в выходной символьный поток.
     * Метод используется как правило в отладочных целях. 
     * @param out  поток куда будет помещено отформатированное представление данных.
     * @param data  данные, чье форматированное в 16-ном формате представление надо вывести в поток.
     * @throws IOException  в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static void dump(final Appendable out, final byte[] data) throws IOException {
        if (data==null || data.length==0)
            return;
        final StringBuilder buf = new StringBuilder(196);
        for (int i=0; i<data.length; i++) {
            if (i % 32==0) {
                if (i>0) {
                    appendTextExplanation(buf, new String(data, i-32, 32) );
                    out.append( buf );
                    buf.setLength(0);
                }
                buf.append(StringUtil.leadLeft(Integer.toHexString(i).toUpperCase()+": ", '0', 7));
            } else {
                if (i%4==0)
                    buf.append("| ");
            }
            final byte b = data[i];
            buf.append( HEXDIGITS[(0xF0 & b) >>> 4] );
            buf.append( HEXDIGITS[0x0F & b] );
            buf.append(' ');
        }
        int rest = data.length%32;
        if (rest==0)
            rest = 32;
        appendTextExplanation(buf, new String(data, data.length-rest, rest) );
        out.append( buf );
        if (out instanceof Flushable) {
            ((Flushable)out).flush();
        }
    }
    private static void appendTextExplanation(final StringBuilder out, final String text) {
        for (int i=118-out.length(); i>0; i--) {
            out.append(' ');
        }
        for (int i=0,length=text.length(); i<length; i++) {
            final char c = text.charAt(i);
            out.append( c<32 || c>65280 ? (char)0 : c );
        }
        out.append('\n');
    }
    private static final char[] HEXDIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    @SuppressWarnings("unchecked")
    public static Iterator makeIterator(final Object obj) {
        if (obj==null) {
            return Collections.EMPTY_SET.iterator();
        } else
        if (obj instanceof Iterator) {
            return (Iterator)obj;
        } else
        if (obj instanceof Iterable) {
            return ((Iterable)obj).iterator();
        } else
        if (obj instanceof Map) {
            return ((Map)obj).entrySet().iterator();
        } else
        if (obj instanceof Enumeration) {
            return new EnumerationIterator<Object>( (Enumeration<Object>)obj );
        } else
        if (obj instanceof Object[]) {
            return new ObjectArrayIterator<Object>( (Object[])obj );
        } else
        if ( (obj instanceof boolean[]) || (obj instanceof byte[]) ||
             (obj instanceof char[]) || (obj instanceof short[]) ||
             (obj instanceof int[]) || (obj instanceof long[]) ||
             (obj instanceof float[]) || (obj instanceof double[]) ) {
            return new ArrayIterator( obj );
        } else
        if (obj instanceof String) {
            return new EnumerationIterator<Object>( new StringTokenizer((String)obj, ",") );
        } else
            return Collections.singleton(obj).iterator();
    }


    @SuppressWarnings("unchecked")
    public static <T,E extends T> E makeInstance(Class<E> clazz, Class<T> ancestorClass) throws ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (ancestorClass!=null) {
        if (!ancestorClass.isAssignableFrom(clazz))
            throw new IllegalArgumentException(ancestorClass.getName()+" is not assignable from "+clazz.getName());
        }
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException e) {
            final Method method = clazz.getMethod("getInstance");
            return (E)method.invoke(null);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T,E extends T> E makeInstance(String className, Class<T> ancestorClass) throws ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Class<E> clazz = loadClass(className);
        if (ancestorClass!=null) {
        if (!ancestorClass.isAssignableFrom(clazz))
            throw new IllegalArgumentException(ancestorClass.getName()+" is not assignable from "+className);
        }
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException e) {
            final Method method = clazz.getMethod("getInstance");
            return (E)method.invoke(null);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(final String name) throws ClassNotFoundException {
        try {
            return (Class<T>)ClassLoader.getSystemClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            try {
                return (Class<T>)Class.forName(name);
            } catch (ClassNotFoundException ex) {
                return (Class<T>)Thread.currentThread().getContextClassLoader().loadClass(name);
            }
        }
    }


}
