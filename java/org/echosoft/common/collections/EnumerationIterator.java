package org.echosoft.common.collections;

import java.util.Enumeration;
import java.util.Iterator;

/** 
 * Adapter to make {@link Enumeration} instances appear to be {@link Iterator} instances.
 * 
 * @author Anton Sharapov
 */
public class EnumerationIterator<T> implements Iterator<T> {
    
    private final Enumeration<T> enumeration;
    
    /**
     * Constructs a new <code>EnumerationIterator</code> that provides an iterator view of the given enumeration.
     * @param enumeration  the enumeration to use
     */
    public EnumerationIterator(final Enumeration<T> enumeration) {
        this.enumeration = enumeration;
    }


    /**
     * Returns true if the underlying enumeration has more elements.
     */
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }


    /**
     * Returns the next object from the enumeration.
     */
    public T next() {
        return enumeration.nextElement();
    }


    /**
     * Removes the last retrieved element if a collection is attached.
     * This operation does not supported.
     * @exception UnsupportedOperationException raised always.
     */
    public void remove() {
        throw new UnsupportedOperationException("this operation does not supported");
    }

}
