package org.echosoft.common.collections;

/**
 * @author Anton Sharapov
 */
public interface Producer<T> {

    public T get() throws Exception;

}
