package org.echosoft.common.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Содержит ссылки на объекты - реализации интерфейса {@link JsonSerializer} для базового набора классов:
 * <ul>
 * <li> <code>java.lang.Object</code>
 * <li> <code>java.lang.String</code>
 * <li> <code>java.lang.Char</code>
 * <li> <code>java.lang.Enum</code>
 * <li> <code>java.lang.Boolean</code>
 * <li> <code>java.lang.Number</code>
 * <li> <code>java.math.BigDecimal</code>
 * <li> <code>java.util.Date</code>
 * <li> <code>char[]</code>
 * <li> <code>boolean[]</code>
 * <li> <code>byte[]</code>
 * <li> <code>short[]</code>
 * <li> <code>int[]</code>
 * <li> <code>long[]</code>
 * <li> <code>float[]</code>
 * <li> <code>double[]</code>
 * <li> <code>java.lang.Object[]</code>
 * <li> <code>java.lang.String[]</code>
 * <li> <code>java.lang.Integer[]</code>
 * <li> <code>ru.topsbi.common.util.json.JSExpression</code>
 * </ul>
 * @author Anton Sharapov
 */
public class Serializers {

    public static final JsonWriterFactory COMPACT_JSON_WRITER_FACTORY =
            new JsonWriterFactory() {
                public JsonWriter makeJsonWriter(final JsonContext ctx, final Writer out) {
                    return new CompactJsonWriter(ctx, out);
                }
            };

    public static final JsonWriterFactory PRINTABLE_JSON_WRITER_FACTORY =
            new JsonWriterFactory() {
                public JsonWriter makeJsonWriter(final JsonContext ctx, final Writer out) {
                    return  new PrintableJsonWriter(ctx, out);
                }
            };


    public static final JsonFieldNameSerializer COMPACT_FIELDNAME_SERIALIZER =
            new JsonFieldNameSerializer() {
                private final HashMap<String,String> keywords = new HashMap<String,String>();
                {
                    keywords.put(null, "");
                    for (String key : JsonUtil.JS_KEYWORDS) {
                        keywords.put(key, "\""+key+"\"");
                    }
                }
                public void serialize(final String fieldname, final Writer out) throws IOException {
                    final String keyword = keywords.get(fieldname);
                    out.write( keyword==null ? fieldname : keyword );
                }
            };
    public static final JsonFieldNameSerializer STANDARD_FIELD_NAME_SERIALIZER =
            new JsonFieldNameSerializer() {
                private final char[] quotes = {'\"','\"'};
                public void serialize(final String fieldname, final Writer out) throws IOException {
                    if (fieldname==null || fieldname.length()==0) {
                        out.write(quotes,0,2);
                    } else
                    if (fieldname.charAt(0)=='\"') {
                        out.write(fieldname);
                    } else {
                        out.write('\"');
                        out.write(fieldname);
                        out.write('\"');
                    }
                }
            };




    public static final JsonSerializer<CharSequence> STRING =
            new JsonSerializer<CharSequence>() {
                public void serialize(final CharSequence src, final JsonWriter jw) throws IOException {
                    JsonUtil.encodeString(src, jw.getOutputWriter());
                }
            };

    public static final JsonSerializer<Character> CHAR =
            new JsonSerializer<Character>() {
                public void serialize(final Character src, final JsonWriter jw) throws IOException {
                    JsonUtil.encodeChar(src, jw.getOutputWriter());
                }
            };

    public static final JsonSerializer<Boolean> BOOLEAN =
            new JsonSerializer<Boolean>() {
                public void serialize(final Boolean src, final JsonWriter jw) throws IOException {
                    if (src) {
                        jw.getOutputWriter().write(JsonUtil.TRUE,0,4);
                    } else {
                        jw.getOutputWriter().write(JsonUtil.FALSE,0,5);
                    }
                }
            };

    public static final JsonSerializer<Number> NUMBER =
            new JsonSerializer<Number>() {
                public void serialize(final Number src, final JsonWriter jw) throws IOException {
                    jw.getOutputWriter().write( src.toString() );
                }
            };

    public static final JsonSerializer<BigDecimal> BIGDECIMAL =
            new JsonSerializer<BigDecimal>() {
                public void serialize(final BigDecimal src, final JsonWriter jw) throws IOException {
                    jw.getOutputWriter().write( src.toString() );
                }
            };

    public static final JsonSerializer<Enum> ENUM =
            new JsonSerializer<Enum>() {
                public void serialize(final Enum src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    out.write('"');
                    out.write( src.name() );
                    out.write('"');
                }
            };

    public static final JsonSerializer<Date> DATE =
            new JsonSerializer<Date>() {
                public void serialize(final Date src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final Calendar cal = getCalendarInstanceForThread();
                    cal.setTime( src );
                    out.write("new Date(");
                    out.write( Integer.toString(cal.get(Calendar.YEAR)) );
                    out.write(',');
                    out.write( Integer.toString(cal.get(Calendar.MONTH)) );
                    out.write(',');
                    out.write( Integer.toString(cal.get(Calendar.DATE)) );
                    out.write(',');
                    out.write( Integer.toString(cal.get(Calendar.HOUR_OF_DAY)) );
                    out.write(',');
                    out.write( Integer.toString(cal.get(Calendar.MINUTE)) );
                    out.write(',');
                    out.write( Integer.toString(cal.get(Calendar.SECOND)) );
                    out.write(')');
                }
            };

    public static final JsonSerializer<Date> TIMESTAMP =
            new JsonSerializer<Date>() {
                public void serialize(final Date src, final JsonWriter jw) throws IOException {
                    jw.getOutputWriter().write( Long.toString(src.getTime(),10) );
                }
            };



    public static final JsonSerializer<char[]> CHAR_ARRAY =
            new JsonSerializer<char[]>() {
                public void serialize(final char[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        JsonUtil.encodeChar(src[i], out);
                    }
                    out.write(']');
                }
            };

    public static final JsonSerializer<boolean[]> BOOLEAN_ARRAY =
            new JsonSerializer<boolean[]>() {
                public void serialize(final boolean[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        if (src[i]) {
                            out.write(JsonUtil.TRUE,0,4);
                        } else {
                            out.write(JsonUtil.FALSE,0,5);
                        }
                    }
                    out.write(']');
                }
            };

    public static final JsonSerializer<byte[]> BYTE_ARRAY =
            new JsonSerializer<byte[]>() {
                public void serialize(final byte[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        out.write(Integer.toString(src[i],10));
                    }
                    out.write(']');
                }
            };

    public static final JsonSerializer<short[]> SHORT_ARRAY =
            new JsonSerializer<short[]>() {
                public void serialize(final short[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        out.write(Integer.toString(src[i],10));
                    }
                    out.write(']');
                }
            };

    public static final JsonSerializer<int[]> INT_ARRAY =
            new JsonSerializer<int[]>() {
                public void serialize(final int[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        out.write(Integer.toString(src[i],10));
                    }
                    out.write(']');
                }
            };

    public static final JsonSerializer<long[]> LONG_ARRAY =
            new JsonSerializer<long[]>() {
                public void serialize(final long[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        out.write(Long.toString(src[i]));
                    }
                    out.write(']');
                }
            };

    public static final JsonSerializer<float[]> FLOAT_ARRAY =
            new JsonSerializer<float[]>() {
                public void serialize(final float[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        out.write(Float.toString(src[i]));
                    }
                    out.write(']');
                }
            };

    public static final JsonSerializer<double[]> DOUBLE_ARRAY =
            new JsonSerializer<double[]>() {
                public void serialize(final double[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        out.write(Double.toString(src[i]));
                    }
                    out.write(']');
                }
            };



    public static final JsonSerializer<Object[]> OBJECT_ARRAY =
            new JsonSerializer<Object[]>() {
                public void serialize(final Object[] src, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
                    jw.beginArray();
                    for (Object item : src) {
                        jw.writeObject(item);
                    }
                    jw.endArray();
                }
            };


    // данный класс не является необходимым (можно использовать и {@link OBJECT_ARRAY} но дает некоторый выигрыш в производительности.
    public static final JsonSerializer<CharSequence[]> STRING_ARRAY =
            new JsonSerializer<CharSequence[]>() {
                public void serialize(final CharSequence[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        JsonUtil.encodeString(src[i], out);
                    }
                    out.write(']');
                }
            };

    // данный класс не является необходимым (можно использовать и {@link OBJECT_ARRAY} но дает некоторый выигрыш в производительности.
    public static final JsonSerializer<Character[]> CHARS_ARRAY =
            new JsonSerializer<Character[]>() {
                public void serialize(final Character[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        final Character value = src[i];
                        if (value==null) {
                            out.write(JsonUtil.NULL,0,4);             // out.write("null");
                        } else {
                            JsonUtil.encodeChar(value, out);
                        }
                    }
                    out.write(']');
                }
            };

    // данный класс не является необходимым (можно использовать и {@link OBJECT_ARRAY} но дает некоторый выигрыш в производительности.
    public static final JsonSerializer<Boolean[]> BOOLEANS_ARRAY =
            new JsonSerializer<Boolean[]>() {
                public void serialize(final Boolean[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        final Boolean value = src[i];
                        if (value==null) {
                            out.write(JsonUtil.NULL,0,4);               // out.write("null");
                        } else
                        if (value) {
                            out.write(JsonUtil.TRUE,0,4);               // out.write("true");
                        } else {
                            out.write(JsonUtil.FALSE,0,5);              // out.write("false");
                        }
                    }
                    out.write(']');
                }
            };

    // данный класс не является необходимым (можно использовать и {@link OBJECT_ARRAY} но дает некоторый выигрыш в производительности.
    public static final JsonSerializer<Byte[]> BYTES_ARRAY =
            new JsonSerializer<Byte[]>() {
                public void serialize(final Byte[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        final Byte value = src[i];
                        if (value==null) {
                            out.write(JsonUtil.NULL,0,4);               // out.write("null");
                        } else {
                            out.write(Integer.toString(value,10));
                        }
                    }
                    out.write(']');
                }
            };

    // данный класс не является необходимым (можно использовать и {@link OBJECT_ARRAY} но дает некоторый выигрыш в производительности.
    public static final JsonSerializer<Short[]> SHORTS_ARRAY =
            new JsonSerializer<Short[]>() {
                public void serialize(final Short[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        final Short value = src[i];
                        if (value==null) {
                            out.write(JsonUtil.NULL,0,4);               // out.write("null");
                        } else {
                            out.write(Integer.toString(value,10));
                        }
                    }
                    out.write(']');
                }
            };

    // данный класс не является необходимым (можно использовать и {@link OBJECT_ARRAY} но дает некоторый выигрыш в производительности.
    public static final JsonSerializer<Integer[]> INTEGERS_ARRAY =
            new JsonSerializer<Integer[]>() {
                public void serialize(final Integer[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        final Integer value = src[i];
                        if (value==null) {
                            out.write(JsonUtil.NULL,0,4);               // out.write("null");
                        } else {
                            out.write(Integer.toString(value,10));
                        }
                    }
                    out.write(']');
                }
            };

    // данный класс не является необходимым (можно использовать и {@link OBJECT_ARRAY} но дает некоторый выигрыш в производительности.
    public static final JsonSerializer<Long[]> LONGS_ARRAY =
            new JsonSerializer<Long[]>() {
                public void serialize(final Long[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        final Long value = src[i];
                        if (value==null) {
                            out.write(JsonUtil.NULL,0,4);               // out.write("null");
                        } else {
                            out.write(Long.toString(value));
                        }
                    }
                    out.write(']');
                }
            };

    // данный класс не является необходимым (можно использовать и {@link OBJECT_ARRAY} но дает некоторый выигрыш в производительности.
    public static final JsonSerializer<Float[]> FLOATS_ARRAY =
            new JsonSerializer<Float[]>() {
                public void serialize(final Float[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        final Float value = src[i];
                        if (value==null) {
                            out.write(JsonUtil.NULL,0,4);               // out.write("null");
                        } else {
                            out.write(Float.toString(value));
                        }
                    }
                    out.write(']');
                }
            };

    // данный класс не является необходимым (можно использовать и {@link OBJECT_ARRAY} но дает некоторый выигрыш в производительности.
    public static final JsonSerializer<Double[]> DOUBLES_ARRAY =
            new JsonSerializer<Double[]>() {
                public void serialize(final Double[] src, final JsonWriter jw) throws IOException {
                    final Writer out = jw.getOutputWriter();
                    final int length = src.length;
                    out.write('[');
                    for (int i=0; i<length; i++) {
                        if (i>0)
                            out.write(',');
                        final Double value = src[i];
                        if (value==null) {
                            out.write(JsonUtil.NULL,0,4);               // out.write("null");
                        } else {
                            out.write(Double.toString(value));
                        }
                    }
                    out.write(']');
                }
            };



    public static final JsonSerializer ITERABLE =
            new JsonSerializer() {
                public void serialize(final Object src, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
                    jw.beginArray();
                    for (Object item : (Iterable)src) {
                        jw.writeObject(item);
                    }
                    jw.endArray();
                }
            };

    public static final JsonSerializer ITERATOR =
            new JsonSerializer() {
                public void serialize(final Object src, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
                    final Iterator it = (Iterator)src;
                    jw.beginArray();
                    while (it.hasNext()) {
                        jw.writeObject(it.next());
                    }
                    jw.endArray();
                }
            };

    public static final JsonSerializer ENUMERATION =
            new JsonSerializer() {
                public void serialize(final Object src, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
                    final Enumeration en = (Enumeration)src;
                    jw.beginArray();
                    while (en.hasMoreElements()) {
                        jw.writeObject(en.nextElement());
                    }
                    jw.endArray();
                }
            };

    public static final JsonSerializer MAP =
            new JsonSerializer() {
                public void serialize(final Object src, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
                    jw.beginObject();
                    for (Map.Entry<Object,Object> entry : ((Map<Object,Object>)src).entrySet()) {
                        final String name = "\"" + entry.getKey() + '\"';  // TODO: баг, такое код не совместим со STANDARD_FIELD_NAME_SERIALIZER
                        jw.writeProperty(name, entry.getValue());
                    }
                    jw.endObject();
                }
            };


    public static final JsonSerializer<JSExpression> JSEXPRESSION =
            new JsonSerializer<JSExpression>() {
                public void serialize(final JSExpression src, final JsonWriter jw) throws IOException {
                    jw.getOutputWriter().write( src.getExpression() );
                }
            };


    /**
     * Возвращает некогда ранее созданный экземпляр класса {@link Calendar} с неопределенным на момент вызова этого метода значением.<br/>
     * Единственное (и самое главное!) что гарантирует данный метод это то что возвращаемый объект можно безопасно  использовать в текущем потоке (и только в нем!).<br/>
     * Данный метод используется в целях избежания потерь на избыточном создании новых экземпляров {@link Calendar} так как этот класс не является потокобезопасным.
     * @return экземпляр класса {@link Calendar} с неопределенными значениями своих свойств.
     */
    private  static Calendar getCalendarInstanceForThread() {
        Calendar result = _ctl.get();
        if (result==null) {
            result = Calendar.getInstance();
            _ctl.set(result);
        }
        return result;
    }
    private static final ThreadLocal<Calendar> _ctl = new ThreadLocal<Calendar>();
}
