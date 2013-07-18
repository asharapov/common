package org.echosoft.common.data;

/**
 * @author Anton Sharapov
 */
public interface Issuer<T> {

    public boolean hasNext() throws Exception;

    public T next() throws Exception;

    public void close() throws Exception;

}
