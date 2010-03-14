package org.echosoft.common.scheduler;

import java.util.Date;
import java.util.concurrent.Future;

/**
 * Описывает результат выполнения задачи в очереди.
 * @author Anton Sharapov
 */
public interface TaskDescriptor<V> extends Future<V> {

    /**
     * Идентификатор задачи.
     * @return  внутренний идентфикатор задачи.
     */
    public int getId();

    /**
     * Приоритет выполнения задачи.
     * @return  приоритет выполнения задачи.
     */
    public TaskPriority getPriority();

    /**
     * Аккаунт породивший данную задачу.
     * @return  строка с имененм аккаунта, от чьего имени выполняется данная задача.
     */
    public String getOwner();

    /**
     * Дата и время постановки задачи в очередь на выполнение.
     * @return  дата и время постановки задачи в очередь. Никогда не может быть <code>null</code>.
     */
    public Date getEnqueueTime();

    /**
     * Дата и время начала обработки задачи.
     * @return  дата и время начала обработки задачи или <code>null</code> если задача пока только находится в очереди на обработку.
     */
    public Date getStartTime();

    /**
     * Дата и время завершения обработки задачи.
     * @return  дата и время завершения обработки задачи или <code>null</code> если задача пока только выполняется или находится в очереди на обработку.
     */
    public Date getFinishTime();

    /**
     * Возвращает текущее состояние обработки задачи.
     * @return  текущее состояние обработки задачи.
     */
    public TaskState getState();

    /**
     * Ссылка на собственно задачу которая ставилась в очередь.
     * @return  задача.
     */
    public AbstractTask<V> getTask();

    /**
     * Возвращает слепок информации о текущем состоянии выполнения задачи.
     * @return  копия информация о текущем состоянии задачи. Со временем не изменяется.
     */
    public TaskSnapshot makeSnapshot();
}
