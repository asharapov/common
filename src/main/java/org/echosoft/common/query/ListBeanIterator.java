package org.echosoft.common.query;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Anton Sharapov
 */
public final class ListBeanIterator<T> implements BeanIterator<T> {

    private final List<T> beans;
    private final BeanMetaData metadata;
    private final int size;
    private int cursor;

    public ListBeanIterator(List<T> beans, BeanMetaData metadata) {
        if (beans==null)
            beans = Collections.emptyList();
        if (metadata==null) {
            metadata = BeanMetaData.resolveMetaData( beans.size()>0 ? beans.get(0) : null );
        }
        this.beans = beans;
        this.size = beans.size();
        this.metadata = metadata;
    }

    public ListBeanIterator(QueryResult<T> qr) {
        if (qr==null)
            throw new IllegalArgumentException();
        this.beans = qr.getBeans();
        this.size = qr.getBeans().size();
        this.metadata = qr.getMetaData();
    }


    /**
     * {@inheritDoc}
     */
    public BeanMetaData getMetaData() {
        return metadata;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return cursor < size;
    }

    /**
     * {@inheritDoc}
     */
    public T next() {
        if (cursor>=size)
            throw new NoSuchElementException();
        return beans.get(cursor++);
    }

    /**
     * {@inheritDoc}
     */
    public T readAhead() {
        if (cursor>=size)
            throw new NoSuchElementException();
        return beans.get(cursor);
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
    }

}
