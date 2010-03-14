package org.echosoft.common.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.echosoft.common.model.Predicate;


/** 
 * A Proxy {@link Iterator} which takes a {@link org.echosoft.common.model.Predicate} instance to filter out objects from an
 * underlying {@link Iterator} instance.
 * Only objects for which the specified <code>Predicate</code> evaluates to <code>true</code> are returned.
 * 
 * @author Anton Sharapov
 */
public class FilterIterator<T> implements Iterator<T> {
    
    private final Iterator<T> iterator;
    private final Predicate<T> predicate;
    private T nextObject;
    private boolean hasNextObject = false;
    
    /**
     *  Constructs a new <Code>FilterIterator</Code> that will use the given iterator and predicate.
     *  @param iterator  the iterator to use
     *  @param predicate  the predicate to use
     */
    public FilterIterator(Iterator<T> iterator, Predicate<T> predicate ) {
        this.iterator = iterator;
        this.predicate = predicate;
    }


    /** 
     *  Returns true if the underlying iterator contains an object that matches the predicate.
     *  @return true if there is another object that matches the predicate 
     */
    public boolean hasNext() {
        return hasNextObject || findNextObject();
    }

    /** 
     *  Returns the next object that matches the predicate.
     *  @return the next object which matches the given predicate
     *  @throws NoSuchElementException if there are no more elements that match the predicate
     */
    public T next() {
        if ( !hasNextObject ) {
            if (!findNextObject()) {
                throw new NoSuchElementException();
            }
        }
        hasNextObject = false;
        return nextObject;
    }

    /**
     * Always throws UnsupportedOperationException as this class does look-ahead with its internal iterator.
     * @throws UnsupportedOperationException  always
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
        

    /**
     * Set nextObject to the next object. 
     * If there are no more objects then return false. Otherwise, return true.
     * @return <code>true</code> if next object was finded.
     */
    private boolean findNextObject() {
        while ( iterator.hasNext() ) {
            final T object = iterator.next();
            if ( predicate.accept( object ) ) {
                nextObject = object;
                hasNextObject = true;
                return true;
            }
        }
        return false;
    }
}
