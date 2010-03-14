package org.echosoft.common.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** 
 * An {@link Iterator} over an array of objects.<br/>
 * This iterator does not support {@link #remove}, as the object array cannot be modified.
 *
 * @author Anton Sharapov
 */
public class ObjectArrayIterator<T> implements Iterator<T> {

    private final T[] array;
    private final int length;
    private int index;

    /**
     * Constructs an ObjectArrayIterator that will iterate over the values in the specified array.
     * @param array - the array to iterate over
     * @throws NullPointerException if <code>array</code> is null.
     */
    public ObjectArrayIterator(T[] array) {
        this.array = array;
        this.length = array.length;
        this.index = 0;
    }


    /**
     * Returns true if there are more elements to return from the array.
     */
    public boolean hasNext() {
        return index < length;
    }

    /**
     * Returns the next element in the array.
     * @return the next element in the array
     * @throws NoSuchElementException if all the elements in the array have already been returned
     */
    public T next() {
        if (index>=length)
            throw new NoSuchElementException();
        return array[index++];
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException always
     */
    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported for an ObjectArrayIterator");
    }
}
