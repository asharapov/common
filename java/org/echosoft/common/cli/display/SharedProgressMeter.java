package org.echosoft.common.cli.display;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Используется для демонстрации прогресса при выполнении консольных программ когда информацию о прогрессе требуется обновлять из разных потоков.
 *
 * @author Anton Sharapov
 */
public class SharedProgressMeter extends ProgressMeter {

    public SharedProgressMeter(final Appendable out, final int hitsPerDot) {
        super(out, hitsPerDot);
    }

    public SharedProgressMeter(final Appendable out, final int hitsPerDot, final int dotsPerLine, final TimeUnit timeUnit) {
        super(out, hitsPerDot, dotsPerLine, timeUnit);
    }

    @Override
    public synchronized void start() throws IOException {
        super.start();
    }

    @Override
    public synchronized void hit(final boolean success) throws IOException {
        super.hit(success);
    }

    @Override
    public synchronized void complete() throws IOException {
        super.complete();
    }
}
