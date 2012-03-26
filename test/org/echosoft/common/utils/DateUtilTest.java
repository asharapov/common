package org.echosoft.common.utils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anton Sharapov
 */
public class DateUtilTest {

    private static Date base1, base2;
    private static Map<String, String> tests1, tests2;
    private static String[] incorrectTests = {"current", "week", "ago", "fucked week", "past", "tense", "0", "1 week", "bla-bla-bla", "x now"};

    @BeforeClass
    public static void beforeClass() throws Exception {
        base1 = StringUtil.parseISODateTime("2012-02-15 13:27:55");
        tests1 = new HashMap<String, String>();
        tests1.put("now", "2012-02-15 13:27:55");
        tests1.put("today", "2012-02-15 13:27:55");
        tests1.put("start of today", "2012-02-15 00:00:00");
        tests1.put("end today", "2012-02-15 23:59:59");
        tests1.put("last day", "2012-02-14 13:27:55");
        tests1.put("2 days ago", "2012-02-13 13:27:55");
        tests1.put("beginning of 2 days ago", "2012-02-13 00:00:00");
        tests1.put("end of 2 days ago", "2012-02-13 23:59:59");
        tests1.put("40 days ago", "2012-01-06 13:27:55");
        final int firstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
        if (firstDayOfWeek == Calendar.MONDAY) {
            tests1.put("start of current week", "2012-02-13 00:00:00");
            tests1.put("end of current week", "2012-02-19 23:59:59");
            tests1.put("three weeks ago", "2012-01-25 13:27:55");
            tests1.put("last week", "2012-02-08 13:27:55");
        } else
        if (firstDayOfWeek == Calendar.SUNDAY) {
            tests1.put("start of current week", "2012-02-12 00:00:00");
            tests1.put("end of current week", "2012-02-18 23:59:59");
            tests1.put("three weeks ago", "2012-01-25 13:27:55");
            tests1.put("last week", "2012-02-08 13:27:55");
        }
        tests1.put("2 month ago", "2011-12-15 13:27:55");
        tests1.put("start of 2 month ago", "2011-12-01 00:00:00");
        tests1.put("end of 2 months ago", "2011-12-31 23:59:59");
        tests1.put("previous quarter start", "2011-10-01 00:00:00");
        tests1.put("start of the next quarter", "2012-04-01 00:00:00");
        tests1.put("end of the current quarter", "2012-03-31 23:59:59");
        base2 = StringUtil.parseISODateTime("2012-03-22 17:58:30");
        tests2 = new HashMap<String, String>();
        if (firstDayOfWeek == Calendar.MONDAY) {
            tests2.put("start last week", "2012-03-12 00:00:00");
            tests2.put("end last week", "2012-03-18 23:59:59");
        } else
        if (firstDayOfWeek == Calendar.SUNDAY) {
            tests2.put("start last week", "2012-03-11 00:00:00");
            tests2.put("end last week", "2012-03-17 23:59:59");
        }
    }

    @Test
    public void testSpecialDefinitions() throws Exception {
        Assert.assertEquals("null", null, DateUtil.calculate(base1, null));
        Assert.assertEquals("null", null, DateUtil.calculate(base1, ""));
        Assert.assertEquals("null", null, DateUtil.calculate(base1, "  "));
        Assert.assertEquals("now", base1, DateUtil.calculate(base1, "now"));
    }

    @Test
    public void testValidDefinitions1() throws Exception {
        for (Map.Entry<String, String> entry : tests1.entrySet()) {
            final Date result = DateUtil.calculate(base1, entry.getKey());
            final Date normalizedResult = StringUtil.parseISODateTime(StringUtil.formatISODateTime(result));
            final Date controlResult = StringUtil.parseISODateTime(entry.getValue());
            Assert.assertEquals(entry.getKey(), controlResult, normalizedResult);
        }
    }

    @Test
    public void testValidDefinitions2() throws Exception {
        for (Map.Entry<String, String> entry : tests2.entrySet()) {
            final Date result = DateUtil.calculate(base2, entry.getKey());
            final Date normalizedResult = StringUtil.parseISODateTime(StringUtil.formatISODateTime(result));
            final Date controlResult = StringUtil.parseISODateTime(entry.getValue());
            Assert.assertEquals(entry.getKey(), controlResult, normalizedResult);
        }
    }

    @Test
    public void testInvalidDefinitions() throws Exception {
        for (String incorrectTest : incorrectTests) {
            try {
                DateUtil.calculate(base1, incorrectTest);
                Assert.fail(incorrectTest);
            } catch (ParseException e) {
                // all is ok
            }
        }
    }

}
