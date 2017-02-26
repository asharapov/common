package org.echosoft.common.utils;

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class Base64UtilTest {

    @Test
    public void boundaryTest() {
        final byte[][] testcases = {
            makeArray(0, 0),
            makeArray(1, 0),
            makeArray(2, 0),
            makeArray(1, 127),
            makeArray(1, 128),
            makeArray(1, 255),
            makeArray(2, 127),
            makeArray(2, 128),
            makeArray(2, 255),
            makeArray(3, 127),
            makeArray(3, 128),
            makeArray(3, 255),
        };
        for (byte[] data : testcases) {
            final String encstr = Base64Util.encode(data);
            final byte[] decoded = Base64Util.decode(encstr);
            Assert.assertArrayEquals(data, decoded);
        }
    }
    private static byte[] makeArray(int length, int value) {
        final byte[] result = new byte[length];
        Arrays.fill(result, (byte)value);
        return result;
    }

    @Test
    public void randomTest() {
        final Random rnd = new Random();
        final int cnt = 10;
        final int maxSize = 1024;
        for (int i=0; i<cnt; i++) {
            for (int j=0; j<maxSize; j++) {
                final byte[] data = new byte[j];
                rnd.nextBytes(data);
                final String encstr = Base64Util.encode(data);
                final byte[] decoded = Base64Util.decode(encstr);
                Assert.assertArrayEquals(data, decoded);
            }
        }

    }
}
