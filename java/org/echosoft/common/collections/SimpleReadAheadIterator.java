package org.echosoft.common.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Простая реализация интерфейса <code>ReadAheadIterator</code>.
 *
 * @author Anton Sharapov
 */
public class SimpleReadAheadIterator<T> implements ReadAheadIterator<T> {

    private final Iterator<T> iter;
    private boolean hasnext;
    private T next;

    public SimpleReadAheadIterator(final Iterator<T> iter) {
        this.iter = iter;
        hasnext = iter.hasNext();
        if (hasnext) {
            next = iter.next();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return hasnext;
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public void remove() {
        iter.remove();
    }


    /**
     * {@inheritDoc}
     */
    public T readAhead() {
        if (!hasnext)
            throw new NoSuchElementException();
        return next;
    }

}
