package org.echosoft.common.json;

import org.echosoft.common.utils.StringUtil;

/**
 * Javascript выражение, описывающее некоторую функции (именованную или анонимную).
 *
 * @author Anton Sharapov
 */
public class JSFunction extends JSExpression {

    private final String[] args;
    private final String body;

    public JSFunction(final String[] args, final JSExpression body) {
        this(args, body != null ? body.getExpression() : null);
    }
    public JSFunction(final String[] args, final String body) {
        super(makeFunction(args, body));
        this.args = args != null ? args : StringUtil.EMPTY_STRING_ARRAY;
        this.body = body != null ? body : "";
    }

    public JSFunction(final JSExpression body) {
        this(body != null ? body.getExpression() : null);
    }
    public JSFunction(final String body) {
        super(makeFunction(null, body));
        this.args = StringUtil.EMPTY_STRING_ARRAY;
        this.body = body != null ? body : "";
    }

    /**
     * Возвращает перечень формальных аргументов для данной функции.
     *
     * @return Перечень формальных аргументов функции. Для функций без аргументов возвращает массив нулевой длины. Никогда не возвращает <code>null</code>.
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Возвращает тело функции.
     *
     * @return тело функции.
     */
    public String getBody() {
        return body;
    }

    private static String makeFunction(final String[] args, final String body) {
        final StringBuilder out = new StringBuilder(30);
        out.append("function(");
        if (args != null) {
            for (int i = 0, ln = args.length; i < ln; i++) {
                if (i > 0)
                    out.append(',');
                out.append(args[i]);
            }
        }
        out.append("){");
        if (body != null)
            out.append(body);
        out.append('}');
        return out.toString();
    }
}
