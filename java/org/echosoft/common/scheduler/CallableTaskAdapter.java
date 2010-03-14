package org.echosoft.common.scheduler;

import java.util.concurrent.Callable;

/**
 * Конвертирует объекты реализующие интерфейс {@link Callable} в объекты класса {@link AbstractTask}.
 * @author Anton Sharapov
 */
public class CallableTaskAdapter<V> extends AbstractTask<V> {

    private final Callable<V> callable;

    public CallableTaskAdapter(final Callable<V> callable) {
        super(callable, null, callable.toString());
        this.callable = callable;
    }

    @Override
    public V call() throws Exception {
        return callable.call();
    }

    @Override
    public int hashCode() {
        return callable.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final CallableTaskAdapter other = (CallableTaskAdapter)obj;
        return callable.equals(other.callable);
    }

    @Override
    public String toString() {
        return "[CallableTaskAdapter{"+callable+"}]";
    }
}
