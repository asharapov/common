package org.echosoft.common.collections;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.echosoft.common.io.SequenceReader;
import org.echosoft.common.utils.StreamUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class SequenceReaderTest {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final Map<Integer, Reader> awaitingForClose = new TreeMap<Integer, Reader>();

    @Before
    public void setUp() {
        counter.set(0);
        awaitingForClose.clear();
    }

    @Test
    public void testEmpty() throws IOException {
        for (int cnt = 0; cnt < 20; cnt++) {
            final Reader[] streams = new Reader[cnt];
            for (int i = 0; i < streams.length; i++) {
                streams[i] = new MockReader(new char[0]);
            }
            final Reader complexReader = new SequenceReader(streams);
            Assert.assertTrue(complexReader.read() == -1);
        }
        Assert.assertTrue(awaitingForClose.isEmpty());
    }

    @Test
    public void testReadsByOne() throws IOException {
        for (int cnt = 0; cnt < 20; cnt++) {
            final Reader[] streams = new Reader[cnt];
            int expectedLength = 0;
            for (int i = 0; i < streams.length; i++) {
                expectedLength += i;
                final char[] buf = new char[i];
                Arrays.fill(buf, (char)((int)'0' + i));
                streams[i] = new MockReader(buf);
            }

            final char[] expectedData = new char[expectedLength];
            int p = 0;
            for (int i = 0; i < streams.length; i++) {
                for (int j = 0; j < i; j++) {
                    expectedData[p++] = (char)((int)'0' + i);
                }
            }
            final Reader complexReader = new SequenceReader(streams);
            final char[] actualData = readFully(complexReader);
            Assert.assertArrayEquals(expectedData, actualData);
        }
        Assert.assertTrue(awaitingForClose.isEmpty());
    }

    @Test
    public void testReadsBuffered() throws IOException {
        for (int cnt = 0; cnt < 20; cnt++) {
            final Reader[] streams = new Reader[cnt];
            int expectedLength = 0;
            for (int i = 0; i < streams.length; i++) {
                expectedLength += i;
                final char[] buf = new char[i];
                Arrays.fill(buf, (char)((int)'0' + i));
                streams[i] = new MockReader(buf);
            }

            final char[] expectedData = new char[expectedLength];
            int p = 0;
            for (int i = 0; i < streams.length; i++) {
                for (int j = 0; j < i; j++) {
                    expectedData[p++] = (char)((int)'0' + i);
                }
            }
            final Reader complexReader = new SequenceReader(streams);
            final char[] actualData = readFully(complexReader);
            Assert.assertArrayEquals(expectedData, actualData);
        }
        Assert.assertTrue(awaitingForClose.isEmpty());
    }

    @Test
    public void testClose() throws IOException {
        final Reader[] streams = new Reader[10];
        for (int i = 0; i < streams.length; i++) {
            final char[] buf = new char[i];
            Arrays.fill(buf, (char)((int)'0' + i));
            streams[i] = new MockReader(buf);
        }
        final Reader complexReader = new SequenceReader(streams);
        Assert.assertTrue(awaitingForClose.size() == streams.length);
        complexReader.close();
        Assert.assertTrue(awaitingForClose.isEmpty());
    }



    public static class MockReader extends CharArrayReader {

        private final int id;
        public MockReader(final char[] buf) {
            super(buf);
            this.id = counter.incrementAndGet();
            awaitingForClose.put(id, this);
        }

        public void close() {
            awaitingForClose.remove(id);
            System.out.println("closed: " + id);
        }
    }



    private static char[] readFully(final Reader reader) throws IOException {
        final StringWriter writer = new StringWriter();
        StreamUtil.pipeData(reader, writer);
        return writer.getBuffer().toString().toCharArray();
    }
}
