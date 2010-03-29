package org.echosoft.common.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Реализация интерфейса {@link JsonWriter} особенностями которой являются:
 * <ul>
 *  <li> проверка на корректность последовательностей вызовов методов интерфейса.
 *  <li> форматирование данных в виде наиболее подходящем для визуального просмотра результата (может использоваться для отладки).
 * </ul> 
 * <strong>Внимание!</strong> Экземпляры данного класса не являются потокобезопасными и должны использоваться только из одного потока в каждую единицу времени.
 *
 * @author Anton Sharapov
 */
public class PrintableJsonWriter implements JsonWriter {

    public static final int DEFAULT_INDENT_FACTOR = 2;  // сколько пробелов соответствуют одному уровню иерархии.

    private static enum State{UNKNOWN, ARRAY, OBJECT, OBJATTR}

    private static final class Context {
        private final Context prev;
        private final String prefix;
        private final int depth;
        private State state;
        private int items;
        private boolean inWriteObj;
        private Context() {
            this.prev = this;
            this.prefix = "";
            this.depth = 0;
            this.state = State.UNKNOWN;
        }
        private Context(final Context prev, final State state, final String prefix) {
            this.prev = prev;
            this.prefix = prefix;
            this.depth = prev.depth + 1;
            this.state = state;
        }
        public String toString() {
            return "[Context{state:"+state+", items:"+items+", inWriteObj:"+inWriteObj+"}]";
        }
    }

    private final JsonContext ctx;
    private final Writer out;
    private final JsonFieldNameSerializer fieldNameSerializer;
    private final int indent;
    private final ArrayList<String> indents;
    private Context current;

    /**
     * Инициализирует поток данных в нотации JSON.
     * @param ctx  глобальный контекст. Обязательное поле, не может быть <code>null</code>.
     * @param out  выходной поток куда будет помещаться результат. Не может быть <code>null</code>.
     * @param indent  определяет величину отступов при форматировании.
     */
    public PrintableJsonWriter(final JsonContext ctx, final Writer out, final int indent) {
        if (ctx==null || out==null)
            throw new IllegalArgumentException("All arguments should be specified");
        this.ctx = ctx;
        this.out = out;
        this.fieldNameSerializer = ctx.getFieldNameSerializer();
        this.indent = indent>=0 ? indent : DEFAULT_INDENT_FACTOR;
        this.indents = JsonUtil.getIndentationStrings(this.indent);
        this.current = new Context();
    }

    /**
     * Инициализирует поток данных в нотации JSON.
     * @param ctx  глобальный контекст. Обязательное поле, не может быть <code>null</code>.
     * @param out  выходной поток куда будет помещаться результат. Не может быть <code>null</code>.
     */
    public PrintableJsonWriter(final JsonContext ctx, final Writer out) {
        this(ctx, out, DEFAULT_INDENT_FACTOR);
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
                current = new Context(current, State.ARRAY, getIndentString(current.depth));
                out.write("[");
                break;
            }
            case ARRAY : {
                if (!current.inWriteObj && current.items++>0)
                    out.write(',');
                current = new Context(current, State.ARRAY, getIndentString(current.depth));
                out.write("[");
                break;
            }
            case OBJECT : {
                throw new IllegalStateException();
            }
            case OBJATTR : {
                current.state = State.OBJECT;
                current = new Context(current, State.ARRAY, getIndentString(current.depth));
                out.write("[");
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
                out.write('\n');
                out.write(current.prefix);
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
                current = new Context(current, State.OBJECT, getIndentString(current.depth));
//                out.write("{");
                break;
            }
            case ARRAY : {
                if (!current.inWriteObj && current.items++ > 0) {
                    out.write(',');
                }
                current = new Context(current, State.OBJECT, getIndentString(current.depth));
//                out.write(" {");
                break;
            }
            case OBJECT : {
                throw new IllegalStateException();
            }
            case OBJATTR : {
                current.state = State.OBJECT;
                current = new Context(current, State.OBJECT, getIndentString(current.depth));
//                out.write("{");
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
                if (current.items==0) {
                    current = current.prev;
                    if (current.state==State.ARRAY) {
                        out.write('\n');
                        out.write(current.prefix);
                    }
                    out.write("{}");
                } else {
                    current = current.prev;
                    out.write('\n');
                    out.write(current.prefix);
                    out.write('}');
                }
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
                if (current.items++ > 0) {
                    out.write(',');
                }
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
                    out.write(",\n");
                } else {
                    out.write("{\n");
                }
                out.write(current.prefix);
                fieldNameSerializer.serialize(name, out);
                out.write(": ");
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
                    out.write(",\n");
                } else {
                    out.write("{\n");
                }
                out.write(current.prefix);
                fieldNameSerializer.serialize(name, out);
                out.write(": ");
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
            final char[] buf = new char[size* indent];
            Arrays.fill(buf, ' ');
            final String result = new String(buf);
            indents.add( result );
            return result;
        }
    }

}
