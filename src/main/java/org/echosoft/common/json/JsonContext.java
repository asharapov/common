package org.echosoft.common.json;

import java.io.Writer;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.echosoft.common.json.annotate.JsonUseSeriazer;
import org.echosoft.common.json.introspect.BeanSerializer;

/**
 * <p>Ключевой класс модуля отвечающего за конвертацию java объектов в JSON формат.</p>
 * <p>Содержит информацию о правилах по которым объекты тех или иных java классов должны преобразовываться в JSON формат.<br/>
 * Как правило достаточно создать (и, при необходимости, настроить) один экземпляр этого класса в процессе инициализации приложения
 * и использовать в течение всего времени работы программы.</p>
 *
 * @author Anton Sharapov
 */
public class JsonContext {

    private final Map<Class<?>, JsonSerializer> serializers;
    private final HashMap<Class<?>, JsonSerializer> cserializers;
    private final HashMap<Class<?>, JsonSerializer> iserializers;
    private JsonFieldNameSerializer fieldNameSerializer;
    private JsonWriterFactory writerFactory;

    public JsonContext() {
        this(null);
    }

    public JsonContext(final JsonContext original) {
        serializers = new ConcurrentHashMap<Class<?>, JsonSerializer>(64);
        cserializers = new HashMap<Class<?>, JsonSerializer>();
        iserializers = new HashMap<Class<?>, JsonSerializer>();
        if (original == null) {
            registerSerializer(String.class, Serializers.STRING, false);
            registerSerializer(Character.class, Serializers.CHAR, false);
            registerSerializer(Boolean.class, Serializers.BOOLEAN, false);
            registerSerializer(Number.class, Serializers.NUMBER, true);
            registerSerializer(BigDecimal.class, Serializers.BIGDECIMAL, true);
            registerSerializer(Enum.class, Serializers.ENUM, true);
            registerSerializer(Date.class, Serializers.DATE_ISO_FMT, true);
            registerSerializer(char[].class, Serializers.CHAR_ARRAY, false);
            registerSerializer(boolean[].class, Serializers.BOOLEAN_ARRAY, false);
            registerSerializer(byte[].class, Serializers.BYTE_ARRAY, false);
            registerSerializer(short[].class, Serializers.SHORT_ARRAY, false);
            registerSerializer(int[].class, Serializers.INT_ARRAY, false);
            registerSerializer(long[].class, Serializers.LONG_ARRAY, false);
            registerSerializer(float[].class, Serializers.FLOAT_ARRAY, false);
            registerSerializer(double[].class, Serializers.DOUBLE_ARRAY, false);
            registerSerializer(Object[].class, Serializers.OBJECT_ARRAY, true);
            registerSerializer(String[].class, Serializers.STRING_ARRAY, false);
            registerSerializer(Character[].class, Serializers.CHARS_ARRAY, false);
            registerSerializer(Boolean[].class, Serializers.BOOLEANS_ARRAY, false);
            registerSerializer(Byte[].class, Serializers.BYTES_ARRAY, false);
            registerSerializer(Short[].class, Serializers.SHORTS_ARRAY, false);
            registerSerializer(Integer[].class, Serializers.INTEGERS_ARRAY, false);
            registerSerializer(Long[].class, Serializers.LONGS_ARRAY, false);
            registerSerializer(Float[].class, Serializers.FLOATS_ARRAY, false);
            registerSerializer(Double[].class, Serializers.DOUBLES_ARRAY, false);
            // интерфейсы ...
            registerSerializer(Iterable.class, Serializers.ITERABLE, true);
            registerSerializer(Iterator.class, Serializers.ITERATOR, true);
            registerSerializer(Enumeration.class, Serializers.ENUMERATION, true);
            registerSerializer(Map.class, Serializers.MAP, true);
            registerSerializer(CharSequence.class, Serializers.CHAR_SEQUENCE, true);
            registerSerializer(CharSequence[].class, Serializers.CHAR_SEQUENCE_ARRAY, true);
            registerSerializer(JSExpression.class, Serializers.JSEXPRESSION, true);
            fieldNameSerializer = Serializers.STANDARD_FIELD_NAME_SERIALIZER;
            writerFactory = Serializers.COMPACT_JSON_WRITER_FACTORY;
        } else {
            serializers.putAll(original.serializers);
            cserializers.putAll(original.cserializers);
            iserializers.putAll(original.iserializers);
            fieldNameSerializer = original.fieldNameSerializer;
            writerFactory = original.writerFactory;
        }
    }

    /**
     * Рекомендуемый способ для создания в приложении новых экземпляров {@link JsonWriter}.
     *
     * @param out выходной поток куда должен помещаться результат работы созданного экземпляра {@link JsonWriter}. Не может быть <code>null</code>.
     * @return созданный экземпляр {@link JsonWriter}.
     */
    public JsonWriter makeJsonWriter(final Writer out) {
        return writerFactory.makeJsonWriter(this, out);
    }

    public JsonWriterFactory getWriterFactory() {
        return writerFactory;
    }
    public void setWriterFactory(final JsonWriterFactory factory) {
        if (factory == null)
            throw new IllegalArgumentException("Factory should be specified");
        this.writerFactory = factory;
    }

    /**
     * Определяет алгоритм сериализации имен полей javascript объектов.<br/>
     * Есть два основных алгоритма:
     * <ol>
     * <li> стандартный - ВСЕ имена полей оборачиваются в кавычки.
     * <li> по умолчанию - в кавычки оборачиваются только те имена которые соответствуют зарезервированным словам в javascript.
     * </ol>
     *
     * @return используемый алгоритм сериализации имен полей javascript объектов.
     */
    public JsonFieldNameSerializer getFieldNameSerializer() {
        return fieldNameSerializer;
    }
    public void setFieldNameSerializer(final JsonFieldNameSerializer serializer) {
        if (serializer == null)
            throw new IllegalArgumentException("Serializer should be specified");
        this.fieldNameSerializer = serializer;
    }

    /**
     * Регистрирует сериализер для определенного класса (иерархии классов) или интерфейса.
     *
     * @param cls        класс для которого регистрируется заданный сериализер.
     * @param recursive  если <code>true</code> то данный сериализер будет автоматически применяться и для всех классов унаследованных от указанного в аргументе.
     * @param serializer сериализер ассоциируемый c указанным классом (иерархией классов).
     */
    public void registerSerializer(final Class<?> cls, final JsonSerializer serializer, final boolean recursive) {
        if (cls == null || serializer == null)
            throw new IllegalArgumentException("All arguments should be specified");
        if (cls.isInterface()) {
            iserializers.put(cls, serializer);
        } else {
            serializers.put(cls, serializer);
            if (recursive) {
                cserializers.put(cls, serializer);
            }
        }
    }

    public int removeSerializer(final Class<?> cls, final boolean recursive) {
        if (cls == null)
            throw new IllegalArgumentException("Class must be specified");

        int removed = 0;
        if (recursive) {
            if (cls.isInterface()) {
                for (Class<?> c : iserializers.keySet()) {
                    if (cls.isAssignableFrom(c)) {
                        iserializers.remove(c);
                        removed++;
                    }
                }
            } else {
                for (Class<?> c : cserializers.keySet()) {
                    if (cls.isAssignableFrom(c)) {
                        cserializers.remove(c);
                        removed++;
                    }
                }
                for (Class<?> c : serializers.keySet()) {
                    if (cls.isAssignableFrom(c)) {
                        serializers.remove(c);
                        removed++;
                    }
                }
            }
        } else {
            if (iserializers.remove(cls) != null)
                removed++;
            if (cserializers.remove(cls) != null)
                removed++;
            if (serializers.remove(cls) != null)
                removed++;
        }
        return removed;
    }

    /**
     * Возвращает подходящий сериализер для указанного в аргументе класса.
     *
     * @param cls класс для которого должен быть найден соответствующий сериализатор в JSON формат.
     * @return наиболее подходящий сериализатор или <code>null</code>.
     */
    public <T> JsonSerializer<T> getSerializer(final Class<? extends T> cls) {
        JsonSerializer result = serializers.get(cls);
        if (result == null) {
            result = resolveSerializer(cls);
            serializers.put(cls, result);
        }
        return result;
    }

    /**
     * Осуществляет поиск подходящего сериализера для указанного класса. Выполняется один раз для каждого класса чьи объекты участвуют в сериализации в JSON.
     *
     * @param cls класс для которого требуется подобрать сериализер.
     * @return соответствующий сериализер. Метод никогда не возвращает <code>null</code>.
     */
    private JsonSerializer resolveSerializer(final Class<?> cls) {
        if (cls.isArray()) {
            // если это массив объектов какого-либо класса ...
            return Serializers.OBJECT_ARRAY;
        }
        // ищем прямые указания как сериализовать данный класс или один из его предков (только с пометкой что это правило применимо к классам-потомкам)ю
        for (Class<?> c = cls; c != null && c.getSuperclass() != null; c = c.getSuperclass()) {
            JsonSerializer result = cserializers.get(c);
            if (result != null)
                return result;
            final JsonUseSeriazer an = JsonUtil.getDeclaredAnnotation(c, JsonUseSeriazer.class);
            if (an != null && (an.recursive() || c == cls)) {
                // либо эта аннотация пришпилена непосредственно к требуемому классу
                // либо к одному из его предков с пометкой что она действительна для всех его потомков.
                return JsonUtil.makeInstance(an.value());
            }
        }
        // А может требуемый класс реализует какие-либо знакомые нам интерфейсы ?
        for (Map.Entry<Class<?>, JsonSerializer> entry : iserializers.entrySet()) {
            if (entry.getKey().isAssignableFrom(cls)) {
                return entry.getValue();
            }
        }
        // Если никакие иные рецепты не помогли то остается трактовать данный класс как просто очередной java bean.
        return new BeanSerializer(cls);
    }
}
