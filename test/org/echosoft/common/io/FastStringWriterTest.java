package org.echosoft.common.io;

import java.io.StringWriter;

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

    @Test
    public void test2() {
        final String text = "1234567890";
        for (int offset = 0; offset < text.length(); offset++) {
            for (int len = 0; len < text.length() - offset; len++) {
                final StringWriter buf1 = new StringWriter();
                buf1.write(text, offset, len);
                final FastStringWriter buf2 = new FastStringWriter();
                buf2.write(text, offset, len);
                Assert.assertEquals(buf1.toString(), buf2.toString());
                System.out.println(buf2.toString());
            }
        }
    }

    @Test
    public void test3() {
        final char[] text = "1234567890".toCharArray();
        for (int offset = 0; offset < text.length; offset++) {
            for (int len = 0; len < text.length - offset; len++) {
                final StringWriter buf1 = new StringWriter();
                buf1.write(text, offset, len);
                final FastStringWriter buf2 = new FastStringWriter();
                buf2.write(text, offset, len);
                Assert.assertEquals(buf1.toString(), buf2.toString());
                System.out.println(buf2.toString());
            }
        }
    }
}
