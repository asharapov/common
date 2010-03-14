package org.echosoft.common.scheduler;

import java.util.concurrent.Callable;

import org.echosoft.common.utils.StringUtil;


/**
 * Базовый класс от которого наследуются все задачи.
 * 
 * @author Anton Sharapov
 */
public abstract class AbstractTask<V> implements Callable<V> {

    /**
     * Некоторая исчерпывающая информация о задаче, позволяющая однозначно определять дубликаты данной задачи.
     */
    private final Object key;

    /**
     * Категория задачи.
     */
    private final String category;

    /**
     * Название задачи.
     */
    private final String title;

    /**
     * Строка статуса, содержащая краткое описание текущего состояния задачи.
     */
    private volatile String statusLine;

    /**
     * Процент завершенности задачи
     * (изменяется в диапазоне 0..100).
     */
    private volatile int progress;

    public AbstractTask(Object key, String category, String title) {
        this.key = key;
        this.category = category;
        this.title = title;
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
     * Возвращает строку текущего статуса выполнения данной задачи.
     * @return  строка статуса выполнения задачи.
     */
    public String getStatusLine() {
        return statusLine;
    }

    /**
     * Указывает текущий статус выполнения данной задачи.
     * В процессе выполнения задачи может неоднократно изменяться.
     * @param statusLine строка с текущим статусом задачи.
     */
    public void setStatusLine(final String statusLine) {
        this.statusLine = statusLine;
    }

    /**
     * Возвращает прогресс выполнения задачи в процентах.
     * В процессе выполнения задачи может неоднократно изменяться.
     * @return  прогресс выполнения задачи в диапазоне 0..100
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Указывает прогресс выполнения задачи.
     * @param progress  число в диапазоне 0..100
     */
    public void setProgress(final int progress) {
        if (progress<0) {
            this.progress = 0;
        } else
        if (progress>100) {
            this.progress = 100;
        } else {
            this.progress = progress;
        }
    }

    @Override
    public int hashCode() {
        return key!=null ? key.hashCode() : 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final AbstractTask other = (AbstractTask)obj;
        return (key!=null ? key.equals(other.key) : other.key ==null);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(100);
        buf.append("[");
        buf.append(StringUtil.extractClass(getClass().getName()));
        buf.append("{key:");
        buf.append(key);
        buf.append(", category:");
        buf.append(category);
        buf.append(", title:");
        buf.append(title);
        buf.append("]}");
        return buf.toString();
    }
}
