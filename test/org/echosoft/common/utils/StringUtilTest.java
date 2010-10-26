package org.echosoft.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class StringUtilTest {

    @Test
    public void testLeadLeft() throws Exception {
        Assert.assertEquals("", StringUtil.leadLeft("",' ', 0));
        Assert.assertEquals("", StringUtil.leadLeft(null,' ', 0));
        Assert.assertEquals("abc", StringUtil.leadLeft("abc",' ', 2));
        Assert.assertEquals("abc", StringUtil.leadLeft("abc",' ', 3));
        Assert.assertEquals(" abc", StringUtil.leadLeft("abc",' ', 4));
        Assert.assertEquals("  ab", StringUtil.leadLeft("ab",' ', 4));
        Assert.assertEquals("   a", StringUtil.leadLeft("a",' ', 4));
        Assert.assertEquals("    ", StringUtil.leadLeft("",' ', 4));
        Assert.assertEquals("    ", StringUtil.leadLeft(null,' ', 4));
    }

    @Test
    public void testLeadRight() throws Exception {
        Assert.assertEquals("", StringUtil.leadRight("",' ', 0));
        Assert.assertEquals("", StringUtil.leadRight(null,' ', 0));
        Assert.assertEquals("abc", StringUtil.leadRight("abc",' ', 2));
        Assert.assertEquals("abc", StringUtil.leadRight("abc",' ', 3));
        Assert.assertEquals("abc ", StringUtil.leadRight("abc",' ', 4));
        Assert.assertEquals("ab  ", StringUtil.leadRight("ab",' ', 4));
        Assert.assertEquals("a   ", StringUtil.leadRight("a",' ', 4));
        Assert.assertEquals("    ", StringUtil.leadRight("",' ', 4));
        Assert.assertEquals("    ", StringUtil.leadRight(null,' ', 4));
    }

    @Test
    public void testIndexOf() throws Exception {
        final String text = "abcdefgh";
        Assert.assertEquals(0, StringUtil.indexOf(text, 0, 'b','a'));
        Assert.assertEquals(1, StringUtil.indexOf(text, 1, 'b','a'));
        Assert.assertEquals(-1, StringUtil.indexOf(text, 2, 'b','a'));
        Assert.assertEquals(3, StringUtil.indexOf(text, 0, 'd','h'));
        Assert.assertEquals(-1, StringUtil.indexOf(text, 0));
        Assert.assertEquals(-1, StringUtil.indexOf(text, 0));
    }

    @Test
    public void testParseDate() throws Exception {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Assert.assertNull( StringUtil.parseDate(null) );
        Assert.assertNull( StringUtil.parseDate("") );
        final String[] validTestCases = {"01.02.2010", "30.12.1976", "31.12.2009xxx"};
        for (String txt : validTestCases) {
            final Date date = StringUtil.parseDate(txt);
            Assert.assertNotNull(date);
            Assert.assertEquals(txt.substring(0,10), StringUtil.formatDate(date));
            Assert.assertEquals(date, formatter.parse(txt));
        }
        final String[] invalidTestCases = {"01", "01.", "01.02.", "01..02.2003", "xx.01.02.2009", "01-02-2009", "?1.12.2009", "+1.11.2009", "-1.15.33"};
        for (String txt : invalidTestCases) {
            try {
                StringUtil.parseDate(txt);
                Assert.fail("incorrect pattern parsed: "+txt);
            } catch (ParseException e) {
            }
            try {
                final Date date = formatter.parse(txt);
                System.err.println("WARN: potentially correct pattern ("+formatter.toPattern()+") failed: "+txt+"  -->  "+StringUtil.formatDate(date));
            } catch (ParseException e) {
            }
        }
    }

    @Test
    public void testParseDateTime() throws Exception {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Assert.assertNull( StringUtil.parseDate(null) );
        Assert.assertNull( StringUtil.parseDate("") );
        final String[] validTestCases = {"01.02.2010 00:00:00", "30.12.1976 23:59:59", "31.12.2009 13:30:00xxx"};
        for (String txt : validTestCases) {
            final Date date = StringUtil.parseDateTime(txt);
            Assert.assertNotNull(date);
            Assert.assertEquals(txt.substring(0,19), StringUtil.formatDateTime(date));
            Assert.assertEquals(date, formatter.parse(txt));
        }
        final String[] invalidTestCases = {
                "01", "01.", "01.02.", "xx.01.02.2009", "01-02-2009", "?1.12.2009", "+1.11.2009", "-1.11.2009", "1.2.2010", "1.2.30",
                "01.02.2001 ", "01.02.2002 12", "01.02.2002 12:", "01.02.2002 13:17", "1.02.2003    07:40:00", "01.3.2003 07:40:"
        };
        for (String txt : invalidTestCases) {
            try {
                StringUtil.parseDateTime(txt);
                Assert.fail("incorrect pattern parsed: "+txt);
            } catch (ParseException e) {
            }
            try {
                final Date date = formatter.parse(txt);
                System.err.println("WARN: potentially correct pattern ("+formatter.toPattern()+") failed: "+txt+"  -->  "+StringUtil.formatDateTime(date));
            } catch (ParseException e) {
            }
        }
    }

    @Test
    public void testParseDateTime2() throws Exception {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Assert.assertNull( StringUtil.parseDate(null) );
        Assert.assertNull( StringUtil.parseDate("") );
        final String[] validTestCases = {"01.02.2010 00:00", "30.12.1976 23:59", "31.12.2009 13:30:11xxx"};
        for (String txt : validTestCases) {
            final Date date = StringUtil.parseDateTime2(txt);
            Assert.assertNotNull(date);
            Assert.assertEquals(txt.substring(0,16), StringUtil.formatDateTime2(date));
            Assert.assertEquals(date, formatter.parse(txt));
        }
        final String[] invalidTestCases = {
                "01", "01.", "01.02.", "xx.01.02.2009", "01-02-2009", "?1.12.2009", "+1.11.2009", "-1.11.2009", "1.2.2010", "1.2.30",
                "01.02.2001 ", "01.02.2002 12", "01.02.2002 12:", "1.02.2003    07:40", 
        };
        for (String txt : invalidTestCases) {
            try {
                StringUtil.parseDateTime2(txt);
                Assert.fail("incorrect pattern parsed: "+txt);
            } catch (ParseException e) {
            }
            try {
                final Date date = formatter.parse(txt);
                System.err.println("WARN: potentially correct pattern ("+formatter.toPattern()+") failed: "+txt+"  -->  "+StringUtil.formatDateTime2(date));
            } catch (ParseException e) {
            }
        }
    }

    @Test
    public void testNormalizePath() throws Exception {
        final String ctx = "/ctx";
        final String[][] testcases = new String[][]{
                {null,            ctx},
                {"",              ctx},
                {"/",             "/"},
                {"//",            "/"},
                {"aa",            (ctx.endsWith("/") ? ctx : ctx+"/")+"aa"},
                {"/aa/bb/cc",     "/aa/bb/cc"},
                {"/aa//bb/./cc/", "/aa/bb/cc"},
                {"/aa/bb/../cc",  "/aa/cc"},
                {"aa/bb/../cc",   ctx+"/"+"aa/cc"},
                {"./aa/bb/../cc", ctx+"/"+"aa/cc"}
        };
        for (String[] testcase : testcases) {
            final String result = StringUtil.normalizePath(ctx, testcase[0]);
            System.out.println(testcase[0]+"  -->  "+result);
            Assert.assertEquals("failed testcase '"+testcase[0]+"': ", testcase[1], result);
        }
    }

    @Test
    public void testJoinAndSplit() throws Exception {
        final Object[][] testcases = {
                new Object[]{null, null},
                new Object[]{new String[]{}, ""},
                new Object[]{new String[]{" "}, " "},
                new Object[]{new String[]{"",""}, ":"},
                new Object[]{new String[]{"&",":"}, "&&:&:"},
                new Object[]{new String[]{"aaa", "bbb", "ccc"},  "aaa:bbb:ccc"},
                new Object[]{new String[]{"", "bbb", ""},  ":bbb:"},
                new Object[]{new String[]{"a:1", "b:2", "x&y"},  "a&:1:b&:2:x&&y"},
                new Object[]{new String[]{"a&&"},  "a&&&&"},
        };
        for (Object[] testcase : testcases) {
            final String[] tcParts = (String[])testcase[0];
            final String tcJoined = (String)testcase[1];
            final String joined = StringUtil.join('&', ':', tcParts);
            Assert.assertEquals(tcJoined, joined);
            final String[] parts = StringUtil.split('&', ':', joined);
            Assert.assertArrayEquals(tcParts, parts);
        }
        Assert.assertNull( StringUtil.split('&',':',null) );
    }

    @Test
    public void testReplace2() throws Exception {
        final Map<String,CharSequence> attrs = new HashMap<String,CharSequence>();
        attrs.put("a", "AA");
        attrs.put("b", "BB");
        attrs.put("c", "");
        final String[][] testcases = {
                {null,              null},
                {"",                ""},
                {"'{a}'+'{b}'=c",   "'AA'+'BB'=c"},
                {"{a}{b}",          "AABB"},
                {"{a}{b}{c}",       "AABB"},
                {"{a}{b}{c}{d}",    "AABBnull"},
                {"}{a}{b}{",        "}AABB{"},
                {"{x{a}+{b}}{",     "{xAA+BB}{"},
                {"{{{x{a}}}}+{b}}{","{{{xAA}}}+BB}{"}
        };
        for (String[] testcase : testcases) {
            final String result = StringUtil.replace(testcase[0], attrs,'{','}');
            Assert.assertEquals(testcase[0], testcase[1], result);
        }
    }

    @Test
    public void testMask() throws Exception {
        final String[][] testcases = {
                {null, null}, {"",  ""}, {"a", "a"}, {"ab", "ab"}, {"&", "\\&"}, {"\\", "\\\\"},
                {"abc&def\\&", "abc\\&def\\\\\\&"}
        };
        for (String[] testcase : testcases) {
            String result = StringUtil.mask(testcase[0],'&','\\');
            Assert.assertEquals(testcase[0],testcase[1],result);
        }
    }

    @Test
    public void testUnmask() throws Exception {
        final String[][] testcases = {
                {null, null}, {"",  ""}, {"a", "a"}, {"ab", "ab"}, {"\\&", "&"}, {"\\\\", "\\"},
                {"abc\\&def\\\\\\&", "abc&def\\&"}, {"abc&def\\ghi\\", "abc&def\\ghi\\"}
        };
        for (String[] testcase : testcases) {
            String result = StringUtil.unmask(testcase[0],'&','\\');
            Assert.assertEquals(testcase[0],testcase[1],result);
        }
    }

    @Test
    public void testJavaIdentifier() throws Exception {
        final String[] validcases = {"a", "ab", "тест", "a1", "_a1", "_"};
        final String[] invalidcases = {null, "", " ", "\t", " a", "a b", "a ", "1", " 0", "1a", "x*y", ":", "?"};
        for (String str : validcases) {
            Assert.assertEquals(str, true, StringUtil.isJavaIdentifier(str));
        }
        for (String str : invalidcases) {
            Assert.assertEquals(str, false, StringUtil.isJavaIdentifier(str));
        }
    }
}
