package org.echosoft.common.io;

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

    private static final String TEST3 = "\"test1\",\"test2\",\"\", \"test3 \"fg,gf\"test4,4\" \"dfg,\"qqq\"fgf";
    private static final String[] EXPECTED3 = {"test1", "test2", "", "test3 ", "test4,4", "qqq"};


    @Test
    public void testSimpleTokens1() {
        testTokens(TEST1, EXPECTED1, ',', (char)0);
    }

    @Test
    public void testSimpleTokens2() {
        testTokens(TEST2, EXPECTED2, ',', (char)0);
    }

    @Test
    public void testWrappedTokens1() {
        testTokens(TEST3, EXPECTED3, ',', '"');
    }

    protected void testTokens(String text, String[] result, char delimiter, char wrapper) {
        final FastStringTokenizer tokenizer = new FastStringTokenizer(text, delimiter, wrapper);
        for (String expected : result) {
            Assert.assertTrue("tokenizer hasn't expected token: " + expected, tokenizer.hasNext());
            final String token = tokenizer.next();
            Assert.assertEquals("parsed token doesn't equals expected", expected, token);
        }
        Assert.assertFalse("tokenizer has redundant tokens", tokenizer.hasNext());
    }
}
