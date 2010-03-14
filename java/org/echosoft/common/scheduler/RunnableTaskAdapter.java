package org.echosoft.common.scheduler;

/**
 * Конвертирует объекты реализующие интерфейс {@link Runnable} в объекты класса {@link AbstractTask}.
 * @author Anton Sharapov
 */
public class RunnableTaskAdapter<V> extends AbstractTask<V> {

    private final Runnable runnable;
    private final V result;

    public RunnableTaskAdapter(final Runnable runnable, final V result) {
        super(runnable, null, runnable.toString());
        this.runnable = runnable;
        this.result = result;
    }

    @Override
    public V call() throws Exception {
        runnable.run();
        return result;
    }

    @Override
    public int hashCode() {
        return runnable.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final RunnableTaskAdapter other = (RunnableTaskAdapter)obj;
        return runnable.equals(other.runnable);
    }

    @Override
    public String toString() {
        return "[RunnableTaskAdapter{"+runnable+"}]";
    }
}