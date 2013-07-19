package org.echosoft.common.data;

/**
 * Performs some predicate which returns true or false based on the input object.
 * Predicate instances can be used to implement queries or to do filtering.
 * @author Anton Sharapov
 */
public interface Predicate<T> {

    /**
     * Returns <code>true</code> if the input object matches this predicate.
     * @param input  an input object
     * @return <code>true</code> if the input object matches this predicate, else returns false
     */
    public boolean accept(final T input);
}
