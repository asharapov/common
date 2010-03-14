package org.echosoft.common.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Decorates existing iterator and add new method for reading next element without changing cursor in the iterator.
 *
 * @author Anton Sharapov
 */
public class ReadAheadIterator<T> implements Iterator<T> {

    private final Iterator<T> iter;
    private boolean hasnext;
    private T next;

    public ReadAheadIterator(final Iterator<T> iter) {
        this.iter = iter;
        hasnext = iter.hasNext();
        if (hasnext) {
            next = iter.next();
        }
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        return hasnext;
    }

    /**
     * Returns the next element in the iteration.  Calling this method
     * repeatedly until the {@link #hasNext()} method returns false will
     * return each element in the underlying collection exactly once.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException  if iteration has no more elements.
     */
    public T next() {
        if (!hasnext)
            throw new NoSuchElementException();
        final T result = next;
        hasnext = iter.hasNext();
        if (hasnext) {
            next = iter.next();
        }
        return result;
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation).  This method can be called only once per
     * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
     * the underlying collection is modified while the iteration is in
     * progress in any way other than by calling this method.
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt>
     *                                       operation is not supported by this Iterator.
     * @throws IllegalStateException         if the <tt>next</tt> method has not
     *                                       yet been called, or the <tt>remove</tt> method has already
     *                                       been called after the last call to the <tt>next</tt>
     *                                       method.
     */
    public void remove() {
        iter.remove();
    }


    /**
     * Returns the next element in the iteration without changing iteration state.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException  if iteration has no more elements.
     */
    public T readAhead() {
        if (!hasnext)
            throw new NoSuchElementException();
        return next;
    }

}
