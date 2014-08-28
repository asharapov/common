package org.echosoft.common.collections;

/**
 * @author Anton Sharapov
 */
public interface Consumer<T> {

    public boolean consume(final T t) throws Exception;

}
