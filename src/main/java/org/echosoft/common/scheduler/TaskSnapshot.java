package org.echosoft.common.scheduler;

import java.io.Serializable;
import java.util.Date;

import org.echosoft.common.utils.StringUtil;


/**
 * Информация о состоянии задачи на конкретный момент времени.
 * 
 * @author Anton Sharapov
 */
public class TaskSnapshot implements Serializable {

    private final int id;
    private final TaskPriority priority;
    private final String owner;
    private final Object key;
    private final String category;
    private final String title;
    private final Date enqueueTime;
    private final Date startTime;
    private final Date finishTime;
    private final TaskState state;
    private final String statusLine;
    private final int progress;

    public TaskSnapshot(int id, TaskPriority priority, String owner, Object key, String category, String title,
                        Date enqueueTime, Date startTime, Date finishTime, TaskState state,
                        String statusLine, int progress) {
        this.id = id;
        this.priority = priority;
        this.owner = owner;
        this.key = key;
        this.category  = category;
        this.title = title;
        this.enqueueTime = enqueueTime;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.state = state;
        this.statusLine = statusLine;
        this.progress = progress;
    }

    /**
     * Идентификатор задачи.
     * @return  внутренний идентфикатор задачи.
     */
    public int getId() {
        return id;
    }

    /**
     * Приоритет выполнения задачи.
     * @return  приоритет выполнения задачи.
     */
    public TaskPriority getPriority() {
        return priority;
    }

    /**
     * Аккаунт породивший данную задачу.
     * @return  строка с имененм аккаунта, от чьего имени выполняется данная задача.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Иногда при постановке в очередь на выполнение очередной задачи требуется убедиться что точно такая же задача не находится уже в очереди на выполнение.
     * Именно для решения подобной задачи используется данное свойство.
     * Для каждой уникальной задачи в системе должно выполняться правило <code> task1.key!=null && task2.key!=null && !task1.key.equals(task2.key) </code>
     * @return  некая произвольная дополнительная информация о задаче. Никогда не может быть <code>null</code>.
     */
    public Object getKey() {
        return key;
    }

    /**
     * Категория задачи.
     * @return  категория задачи.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Название данной задачи.
     * @return название задачи.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Дата и время постановки задачи в очередь на выполнение.
     * @return  дата и время постановки задачи в очередь. Никогда не может быть <code>null</code>.
     */
    public Date getEnqueueTime() {
        return enqueueTime;
    }

    /**
     * Дата и время начала обработки задачи.
     * @return  дата и время начала обработки задачи или <code>null</code> если задача пока только находится в очереди на обработку.
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Дата и время завершения обработки задачи.
     * @return  дата и время завершения обработки задачи или <code>null</code> если задача пока только выполняется или находится в очереди на обработку.
     */
    public Date getFinishTime() {
        return finishTime;
    }

    /**
     * Возвращает текущее состояние обработки задачи.
     * @return  текущее состояние обработки задачи.
     */
    public TaskState getState() {
        return state;
    }

    /**
     * Возвращает строку текущего статуса выполнения данной задачи.
     * @return  строка статуса выполнения задачи.
     */
    public String getStatusLine() {
        return statusLine;
    }

    /**
     * Возвращает прогресс выполнения задачи в процентах.
     * @return  прогресс выполнения задачи в диапазоне 0..100
     */
    public int getProgress() {
        return progress;
    }


    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final TaskSnapshot other = (TaskSnapshot)obj;
        return  id==other.id &&
                (key !=null ? key.equals(other.key) : other.key ==null);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(100);
        buf.append("[");
        buf.append(StringUtil.extractClass(getClass().getName()));
        buf.append("{id:");
        buf.append(id);
        buf.append(", owner:");
        buf.append(owner);
        buf.append(", category:");
        buf.append(category);
        buf.append(", title:");
        buf.append(title);
        buf.append(", state:");
        buf.append(state);
        buf.append(", priority:");
        buf.append(priority);
        buf.append("]}");
        return buf.toString();
    }
}
