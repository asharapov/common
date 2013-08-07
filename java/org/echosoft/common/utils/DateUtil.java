package org.echosoft.common.utils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Содержит методы отвечающие за разбор некоторой даты или периода дат на основе текстовой строки.
 * В простейших случаях строка может содержать дату в одном из классических форматов (например: YYYY-MM-DD), в других случах
 * возможны такие варианты:
 * <ul>
 * <li>now</li>
 * <li>today</li>
 * <li>yesterday</li>
 * <li>tomorrow</li>
 * <li>2 days ago</li>
 * <li>1 week ago</li>
 * <li>last monday</li>
 * <li>start of month</li>
 * <li>end of week</li>
 * </ul>
 *
 * @author Anton Sharapov
 */
public class DateUtil {

    private static final Map<String, TimeUnit> tuTokens;
    private static final Map<String, Tense> tenseTokens;
    private static final Map<String, TimeUnitBound> bnTokens;
    private static final Map<String, Integer> amTokens;

    static {
        tuTokens = new HashMap<String, TimeUnit>();
        tuTokens.put("year", TimeUnit.YEAR);
        tuTokens.put("years", TimeUnit.YEAR);
        tuTokens.put("quarter", TimeUnit.QUARTER);
        tuTokens.put("quarters", TimeUnit.QUARTER);
        tuTokens.put("month", TimeUnit.MONTH);
        tuTokens.put("months", TimeUnit.MONTH);
        tuTokens.put("week", TimeUnit.WEEK);
        tuTokens.put("weeks", TimeUnit.WEEK);
        tuTokens.put("day", TimeUnit.DAY);
        tuTokens.put("days", TimeUnit.DAY);
        tuTokens.put("hour", TimeUnit.HOUR);
        tuTokens.put("hours", TimeUnit.HOUR);
        tuTokens.put("minute", TimeUnit.MINUTE);
        tuTokens.put("minutes", TimeUnit.MINUTE);
        tuTokens.put("second", TimeUnit.SECOND);
        tuTokens.put("seconds", TimeUnit.SECOND);
        tuTokens.put("today", TimeUnit.DAY);
        tuTokens.put("tomorrow", TimeUnit.DAY);
        tuTokens.put("yesterday", TimeUnit.DAY);

        tenseTokens = new HashMap<String, Tense>();
        tenseTokens.put("ago", Tense.PAST);
        tenseTokens.put("before", Tense.PAST);
        tenseTokens.put("last", Tense.PAST);
        tenseTokens.put("past", Tense.PAST);
        tenseTokens.put("previous", Tense.PAST);
        tenseTokens.put("prev", Tense.PAST);
        tenseTokens.put("current", Tense.PAST);
        tenseTokens.put("today", Tense.PAST);
        tenseTokens.put("next", Tense.FUTURE);
        tenseTokens.put("future", Tense.FUTURE);

        bnTokens = new HashMap<String, TimeUnitBound>();
        bnTokens.put("start", TimeUnitBound.LOW);
        bnTokens.put("beginning", TimeUnitBound.LOW);
        bnTokens.put("end", TimeUnitBound.HIGH);

        amTokens = new HashMap<String, Integer>();
        amTokens.put("last", 1);
        amTokens.put("yesterday", 1);
        amTokens.put("previous", 1);
        amTokens.put("today", 0);
        amTokens.put("current", 0);
        amTokens.put("tomorrow", 1);
        amTokens.put("next", 1);
        amTokens.put("next", 1);
        amTokens.put("one", 1);
        amTokens.put("two", 2);
        amTokens.put("three", 3);
        amTokens.put("four", 4);
        amTokens.put("five", 5);
        amTokens.put("six", 6);
        amTokens.put("seven", 7);
        amTokens.put("eight", 8);
        amTokens.put("nine", 9);
        amTokens.put("ten", 10);
    }

    public static enum Tense {
        PAST, FUTURE
    }

    public static enum TimeUnit {
        YEAR, QUARTER, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND
    }

    public static enum TimeUnitBound {
        LOW, NEUTRAL, HIGH
    }

    public static Date calculate(final Date base, final int amount, final TimeUnit unit, final Tense tense, final TimeUnitBound bound) {
        final Calendar cal = Calendar.getInstance();
        if (base != null)
            cal.setTime(base);
        final int sign = tense == Tense.FUTURE ? 1 : -1;
        switch (unit) {
            case YEAR: {
                cal.add(Calendar.YEAR, amount * sign);
                switch (bound) {
                    case LOW: {
                        cal.set(Calendar.MONTH, Calendar.JANUARY);
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        break;
                    }
                    case NEUTRAL:
                        break;
                    case HIGH: {
                        cal.set(Calendar.MONTH, Calendar.DECEMBER);
                        cal.set(Calendar.DAY_OF_MONTH, 31);
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        cal.set(Calendar.MILLISECOND, 999);
                        break;
                    }
                }
                break;
            }
            case QUARTER: {
                cal.add(Calendar.MONTH, 3 * amount * sign);
                cal.getTime(); // sync the time and calendar fields ...
                switch (bound) {
                    case LOW: {
                        final int month = cal.get(Calendar.MONTH);
                        cal.set(Calendar.MONTH, month - month % 3);
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        break;
                    }
                    case NEUTRAL:
                        break;
                    case HIGH: {
                        final int month = cal.get(Calendar.MONTH);
                        cal.set(Calendar.MONTH, month + 2 - month % 3);
                        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        cal.set(Calendar.MILLISECOND, 999);
                        break;
                    }
                }
                break;
            }
            case MONTH: {
                cal.add(Calendar.MONTH, amount * sign);
                switch (bound) {
                    case LOW: {
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        break;
                    }
                    case NEUTRAL:
                        break;
                    case HIGH: {
                        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        cal.set(Calendar.MILLISECOND, 999);
                        break;
                    }
                }
                break;
            }
            case WEEK: {
                cal.add(Calendar.WEEK_OF_YEAR, amount * sign);
                cal.getTime();  // sync the time and calendar fields ...
                switch (bound) {
                    case LOW: {
                        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        break;
                    }
                    case NEUTRAL:
                        break;
                    case HIGH: {
                        int lastDay = cal.getFirstDayOfWeek() + 6;
                        if (lastDay > 7)
                            lastDay = lastDay - 7;
                        cal.set(Calendar.DAY_OF_WEEK, lastDay);
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        cal.set(Calendar.MILLISECOND, 999);
                        break;
                    }
                }
                break;
            }
            case DAY: {
                cal.add(Calendar.DAY_OF_MONTH, amount * sign);
                switch (bound) {
                    case LOW: {
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        break;
                    }
                    case NEUTRAL:
                        break;
                    case HIGH: {
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        cal.set(Calendar.MILLISECOND, 999);
                        break;
                    }
                }
                break;
            }
            case HOUR: {
                cal.add(Calendar.HOUR_OF_DAY, amount * sign);
                switch (bound) {
                    case LOW: {
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        break;
                    }
                    case NEUTRAL:
                        break;
                    case HIGH: {
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        cal.set(Calendar.MILLISECOND, 999);
                        break;
                    }
                }
                break;
            }
            case MINUTE: {
                cal.add(Calendar.MINUTE, amount * sign);
                switch (bound) {
                    case LOW: {
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        break;
                    }
                    case NEUTRAL:
                        break;
                    case HIGH: {
                        cal.set(Calendar.SECOND, 59);
                        cal.set(Calendar.MILLISECOND, 999);
                        break;
                    }
                }
                break;
            }
            case SECOND: {
                cal.add(Calendar.SECOND, amount * sign);
                switch (bound) {
                    case LOW: {
                        cal.set(Calendar.MILLISECOND, 0);
                        break;
                    }
                    case NEUTRAL:
                        break;
                    case HIGH: {
                        cal.set(Calendar.MILLISECOND, 999);
                        break;
                    }
                }
                break;
            }
        }
        return cal.getTime();
    }


    public static Date calculate(final Date base, final String text) throws ParseException {
        if (text == null)
            return null;
        final String[] tokens = StringUtil.splitIgnoringEmpty(text.toLowerCase().replace('_', ' '), ' ');
        if (tokens.length == 0)
            return null;
        if (tokens.length == 1 && "now".equals(tokens[0]))
            return base;
        Integer amount = null;
        Integer amount2 = null;
        TimeUnit unit = null;
        Tense tense = null;
        TimeUnitBound bound = null;

        for (String token : tokens) {
            if (unit == null)
                unit = tuTokens.get(token);
            if (tense == null)
                tense = tenseTokens.get(token);
            if (bound == null)
                bound = bnTokens.get(token);
            if (amount2 == null)
                amount2 = amTokens.get(token);
            if (amount == null) {
                try {
                    amount = Integer.parseInt(token);
                } catch (NumberFormatException e) {
                    // do nothing ...
                }
            }
        }

        if (bound == null)
            bound = TimeUnitBound.NEUTRAL;
//        if (tense == null)
//            tense = Tense.PAST;
        if (amount == null)
            amount = amount2;

        if (amount == null || unit == null || tense == null)
            throw new ParseException("Can't parse date from text '" + text + "'", 0);

        return calculate(base, amount, unit, tense, bound);
    }

}
