package org.echosoft.common.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

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

    private static enum State{UNKNOWN, ARRAY, OBJECT, OBJATTR}
    private static final int DEFAULT_INDENT_FACTOR = 2;         // сколько пробелов соответствуют одному уровню иерархии.
    private static final char[] NUL = {'n','u','l','l'};
    private static final char[] LRB = {'{','}'};
    private static final char[] LBE = {'{','\n'};
    private static final char[] CME = {',','\n'};
    private static final char[] CLW = {':',' '};

    private static final class Indenter {
        private final Writer out;
        private final int factor;
        private int depth;
        private char[] buf;
        private Indenter(final Writer out, final int factor) {
            this.out = out;
            this.factor = factor;
            this.depth = 16;
            this.buf = new char[factor * depth];
            for (int i=buf.length-1; i>=0; i--) buf[i] = ' ';
        }
        private void ensureCapacity(final int requiredDepth) {
            if (requiredDepth>depth) {
                this.depth = requiredDepth;
                this.buf = new char[factor * depth];
                for (int i=buf.length-1; i>=0; i--) buf[i] = ' ';
            }
        }
        private void indent(final int depth) throws IOException {
            out.write(buf, 0, depth*factor);
        }
    }

    private static final class Context {
        private final Context prev;
        private final int depth;
        private final Indenter indenter;
        private State state;
        private int items;
        private boolean inWriteObj;
        private Context(final Indenter indenter) {
            this.prev = this;
            this.depth = 0;
            this.indenter = indenter;
            this.state = State.UNKNOWN;
        }
        private Context(final Context prev, final State state) {
            this.prev = prev;
            this.depth = prev.depth + 1;
            this.indenter = prev.indenter;
            indenter.ensureCapacity(this.depth);
            this.state = state;
        }
        private void indent() throws IOException {
            indenter.indent(depth);
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
     * @param indentFactor  определяет величину отступов при форматировании.
     */
    public PrintableJsonWriter(final JsonContext ctx, final Writer out, final int indentFactor) {
        if (ctx==null || out==null)
            throw new IllegalArgumentException("All arguments should be specified");
        this.ctx = ctx;
        this.out = out;
        this.fieldNameSerializer = ctx.getFieldNameSerializer();
        final Indenter indenter = new Indenter(out, indentFactor>=0 ? indentFactor : DEFAULT_INDENT_FACTOR);
        this.current = new Context( indenter );
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
                out.write('\n');
                current.indent();
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
                break;
            }
            case ARRAY : {
                if (!current.inWriteObj && current.items++ > 0) {
                    out.write(',');
                }
                current = new Context(current, State.OBJECT);
                break;
            }
            case OBJECT : {
                throw new IllegalStateException();
            }
            case OBJATTR : {
                current.state = State.OBJECT;
                current = new Context(current, State.OBJECT);
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
                        current.indent();
                    }
                    out.write(LRB,0,2);     // out.write("{}");
                } else {
                    current = current.prev;
                    out.write('\n');
                    current.indent();
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
                    out.write(NUL,0,4);     // out.write("null");
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
                    out.write(NUL,0,4);     // out.write("null");
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
                    out.write(NUL,0,4);     // out.write("null");
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
                    out.write(CME,0,2);     // out.write(",\n");
                } else {
                    out.write(LBE,0,2);     // out.write("{\n");
                }
                current.indent();
                fieldNameSerializer.serialize(name, out);
                out.write(CLW,0,2);         // out.write(": ");
                if (value == null) {
                    out.write(NUL,0,4);     // out.write("null");
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
                    out.write(CME,0,2);     // out.write(",\n");
                } else {
                    out.write(LBE,0,2);     // out.write("{\n");
                }
                current.indent();
                fieldNameSerializer.serialize(name, out);
                out.write(CLW,0,2);         // out.write(": ");
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

    public int getIndentLength() {
        return current.indenter.factor * current.depth;
    }
}
