package org.echosoft.common.query;

import java.io.Serializable;

/**
 * This class pairs together a field name and a direction by which a data can be sorted.
 * @see Query
 * @author Anton Sharapov
 */
public final class SortCriterion implements Serializable {

    private final String field;
    private boolean isAscending;

    public SortCriterion(String field, boolean isAscending) {
        if (field==null)
            throw new NullPointerException("Field must be specified");
        this.field = field;
        this.isAscending = isAscending;
    }

    public String getField() {
        return field;
    }

    /**
     * Gets the direction in which the item of this class is sorted.
     * @return true if the item identified by this class is sorted in ascending order.
     */
    public boolean isAscending() {
        return isAscending;
    }
    public void setAscending(boolean isAscending) {
        this.isAscending = isAscending;
    }


    public int hashCode() {
        return field.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SortCriterion))
            return false;
        final SortCriterion that = (SortCriterion)obj;
        return this.field.equals(that.field) &&
               this.isAscending == that.isAscending;
    }

    public String toString() {
        return "[SortCriterion{"+field+" "+(isAscending ? "ASC" : "DESC")+"}]";
    }
}
