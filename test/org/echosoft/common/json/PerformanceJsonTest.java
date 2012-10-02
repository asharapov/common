package org.echosoft.common.json;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;

/**
 * @author Anton Sharapov
 */
public class PerformanceJsonTest {

    private static final Writer NULL_WRITER =
            new Writer() {
                public int counter;     // используем на всякий случай чтобы JIT не помножил все наши тесты на 0.
                @Override
                public void write(final int c) {
                    counter++;
                }
                @Override
                public void write(final char[] cbuf, final int off, final int len) {
                    counter++;
                }
                @Override
                public void write(final char cbuf[]) {
                    counter++;
                }
                @Override
                public void write(final String str) {
                    counter++;
                }
                @Override
                public void write(final String str, final int off, final int len) {
                    counter++;
                }
                @Override
                public void flush() {
                }
                @Override
                public void close() {
                }
                @Override
                public String toString() {
                    return "[NullWriter{" + counter + "}]";
                }
            };


    private static final BigDecimal BD_VALUE = new BigDecimal("3.1415926");
    private static final Double DB_VALUE = new Double("3.1415926");

    public static void main(final String[] args) throws Exception {
        System.out.println("warming up...");
        testDouble(50000);
        testBigDecimal(50000);
        Thread.sleep(5000L);

        System.out.println("start tests ...");
        for (int i = 0; i < 5; i++) {
            System.out.println("DOUBLE: " + testDouble(50000));
            System.out.println("BIGDEC: " + testBigDecimal(50000));
            System.out.println();
        }

        System.out.println(NULL_WRITER);
    }


    private static long testDouble(final int cnt) throws IOException {
        final long started = System.currentTimeMillis();
        for (int i = 0; i < cnt; i++) {
            NULL_WRITER.write(DB_VALUE.toString());
        }
        return System.currentTimeMillis() - started;
    }

    private static long testBigDecimal(final int cnt) throws IOException {
        final long started = System.currentTimeMillis();
        for (int i = 0; i < cnt; i++) {
            NULL_WRITER.write(BD_VALUE.toString());
        }
        return System.currentTimeMillis() - started;
    }
}
