package org.echosoft.common.cli;

import java.util.concurrent.TimeUnit;

import org.echosoft.common.cli.display.ProgressMeter;

/**
 * @author Anton Sharapov
 */
public class ProgressMeterTest {

    public static void main(String[] args) throws Exception {

        final ProgressMeter meter1 = new ProgressMeter(System.out, 10, 50, null).applyLegend("items");
        meter1.start();
        for (int i = 0; i < 1333; i++) {
            final boolean ok = i % 66 != 0;
            meter1.hit(ok);
            //Thread.sleep(10);
        }
        meter1.complete();
        System.out.println("==================================================");
        System.out.println("total hits: " + meter1.getTotalHitsCount());
        System.out.println("errors:     " + meter1.getTotalErrorsCount());
        System.out.println("total time: " + (meter1.getTotalTime()) + " ms.");
    }
}
