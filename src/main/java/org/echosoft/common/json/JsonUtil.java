package org.echosoft.common.json;

import java.beans.Introspector;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Anton Sharapov
 */
public final class JsonUtil {

    public static final String[] JS_KEYWORDS = {
            "break", "case", "catch", "class", "comment", "const", "continue", "debugger", "default", "delete", "do",
            "else", "enum", "export", "extends", "finally", "for", "function", "if", "import", "in", "label", "new",
            "return", "super", "switch", "this", "throw", "try", "typeof", "var", "void", "while", "with"
    };

    private static final char[][] REPLACEMENT_CHARS;
    static {
        REPLACEMENT_CHARS = new char[128][];
        for (int i = 0; i < 32; i++) {
            REPLACEMENT_CHARS[i] = String.format("\\u%04x", i).toCharArray();
        }
        REPLACEMENT_CHARS['"'] = "\\\"".toCharArray();
        REPLACEMENT_CHARS['\\'] = "\\\\".toCharArray();
        REPLACEMENT_CHARS['\t'] = "\\t".toCharArray();
        REPLACEMENT_CHARS['\b'] = "\\b".toCharArray();
        REPLACEMENT_CHARS['\n'] = "\\n".toCharArray();
        REPLACEMENT_CHARS['\r'] = "\\r".toCharArray();
        REPLACEMENT_CHARS['\f'] = "\\f".toCharArray();
    }

    public static final char[] NULL = {'n', 'u', 'l', 'l'};
    public static final char[] TRUE = {'t', 'r', 'u', 'e'};
    public static final char[] FALSE = {'f', 'a', 'l', 's', 'e'};

    public static void encodeChar(final char c, final Writer out) throws IOException {
        out.write('"');
        if (c < 128) {
            final char[] replacements = REPLACEMENT_CHARS[c];
            if (replacements != null) {
                out.write(replacements);
            } else {
                out.write(c);
            }
        } else
        if (c == '\u2028') {
            out.write("\\u2028");
        } else
        if (c == '\u2029') {
            out.write("\\u2029");
        } else
            out.write(c);
        out.write('"');
    }

    public static void encodeString(final String text, final Writer out) throws IOException {
        if (text == null) {
            out.write(NULL, 0, 4);
        } else {
            out.write('"');
            final int length = text.length();
            int last = 0;
            for (int i = 0; i < length; i++) {
                final char c = text.charAt(i);
                final char replacement[];
                if (c < 128) {
                    replacement = REPLACEMENT_CHARS[c];
                    if (replacement == null)
                        continue;
                } else
                if (c == '\u2028') {
                    replacement = "\\u2028".toCharArray();
                } else
                if (c == '\u2029') {
                    replacement = "\\u2029".toCharArray();
                } else
                    continue;

                if (last < i) {
                    out.write(text, last, i - last);
                }
                out.write(replacement);
                last = i + 1;
            }
            if (last < length) {
                out.write(text, last, length - last);
            }
            out.write('"');
        }
    }

    public static JsonSerializer<?> makeInstance(final Class<? extends JsonSerializer> scls) {
        try {
            return scls.newInstance();
        } catch (Exception e) {
            try {
                // A вдруг это синглтон ?
                final Method method = scls.getMethod("getInstance");
                final JsonSerializer result = (JsonSerializer) method.invoke(null);
                if (result == null)
                    throw new NullPointerException();
                return result;
            } catch (Exception ee) {
                // Это какой-то неправильный синглтон. Мерзко ругаемся чтоб программисты быстрее это зафиксили...
                throw new RuntimeException("Unable to obtain valid json serializer from class: " + scls);
            }
        }
    }

    public static <A extends Annotation> A getDeclaredAnnotation(final Class<?> cls, final Class<A> ac) {
        for (Annotation a : cls.getDeclaredAnnotations()) {
            if (ac.isInstance(a))
                return (A) a;
        }
        return null;
    }

    public static Collection<NamedMethod> findGetters(final Class<?> cls) {
        final Collection<NamedMethod> result = new ArrayList<NamedMethod>();
        for (final Method method : cls.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()))
                continue;
            final Class returnType = method.getReturnType();
            if (returnType == void.class)
                continue;
            final String name = method.getName();
            final int nameLength = name.length();
            if (nameLength > 3 && name.startsWith("get", 0)) {
                if (method.getParameterTypes().length > 0)
                    continue;
                if (method.getDeclaringClass() == Object.class)
                    continue;
                result.add(new NamedMethod(method, Introspector.decapitalize(name.substring(3))));
            } else
            if (name.length() > 2 && name.startsWith("is", 0)) {
                if (method.getParameterTypes().length > 0)
                    continue;
                if (returnType != boolean.class)
                    continue;
                result.add(new NamedMethod(method, Introspector.decapitalize(name.substring(2))));
            }
        }
        return result;
    }

    public static final class NamedMethod {
        public final Method method;
        public final String name;
        public NamedMethod(final Method method, final String name) {
            this.method = method;
            this.name = name;
        }
    }
}
