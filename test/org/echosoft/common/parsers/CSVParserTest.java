package org.echosoft.common.parsers;

import java.io.StringReader;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class CSVParserTest {

    private static final boolean VERBOSE = false;

    @Test
    public void test0() throws Exception {
        final CSVParser parser1 = new CSVParser(new StringReader(""));
        Assert.assertFalse(parser1.nextLine());

        final CSVParser parser2 = new CSVParser(new StringReader(" "));
        Assert.assertTrue(parser2.nextLine());
        Assert.assertTrue(parser2.hasNextToken());
        Assert.assertEquals("", parser2.nextToken());
        Assert.assertFalse(parser2.hasNextToken());
        Assert.assertFalse(parser2.nextLine());
    }

    @Test
    public void test1() throws Exception {
        final String src =
                "42, 5 ,\"A good token\",test ,a bad token";
        final CSVParser parser = new CSVParser(new StringReader(src));
        try {
            parser.hasNextToken();
            Assert.fail("Illegal parser state");
        } catch (IllegalStateException e) { /* all is ok. */ }
        Assert.assertTrue(parser.nextLine());
        Assert.assertTrue(parser.hasNextToken());
        Assert.assertEquals("42", parser.nextToken());
        Assert.assertTrue(parser.hasNextToken());
        Assert.assertEquals("5", parser.nextToken());
        Assert.assertTrue(parser.hasNextToken());
        Assert.assertEquals("A good token", parser.nextToken());
        Assert.assertTrue(parser.hasNextToken());
        Assert.assertEquals("test", parser.nextToken());
        try {
            parser.hasNextToken();
            Assert.fail("Illegal parser state");
        } catch (IllegalStateException e) { /* all is ok. */ }
    }

    @Test
    public void test2() throws Exception {
        final String src =
                "a,b,c,\r\n" +
                "d,e,f,g\r\n" +
                "\r\n" +
                "\n" +
                " \n" +
                "h,,j,k, ";
        final String[][] expectations = {
                {"a", "b", "c", ""},
                {"d", "e", "f", "g"},
                {},
                {},
                {""},
                {"h", "", "j", "k", ""}
        };
        testImpl(src, expectations, 0, VERBOSE);
    }

    @Test
    public void test3() throws Exception {
        final String src =
                "\"\", \"b\"\"b\" , \"c\",\r\n" +
                "\"d\",\"e\",\"f\",\"g\"\r\n" +
                "\"h\r\nh\",\"\",\"j\",\"k\", \n" +
                "\r\n" +
                "\r\n" +
                " \r\n" +
                "  \"complex text\" , \"complex test \"\"2\"\"\"\n";
        final String[][] expectations = {
                {"", "b\"b", "c", ""},
                {"d", "e", "f", "g"},
                {"h\r\nh", "",  "j", "k", ""},
                {},
                {},
                {""},
                {"complex text", "complex test \"2\""}
        };
        testImpl(src, expectations, 0, VERBOSE);
    }

    @Test
    public void test4() throws Exception {
        final String src =
                "This is a header \r\n" +
                "This is a header 2 \r\n" +
                ",\"\",1, 2 ,\" hello \", \"world\", 3\n" +
                ",, ,  2  , \" test \"  , \" word\", 5 ,\n" +
                " test , hello ,world";
        final String[][] expectations = {
                {},
                {},
                {"", "", "1", "2", " hello ", "world", "3"},
                {"", "", "", "2", " test ", " word", "5", ""},
                {"test", "hello", "world"}
        };
        testImpl(src, expectations, 2, VERBOSE);
    }

    @Test
    public void test5() throws Exception {
        final String src =
                "5, 1, 3, test, \"a quick brown fox\" , 3.14 \n" +
                "\n" +
                "\n" +
                ",,,\n";
        final String[][] expectations = {
                {"5", "1", "3", "test", "a quick brown fox", "3.14"},
                {},
                {},
                {"", "", "", ""}
        };
        testImpl(src, expectations, 0, VERBOSE);
    }


    private void testImpl(final String source, final String[][] expectations, final int skipLines, final boolean verbose) throws Exception {
        try (CSVParser parser = new CSVParser(new StringReader(source), ',')) {
            parser.nextLine(skipLines);
            while (parser.nextLine()) {
                final int lineNum = parser.getLineNum();
                Assert.assertTrue("too many lines at source: " + lineNum, expectations.length >= lineNum);
                final String[] expectedTokens = expectations[lineNum - 1];
                while (parser.hasNextToken()) {
                    final String token = parser.nextToken();
                    final int tokenNum = parser.getLastTokenNum();
                    Assert.assertTrue("too many tokens at source (line " + lineNum + "): " + tokenNum, expectedTokens.length >= tokenNum);
                    final String expected = expectedTokens[tokenNum - 1];
                    Assert.assertEquals("bad token " + '\'' + token + "\' at pos " + lineNum + "." + tokenNum, expected, token);
                    if (verbose)
                        System.out.println(lineNum + "." + tokenNum + ":\t" + '\'' + token + '\'');
                }
            }
        }
    }

}
