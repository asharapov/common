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

    public static final char[] NULL = {'n','u','l','l'};
    public static final char[] TRUE = {'t', 'r', 'u', 'e'};
    public static final char[] FALSE = {'f', 'a', 'l', 's', 'e'};
    private static final char[] SB = {'\\','b'};
    private static final char[] ST = {'\\','t'};
    private static final char[] SN = {'\\','n'};
    private static final char[] SF = {'\\','f'};
    private static final char[] SR = {'\\','r'};
    private static final char[] SQ1 = {'\\','\''};
    private static final char[] SQ2 = {'\\','\"'};
    private static final char[] SS = {'\\','\\'};

    public static void encodeChar(final char c, final Writer out) throws IOException {
        out.write('"');
        switch (c) {
            case '\b' : out.write(SB,0,2); break;   // out.write("\\b")
            case '\t' : out.write(ST,0,2); break;   // out.write("\\t")
            case '\n' : out.write(SN,0,2); break;   // out.write("\\n")
            case '\f' : out.write(SF,0,2); break;   // out.write("\\f")
            case '\r' : out.write(SR,0,2); break;   // out.write("\\r")
            case '\"' : out.write(SQ2,0,2); break;  // out.write("\\\"")
            case '\'' : out.write(SQ1,0,2); break;  // out.write("\\\'")
            case '\\' : out.write(SS,0,2); break;   // out.write("\\\\")
            default: out.write(c);
        }
        out.write('"');
    }

    public static void encodeString(final CharSequence text, final Writer out) throws IOException {
        if (text==null) {
            out.write(NULL,0,4);             // out.write("null");
        } else {
            out.write('"');
            final int length = text.length();
            for (int i=0; i<length; i++) {
                final char c = text.charAt(i);
                switch (c) {
                    case '\b' : out.write(SB,0,2); break;   // out.write("\\b")
                    case '\t' : out.write(ST,0,2); break;   // out.write("\\t")
                    case '\n' : out.write(SN,0,2); break;   // out.write("\\n")
                    case '\f' : out.write(SF,0,2); break;   // out.write("\\f")
                    case '\r' : out.write(SR,0,2); break;   // out.write("\\r")
                    case '\"' : out.write(SQ2,0,2); break;  // out.write("\\\"")
                    case '\'' : out.write(SQ1,0,2); break;  // out.write("\\\'")
                    case '\\' : out.write(SS,0,2); break;   // out.write("\\\\")
                    default : out.write(c);
                }
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
                final JsonSerializer result = (JsonSerializer)method.invoke(null);
                if (result==null)
                    throw new NullPointerException();
                return result;
            } catch (Exception ee) {
                // Это какой-то неправильный синглтон. Мерзко ругаемся чтоб программисты быстрее это зафиксили...
                throw new RuntimeException("Unable to obtain valid json serializer from class: "+scls);
            }
        }
    }

    public static <A extends Annotation> A getDeclaredAnnotation(final Class<?> cls, final Class<A> ac) {
        for (Annotation a : cls.getDeclaredAnnotations()) {
            if (ac.isInstance(a))
                return (A)a;
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
            if (nameLength>3 && name.startsWith("get",0)) {
                if (method.getParameterTypes().length>0)
                    continue;
                if (method.getDeclaringClass()==Object.class)
                    continue;
                result.add( new NamedMethod(method, Introspector.decapitalize(name.substring(3))) );
            } else
            if (name.length()>2 && name.startsWith("is",0)) {
                if (method.getParameterTypes().length>0)
                    continue;
                if (returnType != boolean.class)
                    continue;
                result.add( new NamedMethod(method, Introspector.decapitalize(name.substring(2))) );
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
