package org.echosoft.common.io;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class FastStringWriterTest {

    @Test
    public void test() {
        final FastStringWriter out = new FastStringWriter(4);
        out.write("abc");
        out.write("de");
        out.write("fghijklmnopqrst");
        Assert.assertEquals("abcdefghijklmnopqrst", out.toString());
    }
}
