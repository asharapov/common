package org.echosoft.common.collections;

/**
 * @author Anton Sharapov
 */
public interface CloseableConsumer<T> extends Consumer<T>, AutoCloseable {

    @Override
    public boolean consume(final T t) throws Exception;

    @Override
    public void close() throws Exception;

}
