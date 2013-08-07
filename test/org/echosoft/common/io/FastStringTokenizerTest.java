package org.echosoft.common.io;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class FastStringTokenizerTest {

    private static final String TEST1 = "test1,test2,,test3, test4 ,test5,,v,";
    private static final String[] EXPECTED1 = {"test1", "test2", "", "test3", " test4 ", "test5", "", "v"};

    private static final String TEST2 = "test1,test2,,test3, test4 ,test5,  ,v, ";
    private static final String[] EXPECTED2 = {"test1", "test2", "", "test3", " test4 ", "test5", "  ", "v", " "};


    @Test
    public void testSimpleTokens1() throws IOException {
        testTokens(TEST1, EXPECTED1, ',');
    }

    @Test
    public void testSimpleTokens2() throws IOException {
        testTokens(TEST2, EXPECTED2, ',');
    }

    protected void testTokens(String text, String[] result, char delimiter) {
        final FastStringTokenizer tokenizer = new FastStringTokenizer(text, delimiter);
        for (String expected : result) {
            Assert.assertTrue("tokenizer hasn't expected token: " + expected, tokenizer.hasNext());
            final String token = tokenizer.next();
            Assert.assertEquals("parsed token doesn't equals expected", expected, token);
        }
        Assert.assertFalse("tokenizer has redundant tokens", tokenizer.hasNext());
    }

}
