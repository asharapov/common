package org.echosoft.common.query;

import java.io.Serializable;
import java.util.Comparator;

import org.echosoft.common.utils.BeanUtil;

/**
 * Compares beans according to passed ordering criteria.
 * @author Anton Sharapov
 */
public final class BeanComparator<T> implements Comparator<T>, Serializable {
    private final SortCriterion criteria[];

    public BeanComparator(SortCriteria criteria) {
        this.criteria = criteria.toArray();
    }

    /**
     * Compares its two objects for order.
     * @param o1 the first bean to be compared.
     * @param o2 the second bean to be compared.
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the second.
     * @throws ClassCastException if the arguments' types prevent them from
     *         being compared by this Comparator.
     */
    public int compare(final T o1, final T o2) {
        try {
            for (int i=0; i<criteria.length; i++) {
                final Object v1 = BeanUtil.getProperty(o1, criteria[i].getField());
                final Object v2 = BeanUtil.getProperty(o2, criteria[i].getField());

                if (v1==null && v2!=null)
                    return criteria[i].isAscending() ? -1 : 1;
                if (v1!=null && v2==null)
                    return criteria[i].isAscending() ? 1 : -1;

                if ( (v1 instanceof Comparable) ) {
                    final int result = ((Comparable)v1).compareTo(v2);
                    if (result!=0)
                        return criteria[i].isAscending() ? result : -result;
                }
            }
            return 0;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}