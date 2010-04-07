package org.echosoft.common.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

/**
 * Простейшая реализация интерфейса {@link JsonWriter}, ориентированная на достижение максимальной производительности за счет удаления
 * большинства проверок на корректность вызовов методов этого класса.
 * <b>Внимание!</b> Экземпляры данного класса не являются потокобезопасными и должны использоваться только из одного потока в каждую единицу времени.
 *
 * @author Anton Sharapov
 */
public class SimpleJsonWriter implements JsonWriter {

    private static final char[] NUL = {'n','u','l','l'};

    private final JsonContext ctx;
    private final Writer out;
    private final JsonFieldNameSerializer fieldNameSerializer;
    private boolean hasPrevFields;
    private int depth;

    /**
     * Инициализирует поток данных в нотации JSON.
     * @param ctx  глобальный контекст. Обязательное поле, не может быть <code>null</code>.
     * @param out  выходной поток куда будет помещаться результат. Не может быть <code>null</code>.
     */
    public SimpleJsonWriter(final JsonContext ctx, final Writer out) {
        if (ctx==null || out==null)
            throw new IllegalArgumentException("All arguments should be specified");
        this.ctx = ctx;
        this.out = out;
        this.fieldNameSerializer = ctx.getFieldNameSerializer();
        hasPrevFields = false;
        depth = 0;
    }


    /**
     * {@inheritDoc}
     */
    public void beginArray() throws IOException {
        if (depth++ > 0 && hasPrevFields) {
            out.write(',');
        }
        hasPrevFields = false;
        out.write('[');
    }

    /**
     * {@inheritDoc}
     */
    public void endArray() throws IOException {
        if (depth-- <= 0)
            throw new IllegalStateException();
        hasPrevFields = true;
        out.write(']');
    }

    /**
     * {@inheritDoc}
     */
    public void beginObject() throws IOException {
        if (depth++ > 0 && hasPrevFields) {
            out.write(',');
        }
        hasPrevFields = false;
        out.write('{');
    }

    /**
     * {@inheritDoc}
     */
    public void endObject() throws IOException {
        if (depth-- <= 0)
            throw new IllegalStateException();
        hasPrevFields = true;
        out.write('}');
    }

    /**
     * {@inheritDoc}
     */
    public void writeObject(final Object obj) throws IOException, InvocationTargetException, IllegalAccessException {
        if (depth>0 && hasPrevFields) {
            out.write(',');
        }
        if (obj==null) {
            out.write(NUL,0,4);     // out.write("null");
        } else {
            hasPrevFields = false;
            ctx.getSerializer(obj.getClass()).serialize(obj, this);
        }
        hasPrevFields = true;
    }

    /**
     * {@inheritDoc}
     */
    public void writeProperty(final String name, final Object value) throws IOException, InvocationTargetException, IllegalAccessException {
        if (depth<=0)
            throw new IllegalStateException();
        if (hasPrevFields) {
            out.write(',');
        }
        fieldNameSerializer.serialize(name, out);
        out.write(':');
        if (value==null) {
            out.write(NUL,0,4);     // out.write("null");
        } else {
            hasPrevFields = false;
            ctx.getSerializer(value.getClass()).serialize(value, this);
        }
        hasPrevFields = true;
    }

    /**
     * {@inheritDoc}
     */
    public void writeComplexProperty(final String name) throws IOException {
        if (depth<=0)
            throw new IllegalStateException();
        if (hasPrevFields) {
            out.write(',');
        }
        fieldNameSerializer.serialize(name, out);
        out.write(':');
        hasPrevFields = false;
    }


    /**
     * {@inheritDoc}
     */
    public JsonContext getContext() {
        return ctx;
    }

    /**
     * {@inheritDoc}
     */
    public Writer getOutputWriter() {
        return out;
    }

}