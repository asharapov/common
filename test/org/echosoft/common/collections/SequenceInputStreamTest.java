package org.echosoft.common.collections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.echosoft.common.utils.StreamUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class SequenceInputStreamTest {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final Map<Integer, InputStream> awaitingForClose = new TreeMap<Integer, InputStream>();

    @Before
    public void setUp() {
        counter.set(0);
        awaitingForClose.clear();
    }

    @Test
    public void testEmpty() throws IOException {
        for (int cnt = 0; cnt < 20; cnt++) {
            final InputStream[] streams = new InputStream[cnt];
            for (int i = 0; i < streams.length; i++) {
                streams[i] = new MockInputStream(new byte[0]);
            }
            final InputStream complexStream = new SequenceInputStream(streams);
            Assert.assertTrue(complexStream.read() == -1);
        }
        Assert.assertTrue(awaitingForClose.isEmpty());
    }

    @Test
    public void testReadsByOne() throws IOException {
        for (int cnt = 0; cnt < 20; cnt++) {
            final InputStream[] streams = new InputStream[cnt];
            int expectedLength = 0;
            for (int i = 0; i < streams.length; i++) {
                expectedLength += i;
                final byte[] buf = new byte[i];
                Arrays.fill(buf, (byte) i);
                streams[i] = new MockInputStream(buf);
            }

            final byte[] expectedData = new byte[expectedLength];
            int p = 0;
            for (int i = 0; i < streams.length; i++) {
                for (int j = 0; j < i; j++) {
                    expectedData[p++] = (byte) i;
                }
            }
            final InputStream complexStream = new SequenceInputStream(streams);
            final byte[] actualData = StreamUtil.streamToBytes(complexStream);
            Assert.assertArrayEquals(expectedData, actualData);
        }
        Assert.assertTrue(awaitingForClose.isEmpty());
    }

    @Test
    public void testReadsBuffered() throws IOException {
        for (int cnt = 0; cnt < 20; cnt++) {
            final InputStream[] streams = new InputStream[cnt];
            int expectedLength = 0;
            for (int i = 0; i < streams.length; i++) {
                expectedLength += i;
                final byte[] buf = new byte[i];
                Arrays.fill(buf, (byte) i);
                streams[i] = new MockInputStream(buf);
            }

            final byte[] expectedData = new byte[expectedLength];
            int p = 0;
            for (int i = 0; i < streams.length; i++) {
                for (int j = 0; j < i; j++) {
                    expectedData[p++] = (byte) i;
                }
            }
            final InputStream complexStream = new SequenceInputStream(streams);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamUtil.pipeData(complexStream, out, 4);
            final byte[] actualData = out.toByteArray();
            Assert.assertArrayEquals(expectedData, actualData);
        }
        Assert.assertTrue(awaitingForClose.isEmpty());
    }

    @Test
    public void testClose() throws IOException {
        final InputStream[] streams = new InputStream[10];
        for (int i = 0; i < streams.length; i++) {
            final byte[] buf = new byte[i];
            Arrays.fill(buf, (byte)i);
            streams[i] = new MockInputStream(buf);
        }
        final InputStream complexStream = new SequenceInputStream(streams);
        Assert.assertTrue(awaitingForClose.size() == streams.length);
        complexStream.close();
        Assert.assertTrue(awaitingForClose.isEmpty());
    }



    public static class MockInputStream extends ByteArrayInputStream {

        private final int id;
        public MockInputStream(final byte[] buf) {
            super(buf);
            this.id = counter.incrementAndGet();
            awaitingForClose.put(id, this);
        }

        public void close() {
            awaitingForClose.remove(id);
            System.out.println("closed: " + id);
        }
    }
}
