package org.echosoft.common.data;

import java.io.Serializable;

/**
 * @author Anton Sharapov
 */
public class SortCriterion implements Serializable {

    private final String field;
    private boolean ascending;

    public SortCriterion(final String field, final boolean ascending) {
        if (field == null)
            throw new IllegalArgumentException("Field must be specified");
        this.field = field;
        this.ascending = ascending;
    }

    public String getField() {
        return field;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(final boolean ascending) {
        this.ascending = ascending;
    }

    public void toggleOrder() {
        this.ascending = !this.ascending;
    }

    public boolean hasSameField(final SortCriterion other) {
        return other != null && field.equals(other.field);
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;
        final SortCriterion other = (SortCriterion) obj;
        return field.equals(other.field) && ascending == other.ascending;
    }

    @Override
    public String toString() {
        return ascending ? "{" + field + " ASC}" : "{" + field + " DESC}";
    }
}
