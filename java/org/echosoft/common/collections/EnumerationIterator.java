package org.echosoft.common.collections;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Представляет объект некоторого класса, реализующего интерфейс {@link Enumeration}
 * в виде другого объекта реализующего интерфейс {@link Iterator}.
 *
 * @author Anton Sharapov
 */
public class EnumerationIterator<T> implements Iterator<T> {
    
    private final Enumeration<T> enumeration;
    
    public EnumerationIterator(final Enumeration<T> enumeration) {
        this.enumeration = enumeration;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }

    /**
     * {@inheritDoc}
     */
    public T next() {
        return enumeration.nextElement();
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        throw new UnsupportedOperationException("this operation does not supported");
    }

}
