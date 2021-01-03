package org.echosoft.common.query.processors;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Преобразует SQL выражения вида
 * <pre>
 *   SELECT a, b, c FROM table WHERE d = &param1 AND e = &param2
 * </pre>
 * к следующему виду:
 * <pre>
 *     SELECT a, b, c FROM table WHERE d = ? AND e = ?
 * </pre>
 * Класс корректно обработает содержимое текстовых идентификаторов и примечаний разных видов.
 *
 * @author Anton Sharapov
 */
public class ParametrizedSQL implements Serializable {

    public static final char PARAMS_HEADER = '&';

    private final String sql;
    private final List<String> paramNames;

    /**
     * @param namedSql текст SQL запроса в котором возможно есть именованные параметры. Параметр не может быть <code>null</code>.
     */
    public ParametrizedSQL(final String namedSql) {
        final ArrayList<String> params = new ArrayList<String>();
        this.sql = transform(namedSql, params);
        params.trimToSize();
        this.paramNames = Collections.unmodifiableList(params);
    }

    /**
     * Возвращает текст запроса в котором все вхождения именованных параметров были заменены на символ <code>'?'</code>.
     *
     * @return текст запроса полученного после замены именованных параметров на анонимные.
     *         Метод никогда не возвращает <code>null</code>.
     */
    public String getQuery() {
        return sql;
    }

    /**
     * Возвращает список имен параметров в том порядке в котором они встречаются в запросе. Если какой-то параметр
     * встречается два и более раза то в возвращаемом списке он будет встречаться соответствующее количество раз в
     * тех позициях которые соответствовали вхождению данного параметра в исходном запросе.
     *
     * @return неизменяемый список имен параметров в том порядке в котором они встречаются в запросе.
     *         Метод никогда не возвращает <code>null</code>.
     */
    public List<String> getParamNames() {
        return paramNames;
    }
    
    public void applyParams(final PreparedStatement pstmt, final Map<String,Object> params) throws SQLException {
        int num = 1;
        for (String paramName : paramNames) {
            final Object value = params.get(paramName);
            if (value == null && !params.containsKey(paramName))
                throw new SQLException("Parameter '" + paramName + "' not specified in the arguments");
            pstmt.setObject(num++, value);
        }
    }
    

    @Override
    public int hashCode() {
        return sql.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;
        final ParametrizedSQL other = (ParametrizedSQL) obj;
        return sql.equals(other.sql) && paramNames.equals(other.paramNames);
    }


    private static enum State {
        GENERAL, TEXT_SQ, TEXT_DQ, COMMENT_SL, COMMENT_ML, PARAM_NAME
    }

    private static String transform(final String namedSql, final List<String> paramNames) {
        if (namedSql.indexOf(PARAMS_HEADER) < 0)
            return namedSql;
        State state = State.GENERAL;
        final StringBuilder buf = new StringBuilder(namedSql.length());
        final StringBuilder pbuf = new StringBuilder();
        char pc = 0;
        for (int i = 0, len = namedSql.length(), lastPos = len - 1; i < len; i++) {
            final char c = namedSql.charAt(i);
            switch (state) {
                case GENERAL: {
                    switch (c) {
                        case '\'': {
                            state = State.TEXT_SQ;
                            buf.append(c);
                            break;
                        }
                        case '\"': {
                            state = State.TEXT_DQ;
                            buf.append(c);
                            break;
                        }
                        case '-': {
                            if (pc == '-')
                                state = State.COMMENT_SL;
                            buf.append(c);
                            break;
                        }
                        case '*': {
                            if (pc == '/')
                                state = State.COMMENT_ML;
                            buf.append(c);
                            break;
                        }
                        case PARAMS_HEADER: {
                            if ((!Character.isLetterOrDigit(pc)) && i < lastPos && Character.isJavaIdentifierStart(namedSql.charAt(i + 1))) {
                                state = State.PARAM_NAME;
                                buf.append('?');
                            } else {
                                buf.append(c);
                            }
                            break;
                        }
                        default: {
                            buf.append(c);
                        }
                    }
                    break;
                }
                case TEXT_SQ: {
                    if (c == '\'')
                        state = State.GENERAL;
                    buf.append(c);
                    break;
                }
                case TEXT_DQ: {
                    if (c == '\"')
                        state = State.GENERAL;
                    buf.append(c);
                    break;
                }
                case COMMENT_SL: {
                    if (c == '\n')
                        state = State.GENERAL;
                    buf.append(c);
                    break;
                }
                case COMMENT_ML: {
                    if (pc == '*' && c == '/')
                        state = State.GENERAL;
                    buf.append(c);
                    break;
                }
                case PARAM_NAME: {
                    if (Character.isJavaIdentifierPart(c)) {
                        pbuf.append(c);
                    } else {
                        paramNames.add(pbuf.toString());
                        pbuf.setLength(0);
                        buf.append(c);
                        state = State.GENERAL;
                    }
                    break;
                }
            }
            pc = c;
        }
        if (state == State.PARAM_NAME)
            paramNames.add(pbuf.toString());
        return buf.toString();
    }
}
