package org.echosoft.common.collections.enums;

import java.util.Enumeration;

import org.echosoft.common.collections.Transformer;
import org.echosoft.common.collections.Transformers;

/**
 * Итератор по некоторому множеству данных с их одновременной трансформацией.
 *
 * @author Anton Sharapov
 */
public class TransformedEnumeration<S, D> implements Enumeration<D> {

    private final Enumeration<S> source;
    private final Transformer<S, D> transformer;

    /**
     * @param source итератор по исходеному множеству данных.
     * @param transformer задает механизм трансформации. Если <code>null</code> то трансформации по сути не происходит.
     */
    public TransformedEnumeration(final Enumeration<S> source, final Transformer<S, D> transformer) {
        this.source = source;
        this.transformer = transformer != null ? transformer : Transformers.<S, D>nothing();
    }

    @Override
    public boolean hasMoreElements() {
        return source.hasMoreElements();
    }

    @Override
    public D nextElement() {
        return transformer.transform(source.nextElement());
    }
}
