package org.echosoft.common.io;

import java.io.StringReader;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class CSVParserTest {

    private static final String TEST1 =
            "a,b,c,\r\n" +
            "d,e,f,g\r\n" +
            "\r\n" +
            "\n" +
            " \n" +
            "h,,j,k, ";
    private static final String[][] TEST1_EXPECTED = {
            {"a", "b", "c", ""},
            {"d", "e", "f", "g"},
            {},
            {},
            {" "},
            {"h", "",  "j", "k", " "}
    };


    private static final String TEST2 =
            "\"\", \"b\"\"b\" , \"c\",\r\n" +
            "\"d\",\"e\",\"f\",\"g\"\r\n" +
            "\"h\r\nh\",\"\",\"j\",\"k\", \n" +
            "\r\n" +
            "\r\n" +
            " \r\n" +
            "  \"complex text\" , \"complex test \"\"2\"\"\"\n";
    private static final String[][] TEST2_EXPECTED = {
            {"", "b\"b", "c", null},
            {"d", "e", "f", "g"},
            {"h\r\nh", "",  "j", "k", null},
            {},
            {},
            {null},
            {"complex text", "complex test \"2\""}

    };


    @Test
    public void test1a() throws Exception {
        final CSVParser parser = new CSVParser(new StringReader(TEST1), ',', false);
        try {
            while (parser.nextLine()) {
                final int lineNum = parser.getLineNum();
                while (parser.hasNextToken()) {
                    final String token = parser.nextToken();
                    final int tokenNum = parser.getLastTokenNum();
                    final String expected = TEST1_EXPECTED[lineNum - 1][tokenNum - 1];
                    Assert.assertEquals(expected, token);
                    //System.out.println(lineNum + "." + tokenNum + ":\t" + token);
                }
            }
        } finally {
            parser.close();
        }
    }

    @Test
    public void test1b() throws Exception {
        final CSVParser parser = new CSVParser(new StringReader(TEST1), ',', false);
        try {
            parser.nextLine();
            while (parser.nextLine()) {
                final int lineNum = parser.getLineNum();
                while (parser.hasNextToken()) {
                    final String token = parser.nextToken();
                    final int tokenNum = parser.getLastTokenNum();
                    final String expected = TEST1_EXPECTED[lineNum - 1][tokenNum - 1];
                    Assert.assertEquals(expected, token);
                    System.out.println(lineNum + "." + tokenNum + ":\t" + token);
                }
            }
        } finally {
            parser.close();
        }
    }

    @Test
    public void test2a() throws Exception {
        final CSVParser parser = new CSVParser(new StringReader(TEST2), ',', true);
        try {
            while (parser.nextLine()) {
                final int lineNum = parser.getLineNum();
                while (parser.hasNextToken()) {
                    final String token = parser.nextToken();
                    final int tokenNum = parser.getLastTokenNum();
                    final String expected = TEST2_EXPECTED[lineNum - 1][tokenNum - 1];
                    Assert.assertEquals(expected, token);
                    //System.out.println(lineNum + "." + tokenNum + ":\t" + token);
                }
            }
        } finally {
            parser.close();
        }
    }

    @Test
    public void test2b() throws Exception {
        final CSVParser parser = new CSVParser(new StringReader(TEST2), ',', true);
        try {
            parser.nextLine();
            while (parser.nextLine()) {
                final int lineNum = parser.getLineNum();
                while (parser.hasNextToken()) {
                    final String token = parser.nextToken();
                    final int tokenNum = parser.getLastTokenNum();
                    final String expected = TEST2_EXPECTED[lineNum - 1][tokenNum - 1];
                    Assert.assertEquals(expected, token);
                    System.out.println(lineNum + "." + tokenNum + ":\t" + token);
                }
            }
        } finally {
            parser.close();
        }
    }
}
