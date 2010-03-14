package org.echosoft.common.collections;

import java.util.Enumeration;
import java.util.Iterator;

/** 
 * Adapter to make an {@link Iterator Iterator} instance appear to be an {@link Enumeration Enumeration} instances
 * @author Anton Sharapov
 */
public class IteratorEnumeration<T> implements Enumeration<T> {

    private final Iterator<T> iterator;

    /**
     *  Constructs a new <Code>IteratorEnumeration</Code> that will use
     *  the given iterator. 
     * 
     *  @param iterator  the iterator to use
     */
    public IteratorEnumeration(Iterator<T> iterator) {
        this.iterator = iterator;
    }


    /**
     *  Returns true if the underlying iterator has more elements.
     *  @return true if the underlying iterator has more elements
     */
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    /**
     *  Returns the next element from the underlying iterator.
     *  @return the next element from the underlying iterator.
     *  @throws java.util.NoSuchElementException  if the underlying iterator has no more elements
     */
    public T nextElement() {
        return iterator.next();
    }
}
