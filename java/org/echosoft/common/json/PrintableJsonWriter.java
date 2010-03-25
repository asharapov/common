package org.echosoft.common.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Реализация интерфейса {@link JsonWriter} особенностями которой являются:
 * <ul>
 *  <li> проверка на корректность последовательностей вызовов методов интерфейса.
 *  <li> форматирование данных в виде наиболее подходящем для визуального просмотра результат (может использоваться для отладки).
 * </ul> 
 * <b>Внимание!</b> Экземпляры данного класса не являются потокобезопасными и должны использоваться только из одного потока в каждую единицу времени.
 *
 * @author Anton Sharapov
 */
public class PrintableJsonWriter implements JsonWriter {

    public static final int DEFAULT_INDENT_FACTOR = 4;  // сколько пробелов соответствуют одному уровню иерархии.

    private static enum State{ARRAY, OBJECT, OBJPARAM}

    private static final class WriterContext {
        public final String prefix;
        public State state;
        public int items;
        public boolean inWriteObj;
        public WriterContext(final State state, final String prefix) {
            this.state = state;
            this.prefix = prefix;
        }
        public String toString() {
            return "[WriterContext{state:"+state+", items:"+items+", inWriteObj:"+inWriteObj+"}]";
        }
    }

    private final JsonContext ctx;
    private final Writer out;
    private final JsonFieldNameSerializer fieldNameSerializer;
    private final int indentFactor;
    private final ArrayList<String> indents;
    private final LinkedList<WriterContext> stack;
    private WriterContext current;

    /**
     * Инициализирует поток данных в нотации JSON.
     * @param ctx  глобальный контекст. Обязательное поле, не может быть <code>null</code>.
     * @param out  выходной поток куда будет помещаться результат. Не может быть <code>null</code>.
     */
    public PrintableJsonWriter(final JsonContext ctx, final Writer out) {
        if (ctx==null || out==null)
            throw new IllegalArgumentException("All arguments should be specified");
        this.ctx = ctx;
        this.out = out;
        this.fieldNameSerializer = ctx.getFieldNameSerializer();
        this.stack = new LinkedList<WriterContext>();
        this.indentFactor = DEFAULT_INDENT_FACTOR;  //TODO: добавить возможность кастомизации данного параметра.
        this.indents = JsonUtil.getIndentationStrings(indentFactor);
        current = null;
    }


    /**
     * {@inheritDoc}
     */
    public void beginArray() throws IOException {
        if (current != null) {
            if (current.state == State.OBJECT )
                throw new IllegalStateException();
            if (current.state == State.ARRAY) {
                if (!current.inWriteObj && current.items++>0)
                    out.write(",\n");
                out.write(current.prefix);
            }
        }
        current = new WriterContext(State.ARRAY, getIndentString(stack.size()));
        stack.push( current );
        out.write("[\n");
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
        out.write('\n');
        if (current != null) {
            out.write(current.prefix);
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
                out.write(",\n");
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
            if (current.state == State.ARRAY) {
                if (!current.inWriteObj && current.items++>0)
                    out.write(",\n");
                out.write(current.prefix);
            }
        }
        current = new WriterContext(State.OBJECT, getIndentString(stack.size()));
        stack.push( current );
        out.write("{\n");
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
        out.write('\n');
        if (current != null) {
            out.write(current.prefix);
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
            out.write(",\n");
        }
        out.write(current.prefix);
        fieldNameSerializer.serialize(name, out);
        out.write(": ");
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
            out.write(",\n");
        }
        out.write(current.prefix);
        fieldNameSerializer.serialize(name, out);
        out.write(": ");
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


    /**
     * Для заданного уровня вложенности возвращает строку из пробелов той длины что требуется для отрисовки надлежащего кол-ва отступов слева.
     * @param level уровень вложенности.
     * @return  строка из пробелов.
     */
    private String getIndentString(int level) {
        final int size = indents.size();
        level++;
        if (level < size) {
            return indents.get(level);
        } else {
            final char[] buf = new char[size*indentFactor];
            Arrays.fill(buf, ' ');
            final String result = new String(buf);
            indents.add( result );
            return result;
        }
    }

}
