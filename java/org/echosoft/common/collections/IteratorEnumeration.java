package org.echosoft.common.collections;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Представляет объект некоторого класса, реализующего интерфейс {@link Iterator}
 * в виде другого объекта реализующего интерфейс {@link Enumeration}.
 *
 * @author Anton Sharapov
 */
public class IteratorEnumeration<T> implements Enumeration<T> {

    private final Iterator<T> iterator;

    public IteratorEnumeration(final Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    @Override
    public T nextElement() {
        return iterator.next();
    }
}
