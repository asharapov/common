package org.echosoft.common.json;

import java.io.Serializable;

/**
 * Позволяет использовать произвольные выражения языка javascript в качестве значений свойств сериализуемых java бинов.
 * <p>пример:</p>
 * <pre>
 * public class SomeClass {
 *      public final JSExpression currentMonth = new JSExpression("new Date().getMonth()")
 * }
 * </pre>
 * <p>Объекты вышеприведенного класса будут сериализованы в структуру следующего вида:</p>
 * <pre>
 * System.out.println( JsonBuilder.toJson( new SomeClass()) );
 * ===== OUTPUT =====
 * {currentMonth: new Date().getMonth()}
 * </pre>
 *
 * @author Anton Sharapov
 */
public class JSExpression implements Serializable, Cloneable {

    private final String expression;

    public JSExpression(final String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }


    public int hashCode() {
        return expression!=null ? expression.hashCode() : 0;
    }

    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final JSExpression other = (JSExpression)obj;
        return (expression!=null ? expression.equals(other.expression) : other.expression==null);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String toString() {
        return "[JSExpression{"+getExpression()+"}]";
    }
}
