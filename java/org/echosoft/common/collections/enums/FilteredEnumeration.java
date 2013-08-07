package org.echosoft.common.collections.enums;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.echosoft.common.collections.Predicate;
import org.echosoft.common.collections.Predicates;


/** 
 * Итератор - прокси выполняющий фильтрацию содержимого исходного итератора.
 *
 * @author Anton Sharapov
 */
public class FilteredEnumeration<T> implements Enumeration<T> {

    private final Enumeration<T> iter;
    private final Predicate<T> predicate;
    private boolean nextCalculated;
    private boolean hasNext;
    private T next;

    /**
     * @param iter      исходный итератор из которого надо отбирать только те элементы что соответствуют задаваемому предикату.
     * @param predicate предикат для отбора элементов из исходного итератора.
     */
    public FilteredEnumeration(final Enumeration<T> iter, final Predicate<T> predicate) {
        this.iter = iter;
        this.predicate = predicate != null ? predicate : Predicates.<T>all();
        this.nextCalculated = false;
    }

    @Override
    public boolean hasMoreElements() {
        ensureNextCalculated();
        return hasNext;
    }

    @Override
    public T nextElement() {
        ensureNextCalculated();
        if (!hasNext)
            throw new NoSuchElementException();
        final T result = next;
        nextCalculated = false;
        next = null;
        return result;
    }

    public T readAhead() {
        ensureNextCalculated();
        if (!hasNext)
            throw new NoSuchElementException();
        return next;
    }

    protected void ensureNextCalculated() {
        if (!nextCalculated) {
            nextCalculated = true;
            while (iter.hasMoreElements()) {
                final T object = iter.nextElement();
                if (predicate.accept(object)) {
                    next = object;
                    hasNext = true;
                    return;
                }
            }
            hasNext = false;
            next = null;
        }
    }
}
