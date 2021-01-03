package org.echosoft.common.scheduler;

import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * DelayQueue is a concrete implementation of the BlockingQueue interface. Items added to a DelayQueue must implement
 * the new Delayed interface, which has a single method: long getDelay(TimeUnit unit). DelayQueue works as a time-based
 * scheduling queue that is backed by a priority heap data structure. In other words, when you add an element to the
 * queue, you specify how long the queue must wait before the element can be processed. To demonstrate, the following
 * program, DelayTest, provides an implementation of the Delayed interface that works in seconds. The key things to know
 * are (1) a nanosecond is a billionth of a second, and (2) there is a new method of System called nanoTime that allows
 * you to work in nanosecond units. Working in nanoseconds is important because the getDelay method wants the number of
 * nanoseconds returned.
 * 
 * Example taken from http://java.sun.com/developer/JDCTechTips/2004/tt1019.html#1
 */
public class DelayedQueueTest {
    public static long BILLION = 1000000000;


    static final class SecondsDelayed implements Delayed {
        private final String name;
        private long trigger;
        public SecondsDelayed(final String name, final long i) {
            this.name = name;
            trigger = System.nanoTime() + (i * BILLION);
        }

        public long getDelay(final TimeUnit unit) {
            final long n = trigger - System.nanoTime();
            return unit.convert(n, TimeUnit.NANOSECONDS);
        }

        public int compareTo(final Delayed d) {
            final long i = trigger;
            final long j = ((SecondsDelayed) d).trigger;
            final int returnValue;
            if (i < j) {
                returnValue = -1;
            } else
            if (i > j) {
                returnValue = 1;
            } else {
                returnValue = 0;
            }
            return returnValue;
        }

        public boolean equals(final Object other) {
            return ((SecondsDelayed) other).trigger == trigger;
        }


        public long getTriggerTime() {
            return trigger;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name + " / " + String.valueOf(trigger);
        }
    }


    public static void main(final String args[]) throws InterruptedException {
        final Random random = new Random();
        final DelayQueue<SecondsDelayed> queue = new DelayQueue<SecondsDelayed>();
        for (int i = 0; i < 10; i++) {
            int delay = random.nextInt(10);
            System.out.println("Delaying: " + delay + " seconds for loop " + i);
            queue.add(new SecondsDelayed("loop " + i, delay));
        }
        long last = 0;
        for (int i = 0; i < 10; i++) {
            SecondsDelayed delay = (SecondsDelayed) (queue.take());
            String name = delay.getName();
            long tt = delay.getTriggerTime();
            if (i != 0) {
                System.out.println("Delta: " + (tt - last) / (double) BILLION);
            }
            System.out.println(name + " / Trigger time: " + tt);
            last = tt;
        }
    }
}
