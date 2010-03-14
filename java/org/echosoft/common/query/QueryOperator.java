package org.echosoft.common.query;

/**
 * Used for applying constraints on values of various entities attributes.
 * @see Query
 * @author Anton Sharapov
 */
public final class QueryOperator implements java.io.Serializable {
    public static final QueryOperator EQUAL = new QueryOperator("=");
    public static final QueryOperator GREATER = new QueryOperator(">");
    public static final QueryOperator LESSER = new QueryOperator("<");
    public static final QueryOperator EQUAL_OR_GREATER = new QueryOperator(">=");
    public static final QueryOperator EQUAL_OR_LESSER = new QueryOperator("<=");
    public static final QueryOperator IN = new QueryOperator("IN");
    public static final QueryOperator LIKE = new QueryOperator("LIKE");

    private static final QueryOperator operators[] =
            {EQUAL, GREATER, LESSER, EQUAL_OR_GREATER, EQUAL_OR_LESSER, LIKE, IN};

    public static QueryOperator getInstance(String name) {
        name = name!=null ? name.toUpperCase().trim() : null;
        for(int i=0; i<operators.length; i++) {
            if (operators[i].name.equals(name))
                return operators[i];
        }
        return null;
    }


    private final String name;

    private QueryOperator(String name) {
        if (name==null || "".equals(name))
            throw new NullPointerException("Argument must be specified");
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof QueryOperator))
            return false;
        final QueryOperator other = (QueryOperator)obj;
        return name.equals(other.getName());
    }

    public String toString() {
        return "[ "+name+" ]";
    }
}
