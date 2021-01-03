package org.echosoft.common.scheduler;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.echosoft.common.utils.StringUtil;

/**
 * Планировщик для задач с различными приоритетами выполнения.
 *
 * @author Anton Sharapov
 */
public class PriorityThreadPoolExecutor extends ThreadPoolExecutor {

    public static final Comparator<TaskDescriptor> PROCESSING_TASKS_COMPARATOR =
            new Comparator<TaskDescriptor>() {
                public int compare(final TaskDescriptor t1, final TaskDescriptor t2) {
                    if (t1.getId()==t2.getId())
                        return 0;
                    int result = t1.getState().compareTo(t2.getState());
                    if (result==0) {
                        result = t1.getPriority().compareTo(t2.getPriority());
                        if (result==0) {
                            result = t1.getId()>t2.getId() ? 1 : -1;
                        }
                    }
                    return result;
                }
            };
    public static final Comparator<TaskSnapshot> PROCESSED_TASKS_COMPARATOR =
            new Comparator<TaskSnapshot>() {
                public int compare(final TaskSnapshot s1, final TaskSnapshot s2) {
                    int result = s1.getFinishTime().compareTo(s2.getFinishTime());
                    if (result==0) {
                        result = s1.getId()>s2.getId() ? 1 : (s1.getId()<s2.getId() ? -1 : 0);
                    }
                    return result;
                }
            };


    private static final Comparator<FutureTaskWrapper> QUEUE_COMPARATOR =
            new Comparator<FutureTaskWrapper>() {
                public int compare(final FutureTaskWrapper t1, final FutureTaskWrapper t2) {
                    if (t1.id==t2.id)
                        return 0;
                    int result = t1.priority.compareTo(t2.priority);
                    if (result==0) {
                        result = t1.id>t2.id ? 1 : -1;
                    }
                    return result;
                }
            };
    private static final Object present = new Object();

    private final AtomicInteger counter;
    private final ConcurrentHashMap<FutureTaskWrapper,Object> processingTasks;
    private final LinkedBlockingDeque<TaskSnapshot> processedTasks;

    /**
     * Создает и инициализирует планировщик задач.
     * @param poolSize  максимальное количество одновременно выполняющихся задач.
     * @param historyLimit  определяет максимальное количество выполненых задач информация о которых будет храниться в соответствующем буфере планировщика.
     */
    @SuppressWarnings("unchecked")
    public PriorityThreadPoolExecutor(int poolSize, int historyLimit) {
        super(poolSize, poolSize, 0, TimeUnit.MILLISECONDS, new PriorityBlockingQueue(11,QUEUE_COMPARATOR) );
        counter = new AtomicInteger(0);
        processingTasks = new ConcurrentHashMap<FutureTaskWrapper,Object>();
        processedTasks = new LinkedBlockingDeque<TaskSnapshot>(historyLimit);
    }

    /**
     * Копирует в указанный буфер снапшоты всех задач которые стоят в данный момент в очереди на выполнение.
     * @param buf  коллекция в которую будут помещены срезы состояния всех задач которые в настоящий момент стоят в очереди на выполнение.
     */
    public void collectEnqueuedTasks(final Collection<TaskDescriptor> buf) {
        for (Runnable r : getQueue()) {
            final FutureTaskWrapper future = (FutureTaskWrapper)r;
            buf.add( future );
        }
    }

    /**
     * Копирует в указанный буфер снапшоты всех задач которые выполняются в настоящий момент.
     * @param buf  коллекция в которую будут помещены срезы состояния всех задач которые выполняются в настоящий момент.
     */
    public void collectProcessingTasks(final Collection<TaskDescriptor> buf) {
        for (FutureTaskWrapper future : processingTasks.keySet()) {
            buf.add( future );
        }
    }

    /**
     * Копирует в указанный буфер снапшоты N последних выполнененных задач.
     * @param buf  коллекция в которую будут помещены срезы состояния последних выполненных задач.
     */
    public void collectProcessedTasks(final Collection<TaskSnapshot> buf) {
        buf.addAll( processedTasks );
    }

    /**
     * Ставит в очередь на обработку очередную задачу которая должна выполняться с указанным приоритетом.
     * @param task  новая задача. Не может быть <code>null</code>.
     * @param priority  приоритет, с которым должна выполняться данная задача. Значение по умолчанию - {@link TaskPriority#MEDIUM}.
     * @param <V>  тип возвращаемых задачей данных.
     * @return  экземпляр класса {@link FutureTaskWrapper} который позволит всегда быть в курсе текущего состояния данной задачи и управлять ее состоянием.
     */
    public <V> FutureTaskWrapper<V> addTask(final AbstractTask<V> task, final TaskPriority priority) {
        final FutureTaskWrapper<V> future = new FutureTaskWrapper<V>(task, priority, getCurrentUser());
        execute(future);
        return future;
    }

    /**
     * Ставит в очередь на обработку очередную задачу которая должна выполняться с указанным приоритетом и которой еще нет в очереди на обработку.
     * Если подобная задача уже обрабатывается в настоящий момент времени или только присутствует в очереди на обработку то новая подобная задача добавляться
     * в очередь уже не будет и метод вернет <code>null</code>.
     * @param task  новая задача. Не может быть <code>null</code>.
     * @param priority  приоритет, с которым должна выполняться данная задача. Значение по умолчанию - {@link TaskPriority#MEDIUM}.
     * @param <V>  тип возвращаемых задачей данных.
     * @return  экземпляр класса {@link FutureTaskWrapper} который позволит всегда быть в курсе текущего состояния данной задачи и управлять ее состоянием.
     */
    public <V> FutureTaskWrapper<V> addUniqueTask(final AbstractTask<V> task, final TaskPriority priority) {
        if (task==null)
            throw new NullPointerException();
        for (Runnable r : getQueue()) {
            final FutureTaskWrapper future = (FutureTaskWrapper)r;
            if (future.task.equals(task))
                return null;
        }
        final FutureTaskWrapper<V> future = new FutureTaskWrapper<V>(task, priority, getCurrentUser());
        execute(future);
        return future;
    }


    /**
     * Переопределение данного метода обеспечивает нам синхронизацию свойств обрабатываемых задач.
     * @param t  один из свободных потоков в пуле в котором сейчас будет выполняться очередная задача, взятая из очереди.
     * @param r  очередная задача которая будет сейчас выполняться в указанном потоке.
     */
    @Override
    protected void beforeExecute(final Thread t, final Runnable r) {
        super.beforeExecute(t, r);
        final FutureTaskWrapper future = (FutureTaskWrapper)r;
        future.startTime = new Date();
        future.state = TaskState.PROCESSING;
        processingTasks.put(future, present);
    }

    /**
     * Переопределение данного метода обеспечивает нам синхронизацию свойств обрабатываемых задач.
     * @param r  очередная завершенная задача.
     * @param t  данный аргумент равен <code>null</code> если задача была завершена без ошибок, иначе указывается ошибка из-за которой задача была принудительно прервана.
     */
    @Override
    protected void afterExecute(final Runnable r, final Throwable t) {
        final FutureTaskWrapper future = (FutureTaskWrapper)r;
        future.finishTime = new Date();
        future.state = t==null ? TaskState.COMPLETED : TaskState.FAILED;
        processingTasks.remove(future);
        final TaskSnapshot snapshot = future.makeSnapshot();
        while ( !processedTasks.offerLast(snapshot) ) {
            processedTasks.pollFirst();
        }
        super.afterExecute(r, t);
    }

    /**
     * Переопределение данного метода гарантирует нам что все задачи в очереди на исполнение в данном планировщике будут иметь
     * тип <tt>FutureTaskWrapper</tt> что мы можем использовать все методы определенные в классах являющихся предками
     * данного класса без каких-либо серьезных проблем.
     * @param callable  очередная задача которая должна быть помещена в очередь на исполнение.
     * @param <V>  определяет тип данных возвращаемых задачей по завершении ее выполнения.
     */
    @Override
    protected <V> RunnableFuture<V> newTaskFor(final Callable<V> callable) {
        return new FutureTaskWrapper<V>(callable, getCurrentUser());
    }

    /**
     * Переопределение данного метода гарантирует нам что все задачи в очереди на исполнение в данном планировщике будут иметь
     * тип <tt>FutureTaskWrapper</tt> что мы можем использовать все методы определенные в классах являющихся предками
     * данного класса без каких-либо серьезных проблем.
     * @param runnable  очередная задача которая должна быть помещена в очередь на исполнение.
     * @param value  объект используется для построения адаптора от интерфейса <tt>Runnable</tt> к интерфейсу <tt>Callable</tt>.
     * @param <V>  определяет тип данных возвращаемых задачей по завершении ее выполнения.
     */
    @Override
    protected <V> RunnableFuture<V> newTaskFor(final Runnable runnable, final V value) {
        return new FutureTaskWrapper<V>(runnable, value, getCurrentUser());
    }


    /**
     * <p>Возвращает пользователя системы с которым ассоциирован в настоящий момент текущий поток выполнения. Вызывается в процессе регистрации новой задачи в очереди на исполнение</p>
     * Предназначено для переопределения в классах-наследниках.
     * @return имя пользователя (аккаунт) от чьего имени выполняется код в текущем потоке.
     */
    protected String getCurrentUser() {
        return null;
    }


    /**
     * Содержит расширенную информацию о каждой помещенной в очередь на выполнение или выполняемой в настоящий момент задаче.
     * @param <V>  характеризует тип возвращаемых данных в методе <code>call()</code> задачи.
     */
    public final class FutureTaskWrapper<V> extends FutureTask<V> implements TaskDescriptor<V> {
        private final AbstractTask<V> task;     // исходный обработчик задачи.
        private final int id;                   // внутренний идентификатор задачи.
        private final TaskPriority priority;    // приоритет задачи.
        private final String owner;             // от какого пользователя выполняется.
        private final Date enqueueTime;         // время постановки в очередь.
        private volatile Date startTime;        // время начала фактической обработки задачи.
        private volatile Date finishTime;       // время завершения обработки задачи.
        private volatile TaskState state;       // текущее состояние задачи.

        private FutureTaskWrapper(final AbstractTask<V> task, final TaskPriority priority, final String owner) {
            super(task);
            this.task = task;
            this.id = counter.incrementAndGet();
            this.priority = priority!=null ? priority : TaskPriority.MEDIUM;
            this.owner = owner;
            this.enqueueTime = new Date();
            this.startTime = null;
            this.finishTime = null;
            this.state = TaskState.AWAITING;
        }

        private FutureTaskWrapper(final Callable<V> task, final String owner) {
            super(task);
            this.task = new CallableTaskAdapter<V>(task);
            this.id = counter.incrementAndGet();
            this.priority = TaskPriority.MEDIUM;
            this.owner = owner;
            this.enqueueTime = new Date();
            this.startTime = null;
            this.finishTime = null;
            this.state = TaskState.AWAITING;
        }

        private FutureTaskWrapper(final Runnable task, final V result, final String owner) {
            this( new RunnableTaskAdapter<V>(task, result), owner );
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public TaskPriority getPriority() {
            return priority;
        }

        @Override
        public String getOwner() {
            return owner;
        }

        @Override
        public Date getEnqueueTime() {
            return enqueueTime;
        }

        @Override
        public Date getStartTime() {
            return startTime;
        }

        @Override
        public Date getFinishTime() {
            return finishTime;
        }

        @Override
        public TaskState getState() {
            return state;
        }

        @Override
        public AbstractTask<V> getTask() {
            return task;
        }

        /**
         * Возвращает слепок информации о текущем состоянии выполнения задачи.
         * @return  копия информация о текущем состоянии задачи. Со временем не изменяется.
         */
        @Override
        public TaskSnapshot makeSnapshot() {
            return new TaskSnapshot(id, priority, owner, task.getKey(), task.getCategory(), task.getTitle(), 
                                    enqueueTime, startTime, finishTime, state,
                                    task.getStatusLine(), task.getProgress());
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj==null || !getClass().equals(obj.getClass()))
                return false;
            final FutureTaskWrapper other = (FutureTaskWrapper)obj;
            return  id==other.id && task.equals(other.task);
        }

        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder(100);
            buf.append("[");
            buf.append(StringUtil.extractClass(task.getClass().getName()));
            buf.append("{id:");
            buf.append(id);
            buf.append(", owner:");
            buf.append(owner);
            buf.append(", category:");
            buf.append(task.getCategory());
            buf.append(", title:");
            buf.append(task.getTitle());
            buf.append(", state:");
            buf.append(state);
            buf.append("]}");
            return buf.toString();
        }
    }

}
