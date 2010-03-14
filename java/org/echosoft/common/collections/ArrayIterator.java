package org.echosoft.common.collections;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** 
 * Implements an {@link Iterator} over any array.<br/>
 * The array can be either an array of object or of primitives. If you know  that you have an object array, the
 * {@link ObjectArrayIterator} class is a better choice, as it will perform better.
 * 
 * @author Anton Sharapov
 */
public class ArrayIterator implements Iterator {

    private final Object array;
	private final int length;
	private int index;
    
   
    /**
     * Constructs an ArrayIterator that will iterate over the values in the
     * specified array.
     *
     * @param array the array to iterate over.
     * @throws IllegalArgumentException if <code>array</code> is not an array.
     * @throws NullPointerException if <code>array</code> is <code>null</code>
     */
    public ArrayIterator(final Object array) {
        this.array = array;
        this.length = Array.getLength(array);
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
     * @throws NoSuchElementException if all the elements in the array have already been returned.
     */
    public Object next() {
        if (index>=length)
            throw new NoSuchElementException();
        return Array.get(array, index++);
    }


    /**
     * Throws {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException always
     */
    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported");
    }

}
