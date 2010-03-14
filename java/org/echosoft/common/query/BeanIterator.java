package org.echosoft.common.query;

import java.io.Closeable;
import java.util.NoSuchElementException;

/**
 * @author Anton Sharapov
 */
public interface BeanIterator<T> extends Closeable {

    /**
     * Returns the fields definitions. Can be <code>null</code>.
     *
     * @return beans metadata
     */
    public BeanMetaData getMetaData();

    /**
     * Returns <code>true</code> if the dataset has more beans of the same type.
     * (In other words, returns <code>true</code> if <code>next</code> would return an element
     * rather than throwing an exception.)
     *
     * @return <code>true</code> if the dataset has more rows.
     */
    public boolean hasNext();

    /**
     * Retrieves the next bean from the dataset.
     *
     * @return  a corresponding bean instance.
     * @throws NoSuchElementException if dataset has no more elements.
     */
    public T next() throws Exception;


    /**
     * Returns the next element in the iteration without changing iteration state.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException  if iteration has no more elements.
     */
    public T readAhead();

    /**
     * This method called, when the client has finished iterating
     * through the dataset to allow resources to be deallocated.
     */
    public void close();

}
