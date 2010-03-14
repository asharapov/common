package org.echosoft.common.scheduler;

import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Anton Sharapov
 */
public class SchedTest {

    public static void main(final String args[]) throws Exception {
        testScheduler();
    }

    private static void dump(final PriorityThreadPoolExecutor executor) {
        final TreeSet<TaskDescriptor> stamp = new TreeSet<TaskDescriptor>(PriorityThreadPoolExecutor.PROCESSING_TASKS_COMPARATOR);
        executor.collectEnqueuedTasks(stamp);
        executor.collectProcessingTasks(stamp);
        System.err.println("processing:---------------------");
        for (TaskDescriptor ts : stamp) {
            System.err.println(ts);
        }
        System.err.println("--------------------------------");
        final TreeSet<TaskSnapshot> snapshots = new TreeSet<TaskSnapshot>(PriorityThreadPoolExecutor.PROCESSED_TASKS_COMPARATOR);
        executor.collectProcessedTasks(snapshots);
        System.err.println("processed:----------------------");
        for (TaskSnapshot ts : snapshots) {
            System.err.println(ts);
        }
        System.err.println("--------------------------------");
    }

    private static void testPTPE() throws Exception {
        final PriorityThreadPoolExecutor executor = new PriorityThreadPoolExecutor(2, 10);
        executor.submit( new Task1('C', "c1") );
        executor.submit( new Task1('C', "c2") );
        final PriorityThreadPoolExecutor.FutureTaskWrapper f1 = executor.addTask( new Task1(1, "r1"), TaskPriority.LOWEST );
        final PriorityThreadPoolExecutor.FutureTaskWrapper f2 = executor.addTask( new Task1(2, "r2"), TaskPriority.MEDIUM );
        final PriorityThreadPoolExecutor.FutureTaskWrapper f3 = executor.addTask( new Task1(3, "r3"), TaskPriority.LOW );
        final PriorityThreadPoolExecutor.FutureTaskWrapper f4 = executor.addTask( new Task1(4, "r4"), TaskPriority.HIGH );
        final PriorityThreadPoolExecutor.FutureTaskWrapper f5 = executor.addTask( new Task1(5, "r5"), TaskPriority.MEDIUM );
        final PriorityThreadPoolExecutor.FutureTaskWrapper f51 = executor.addTask( new Task1(5, "r5"), TaskPriority.MEDIUM );
        final PriorityThreadPoolExecutor.FutureTaskWrapper f6 = executor.addTask( new Task1(6, "r6"), TaskPriority.HIGHEST );
        final PriorityThreadPoolExecutor.FutureTaskWrapper f61 = executor.addTask( new Task1(6, "r6"), TaskPriority.HIGHEST );
        final Future c1 = executor.submit( new Task1('C', "c3") );
        dump(executor);
        System.out.println(f4+"\t\t"+f4.get());
        dump(executor);
        executor.shutdown();
        executor.shutdownNow();
        boolean t = executor.awaitTermination(1, TimeUnit.SECONDS);
        System.out.println("await termination: "+t);
    }

    private static void testScheduler() throws Exception {
        final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(2);
        ScheduledFuture f1 = scheduler.scheduleAtFixedRate(new Service1(1, "service 1"), 1, 2, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new Service1(2, "service 2"), 1, 2, TimeUnit.SECONDS);
        System.out.println(scheduler.getQueue());
    }


    private static final class Task1 extends AbstractTask<Integer> {
        public Task1(final Object key, final String title) {
            super(key, "report", title);
        }
        public Integer call() {
            try {
                System.out.println("task "+this.getTitle()+" started");
                synchronized (SchedTest.class) {
                    SchedTest.class.wait(10000);
                }
                System.out.println("task "+this.getTitle()+" completed");
                return 0;
            } catch (InterruptedException e) {
                System.out.println("task "+this.getTitle()+" interrupted");
                return -1;
            }
        }
    }

    private static final class Service1 implements Runnable {
        private final Object key;
        private final String title;
        public Service1(final Object key, final String title) {
            this.key = key;
            this.title = title;
        }
        public void run() {
            try {
                System.out.println("task "+this.title+" started");
                synchronized (SchedTest.class) {
                    SchedTest.class.wait(10000);
                }
                System.out.println("task "+this.title+" completed");
            } catch (InterruptedException e) {
                System.out.println("task "+this.title+" interrupted");
            }
        }
    }
}
