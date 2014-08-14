package org.echosoft.common.cli.display;

import java.io.IOException;

/**
 * Используется для демонстрации прогресса при выполнении консольных программ когда информацию о прогрессе требуется обновлять из разных потоков.
 *
 * @author Anton Sharapov
 */
public class SynchronizedProgressMeter extends ProgressMeter {

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
