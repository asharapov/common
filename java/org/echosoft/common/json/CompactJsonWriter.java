package org.echosoft.common.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

/**
 * Реализация интерфейса {@link JsonWriter} особенностями которой являются:
 * <ul>
 *  <li> проверка на корректность последовательностей вызовов методов интерфейса.
 *  <li> форматирование данных в компактной форме.
 * </ul>
 * <strong>Внимание!</strong> Экземпляры данного класса не являются потокобезопасными и должны использоваться только из одного потока в каждую единицу времени.
 *
 * @author Anton Sharapov
 */
public class CompactJsonWriter implements JsonWriter {

    private static enum State{UNKNOWN, ARRAY, OBJECT, OBJATTR}

    private static final class Context {
        private final Context prev;
        private State state;
        private int items;
        private boolean inWriteObj;
        private Context() {
            this.prev = this;
            this.state = State.UNKNOWN;
        }
        private Context(final Context prev, final State state) {
            this.prev = prev;
            this.state = state;
        }
        public String toString() {
            return "[Context{state:"+state+", items:"+items+", inWriteObj:"+inWriteObj+"}]";
        }
    }

    private final JsonContext ctx;
    private final Writer out;
    private final JsonFieldNameSerializer fieldNameSerializer;
    private Context current;

    /**
     * Инициализирует поток данных в нотации JSON.
     * @param ctx  глобальный контекст. Обязательное поле, не может быть <code>null</code>.
     * @param out  выходной поток куда будет помещаться результат. Не может быть <code>null</code>.
     */
    public CompactJsonWriter(final JsonContext ctx, final Writer out) {
        if (ctx==null || out==null)
            throw new IllegalArgumentException("All arguments should be specified");
        this.ctx = ctx;
        this.out = out;
        this.fieldNameSerializer = ctx.getFieldNameSerializer();
        this.current = new Context();
    }


    /**
     * {@inheritDoc}
     */
    public void beginArray() throws IOException {
        switch (current.state) {
            case UNKNOWN : {
                if (!current.inWriteObj && current.items > 0)
                    throw new IllegalStateException();
                current.items = 1;
                current = new Context(current, State.ARRAY);
                out.write('[');
                break;
            }
            case ARRAY : {
                if (!current.inWriteObj && current.items++>0)
                    out.write(',');
                current = new Context(current, State.ARRAY);
                out.write('[');
                break;
            }
            case OBJECT : {
                throw new IllegalStateException();
            }
            case OBJATTR : {
                current.state = State.OBJECT;
                current = new Context(current, State.ARRAY);
                out.write('[');
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endArray() throws IOException {
        switch (current.state) {
            case UNKNOWN :
            case OBJECT :
            case OBJATTR : {
                throw new IllegalStateException();
            }
            case ARRAY : {
                current = current.prev;
                out.write(']');
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void beginObject() throws IOException {
        switch (current.state) {
            case UNKNOWN : {
                if (!current.inWriteObj && current.items > 0)
                    throw new IllegalStateException();
                current.items = 1;
                current = new Context(current, State.OBJECT);
                out.write('{');
                break;
            }
            case ARRAY : {
                if (!current.inWriteObj && current.items++>0)
                    out.write(',');
                current = new Context(current, State.OBJECT);
                out.write('{');
                break;
            }
            case OBJECT : {
                throw new IllegalStateException();
            }
            case OBJATTR : {
                current.state = State.OBJECT;
                current = new Context(current, State.OBJECT);
                out.write('{');
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endObject() throws IOException {
        switch (current.state) {
            case UNKNOWN :
            case ARRAY :
            case OBJATTR : {
                throw new IllegalStateException();
            }
            case OBJECT : {
                current = current.prev;
                out.write('}');
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writeObject(final Object obj) throws IOException, InvocationTargetException, IllegalAccessException {
        switch (current.state) {
            case UNKNOWN : {
                if (current.items > 0)
                    throw new IllegalStateException();
                current.items = 1;
                if (obj==null) {
                    out.write("null");
                } else {
                    current.inWriteObj = true;
                    ctx.getSerializer(obj.getClass()).serialize(obj, this);
                    current.inWriteObj = false;
                }
                break;
            }
            case ARRAY : {
                if (current.items++ > 0)
                    out.write(',');
                if (obj == null) {
                    out.write("null");
                } else {
                    current.inWriteObj = true;
                    ctx.getSerializer(obj.getClass()).serialize(obj, this);
                    current.inWriteObj = false;
                }
                break;
            }
            case OBJECT : {
                throw new IllegalStateException();
            }
            case OBJATTR : {
                current.state = State.OBJECT;
                if (obj == null) {
                    out.write("null");
                } else {
                    current.inWriteObj = true;
                    ctx.getSerializer(obj.getClass()).serialize(obj, this);
                    current.inWriteObj = false;
                }
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writeProperty(final String name, final Object value) throws IOException, InvocationTargetException, IllegalAccessException {
        switch (current.state) {
            case UNKNOWN :
            case ARRAY :
            case OBJATTR : {
                throw new IllegalStateException();
            }
            case OBJECT: {
                if (current.items++ > 0) {
                    out.write(',');
                }
                fieldNameSerializer.serialize(name, out);
                out.write(':');
                if (value == null) {
                    out.write("null");
                } else {
                    current.state = State.OBJATTR;
                    ctx.getSerializer(value.getClass()).serialize(value, this);
                    current.state = State.OBJECT;
                }
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writeComplexProperty(final String name) throws IOException {
        switch (current.state) {
            case UNKNOWN :
            case ARRAY :
            case OBJATTR : {
                throw new IllegalStateException();
            }
            case OBJECT : {
                if (current.items++ > 0) {
                    out.write(',');
                }
                fieldNameSerializer.serialize(name, out);
                out.write(':');
                current.state = State.OBJATTR;
                break;
            }
        }
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
