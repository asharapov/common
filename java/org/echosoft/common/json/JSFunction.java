package org.echosoft.common.json;

import org.echosoft.common.utils.StringUtil;

/**
 * Javascript выражение, описывающее некоторую функции (именованную или анонимную).
 * @author Anton Sharapov
 */
public class JSFunction extends JSExpression {

    private final String name;
    private final String[] args;
    private final String body;

    public JSFunction(final String[] args, final JSExpression body) {
        this(null,args,body!=null ? body.getExpression() : null);
    }
    public JSFunction(final String[] args, final String body) {
        this(null,args,body);
    }
    public JSFunction(final String name, final String[] args, final String body) {
        super( makeFunction(name,args,body) );
        this.name = StringUtil.trim(name);
        this.args = args!=null ? args : StringUtil.EMPTY_STRING_ARRAY;
        this.body = body!=null ? body : "";
    }

    /**
     * Возвращает имя функции или <code>null</code> для анонимной функции.
     * @return  имя функции или <code>null</code>.
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает перечень формальных аргументов для данной функции.
     * @return  Перечень формальных аргументов функции. Для функций без аргументов возвращает массив нулевой длины. Никогда не возвращает <code>null</code>.
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Возвращает тело функции.
     * @return  тело функции.
     */
    public String getBody() {
        return body;
    }

    private static String makeFunction(final String name, final String[] args, final String body) {
        final StringBuilder out = new StringBuilder(30);
        out.append("function");
        if (name!=null) {
            out.append(' ').append(name);
        }
        out.append('(');
        if (args!=null) {
            for (int i = 0, ln = args.length; i < ln; i++) {
                if (i > 0)
                    out.append(',' );
                out.append(args[i]);
            }
        }
        out.append("){");
        out.append(body);
        out.append('}');
        return out.toString();
    }
}
