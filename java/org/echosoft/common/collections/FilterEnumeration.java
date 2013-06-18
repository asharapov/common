package org.echosoft.common.collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.echosoft.common.model.Predicate;


/** 
 * Итератор - прокси выполняющий фильтрацию содержимого исходного итератора.
 *
 * @author Anton Sharapov
 */
public class FilterEnumeration<T> implements Enumeration<T> {
    
    private final Enumeration<T> iter;
    private final Predicate<T> predicate;
    private boolean nextCalculated;
    private boolean hasNext;
    private T next;
    
    /**
     * @param iter      исходный итератор из которого надо отбирать только те элементы что соответствуют задаваемому предикату.
     * @param predicate предикат для отбора элементов из исходного итератора.
     */
    public FilterEnumeration(final Enumeration<T> iter, final Predicate<T> predicate) {
        this.iter = iter;
        this.predicate = predicate;
        this.nextCalculated = false;
    }


    /**
     * Возвращает <code>true</code> если в исходном итераторе содержится как минимум один элемент
     * удовлетворяющий заданному предикату.
     *
     * @return true если итератор содержит как миниму один элемент удовлетворяющий заданному предикату.
     */
    @Override
    public boolean hasMoreElements() {
        ensureNextCalculated();
        return hasNext;
    }

    /**
     * Возвращает очередной элемент исходного итератора соответствующий предикату.
     *
     * @return очередной элемент итератора.
     * @throws NoSuchElementException в случае исчерпания элементов в исходном итераторе.
     */
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
