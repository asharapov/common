package org.echosoft.common.collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.echosoft.common.model.Predicate;


/** 
 * A Proxy {@link Enumeration Enumeration} which takes a {@link Predicate} instance to filter out objects
 * from an underlying {@link Enumeration} instance.
 * Only objects for which the specified <code>Predicate</code> evaluates to <code>true</code> are returned.
 * 
 * @author Anton Sharapov
 */
public class FilterEnumeration<T> implements Enumeration<T> {
    
    private final Enumeration<T> iterator;
    private final Predicate<T> predicate;
    private T nextObject;
    private boolean hasNextObject = false;
    
    /**
     *  Constructs a new <Code>FilterIterator</Code> that will use the given iterator and predicate.
     *  @param iterator  the iterator to use
     *  @param predicate  the predicate to use
     */
    public FilterEnumeration(Enumeration<T> iterator, Predicate<T> predicate) {
        this.iterator = iterator;
        this.predicate = predicate;
    }


    /** 
     *  Returns true if the underlying iterator contains an object that matches the predicate.
     *  @return true if there is another object that matches the predicate 
     */
    public boolean hasMoreElements() {
        return hasNextObject || findNextObject();
    }

    /** 
     *  Returns the next object that matches the predicate.
     *  @return the next object which matches the given predicate
     *  @throws NoSuchElementException if there are no more elements that match the predicate
     */
    public T nextElement() {
        if ( !hasNextObject ) {
            if (!findNextObject()) {
                throw new NoSuchElementException();
            }
        }
        hasNextObject = false;
        return nextObject;
    }
       

    /**
     * Set nextObject to the next object. 
     * If there are no more objects then return false. Otherwise, return true.
     * @return <code>true</code> if next object was finded.
     */
    private boolean findNextObject() {
        while ( iterator.hasMoreElements() ) {
            final T object = iterator.nextElement();
            if ( predicate.accept( object ) ) {
                nextObject = object;
                hasNextObject = true;
                return true;
            }
        }
        return false;
    }
}
