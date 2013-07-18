package org.echosoft.common.data;

/**
 * @author Anton Sharapov
 */
public interface DataIssuer<T> {

    public boolean hasNext() throws Exception;

    public T next() throws Exception;

    public void close() throws Exception;

}
