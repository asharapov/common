package org.echosoft.common.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains and handles a set of {@link SortCriterion} instances.
 * @see Query
 * @author Anton Sharapov
 */
public final class SortCriteria implements Serializable, Cloneable {

    public static final SortCriterion CRITERION_ARRAY[] = new SortCriterion[0];
    private final List<SortCriterion> criteria;
    private transient int hashcode;

    public SortCriteria() {
        criteria = new ArrayList<SortCriterion>(2);
    }

    public SortCriterion[] toArray() {
        return criteria.toArray(new SortCriterion[criteria.size()]);
    }

    public SortCriterion getCriterion(String field) {
        for (int i=0; i<criteria.size(); i++) {
            final SortCriterion criterion = criteria.get(i);
            if (criterion.getField().equals(field))
                return criterion;
        }
        return null;
    }

    public boolean isEmpty() {
        return criteria.isEmpty();
    }

    public int size() {
        return criteria.size();
    }

    public void append(SortCriteria criteria) {
        for (int i=0; i<criteria.criteria.size(); i++)
            append( criteria.criteria.get(i) );
    }

    public void append(String field, boolean ascending) {
        append( new SortCriterion(field, ascending) );
    }

    public void append(SortCriterion criterion) {
        if (criterion==null)
            throw new NullPointerException("Sort criterion must be specified");

        for (int i=0; i<criteria.size(); i++) {    // ensures what this criterion does not exists in order.
            final SortCriterion c = criteria.get(i);
            if (c.getField().equals(criterion.getField())) {
                criteria.remove(i);
                break;
            }
        }
        criteria.add(criterion);
    }


    public void update(SortCriteria criteria) {
        for (int i=0; i<criteria.criteria.size(); i++)
            update( criteria.criteria.get(i) );
    }

    public void update(String field, boolean ascending) {
        this.update(new SortCriterion(field, ascending));
    }

    public void update(SortCriterion criterion) {
        if (criterion==null)
            throw new NullPointerException("Sort criterion must be specified");

        SortCriterion c = getCriterion(criterion.getField());
        if (c!=null)
            c.setAscending(criterion.isAscending());
        else
            criteria.add(criterion);
    }


    public SortCriterion remove(String field) {
        final SortCriterion result = getCriterion(field);
        criteria.remove(result);
        return result;
    }

    public void clear() {
        criteria.clear();
    }


    public Object clone() {
        final SortCriteria result = new SortCriteria();
        for (int i=0; i<criteria.size(); i++) {
            final SortCriterion criterion = criteria.get(i);
            result.criteria.add( new SortCriterion(criterion.getField(), criterion.isAscending()) );
        }
        return result;
    }

    public int hashCode() {
        if (hashcode==0) {
            // we use local variable 'result' to prevent potential problems with concurent calls...
            int result = 0;
            for (int i=0; i<criteria.size(); i++) {
                final SortCriterion criterion = criteria.get(i);
                result += criterion.hashCode() << 4;
            }
            hashcode = result;
        }
        return hashcode;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SortCriteria))
            return false;
        final SortCriteria other = (SortCriteria)obj;
        return criteria.equals(other.criteria);
    }

    public String toString() {
        final StringBuilder out = new StringBuilder(30);
        out.append("[SortCriteria{");
        for (Iterator i=criteria.iterator(); i.hasNext(); ) {
            final SortCriterion criterion = (SortCriterion)i.next();
            out.append(criterion.getField());
            out.append(criterion.isAscending() ? " ASC" : " DESC");
            if (i.hasNext())
                out.append(", ");
        }
        out.append("}]");
        return out.toString();
    }
}
