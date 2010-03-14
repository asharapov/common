package org.echosoft.common.query;

import java.io.Serializable;

/**
 * Used for applying constraints on values of various entities attributes.
 * @see Query
 * @author Anton Sharapov
 */
public final class QueryConstraint implements Serializable {

    private final String attrName;
    private final QueryOperator op;
    private final Object value;

    private boolean negative;
    private boolean caseSensitive;

    /**
     * Creates new constraint for given attribute
     * @param attrName  an attribute name
     * @param op  constraint's kind
     * @param value  an argument for this constraint
     */
    public QueryConstraint(String attrName, QueryOperator op, Object value) {
        this(attrName, op, value, false, true);
    }

    /**
     * Creates new constraint for given attribute
     * @param attrName  an attribute name
     * @param op  constraint's kind
     * @param value  an argument for this constraint
     * @param negative  query processor should negate this constraint
     * @param caseSensitive  query processor should ignore case of this attribute values
     */
    public QueryConstraint(String attrName, QueryOperator op, Object value, boolean negative, boolean caseSensitive) {
        if (attrName==null || op==null)
            throw new IllegalArgumentException("Attribute name and operations kind must be specified");
        this.attrName = attrName;
        this.op = op;
        this.value = value;
        this.negative = negative;
        this.caseSensitive = caseSensitive;
    }

    /**
     * Returns name of attribute for which this constraint was applied.
     * @return an attribute name
     */
    public String getAttrName() {
        return attrName;
    }

    /**
     * Returns kind of given constraint.
     * @return  constraint kind
     */
    public QueryOperator getOperator() {
        return op;
    }

    /**
     * A something value which processed dependently of constraint's kind (operation)
     * @return an
     */
    public Object getValue() {
        return value;
    }

    /**
     * @return <code>true</code> if query processor should negate this constraint
     */
    public boolean isNegative() {
        return negative;
    }

    /**
     * @return <code>true</code> if query processor should ignore case of this attribute values
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }


    public int hashCode() {
        return attrName.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof QueryConstraint))
            return false;
        final QueryConstraint other = (QueryConstraint)obj;
        return attrName.equals(other.attrName) &&
                op.equals(other.op) &&
                negative==other.negative &&
                caseSensitive==other.caseSensitive &&
               (value!=null ? value.equals(other.value) : value==other.value) ;
    }

    public String toString() {
        return "[ "+attrName+" "+op+" "+value+" ]";
    }

}

