package org.echosoft.common.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

/**
 * Реализация интерфейса {@link JsonWriter} особенностями которой являются:
 * <ul>
 *  <li> проверка на корректность последовательностей вызовов методов интерфейса.
 *  <li> форматирование данных в компактной форме.
 * </ul>
 * <b>Внимание!</b> Экземпляры данного класса не являются потокобезопасными и должны использоваться только из одного потока в каждую единицу времени.
 *
 * @author Anton Sharapov
 */
public class CompactJsonWriter implements JsonWriter {

    private static enum State{ARRAY, OBJECT, OBJPARAM}
    private static final class WriterContext {
        public State state;
        public int items;
        public boolean inWriteObj;

        public WriterContext(final State state) {
            this.state = state;
        }
        public String toString() {
            return "[WriterContext{state:"+state+", items:"+items+", inWriteObj:"+inWriteObj+"}]";
        }
    }

    private final JsonContext ctx;
    private final Writer out;
    private final JsonFieldNameSerializer fieldNameSerializer;
    private final LinkedList<WriterContext> stack;
    private WriterContext current;

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
        this.stack = new LinkedList<WriterContext>();
        current = null;
    }


    /**
     * {@inheritDoc}
     */
    public void beginArray() throws IOException {
        if (current != null) {
            if (current.state == State.OBJECT)
                throw new IllegalStateException();
            if (current.state == State.ARRAY /*&& !current.inWriteObj*/ && current.items++>0)      //TODO: вернуть обратно inWriteObj...
                out.write(',');
        }
        current = new WriterContext(State.ARRAY);
        stack.push( current );
        out.write('[');
    }

    /**
     * {@inheritDoc}
     */
    public void endArray() throws IOException {
        if (current.state != State.ARRAY)
            throw new IllegalStateException();
        stack.pop();
        if (stack.size()>0) {
            current = stack.getFirst();
            if (current.state == State.OBJPARAM)
                current.state = State.OBJECT;
        } else {
            current = null;
        }
        out.write(']');
    }

    /**
     * {@inheritDoc}
     */
    public void writeObject(final Object obj) throws IOException, InvocationTargetException, IllegalAccessException {
        if (current != null) {
            if (current.state == State.OBJECT)
                throw new IllegalStateException();
            if (current.state == State.ARRAY && current.items++>0) {
                out.write(',');
            }
            if (obj == null) {
                out.write("null");
            } else {
                current.inWriteObj = true;
                ctx.getSerializer(obj.getClass()).serialize(obj, this);
                current.inWriteObj = false;
            }
        } else {
            if (obj == null) {
                out.write("null");
            } else {
                ctx.getSerializer(obj.getClass()).serialize(obj, this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void beginObject() throws IOException {
        if (current != null) {
            if (current.state == State.OBJECT)
                throw new IllegalStateException();
            if (current.state == State.ARRAY /*&& !current.inWriteObj*/ && current.items++>0)          //TODO: вернуть обратно inWriteObj...
                out.write(',');
        }
        current = new WriterContext(State.OBJECT);
        stack.push( current );
        out.write('{');
    }

    /**
     * {@inheritDoc}
     */
    public void endObject() throws IOException {
        if (current.state != State.OBJECT)
            throw new IllegalStateException();
        stack.pop();
        if (stack.size() > 0) {
            current = stack.getFirst();
            if (current.state == State.OBJPARAM)
                current.state = State.OBJECT;
        } else {
            current = null;
        }
        out.write('}');
    }

    /**
     * {@inheritDoc}
     */
    public void writeProperty(final String name, final Object value) throws IOException, InvocationTargetException, IllegalAccessException {
        if (current.state != State.OBJECT)
            throw new IllegalStateException();
        if (current.items++ > 0) {
            out.write(',');
        }
        fieldNameSerializer.serialize(name, out);
        out.write(':');
        if (value == null) {
            out.write("null");
        } else {
            current.state = State.OBJPARAM;
            ctx.getSerializer(value.getClass()).serialize(value, this);
            current.state = State.OBJECT;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writeComplexProperty(final String name) throws IOException {
        if (current.state != State.OBJECT)
            throw new IllegalStateException();
        if (current.items++ > 0) {
            out.write(',');
        }
        fieldNameSerializer.serialize(name, out);
        out.write(':');
        current.state = State.OBJPARAM;
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
