package org.echosoft.common.cli.display;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Используется для демонстрации прогресса при выполнении консольных программ.
 *
 * @author Anton Sharapov
 */
public class ProgressMeter {

    public static final int DEFAULT_HITS_PER_DOT = 1;
    public static final int DEFAULT_DOTS_PER_LINE = 50;
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    public static final Locale DEFAULT_LOCALE = new Locale("ru", "RU");

    private final Appendable out;
    private final int hitsPerDot;
    private final int dotsPerLine;
    private final TimeUnit timeUnit;
    private String legend;
    private Locale locale;
    private long startTime;
    private long completeTime;
    private long hitsCount;
    private long errorsCount;
    private boolean hasErrors;

    public ProgressMeter() {
        this(System.out, DEFAULT_HITS_PER_DOT, DEFAULT_DOTS_PER_LINE, null);
    }

    public ProgressMeter(final Appendable out) {
        this(out, DEFAULT_HITS_PER_DOT, DEFAULT_DOTS_PER_LINE, null);
    }

    public ProgressMeter(final Appendable out, final int hitsPerDot) {
        this(out, hitsPerDot, DEFAULT_DOTS_PER_LINE, null);
    }

    public ProgressMeter(final Appendable out, final int hitsPerDot, final int dotsPerLine, final TimeUnit timeUnit) {
        this.out = out;
        this.hitsPerDot = hitsPerDot > 0 ? hitsPerDot : DEFAULT_HITS_PER_DOT;
        this.dotsPerLine = dotsPerLine > 0 ? dotsPerLine : DEFAULT_DOTS_PER_LINE;
        this.timeUnit = timeUnit;
        this.legend = "hits";
        this.locale = DEFAULT_LOCALE;
    }

    public Locale getLocale() {
        return locale;
    }

    public ProgressMeter applyLocale(final Locale locale) {
        this.locale = locale;
        return this;
    }

    public ProgressMeter applyLegend(final String legend) {
        this.legend = legend;
        return this;
    }

    public long getTotalHitsCount() {
        return hitsCount;
    }

    public long getTotalErrorsCount() {
        return errorsCount;
    }

    public long getTotalTime() {
        final long time = completeTime == 0 ? System.currentTimeMillis() : completeTime;
        return time - startTime;
    }

    public void start() throws IOException {
        out.append('\n');
        this.hasErrors = false;
        this.hitsCount = 0;
        this.errorsCount = 0;
        this.startTime = System.currentTimeMillis();
        this.completeTime = 0;
    }

    public void hit(final boolean success) throws IOException {
        hitsCount++;
        if (!success) {
            hasErrors = true;
            errorsCount++;
        }
        if ((hitsCount % hitsPerDot) == 0) {
            out.append(hasErrors ? 'E' : '.');
            hasErrors = false;
        }
        if ((hitsCount % (hitsPerDot * dotsPerLine)) == 0) {
            prepareAndShowStatistics();
        }
    }

    public void complete() throws IOException {
        completeTime = System.currentTimeMillis();
        for (long i = hitsCount / hitsPerDot; i==0 || (i % dotsPerLine) != 0; i++)
            out.append(' ');
        prepareAndShowStatistics();
    }

    protected void prepareAndShowStatistics() throws IOException {
        final long msTime = System.currentTimeMillis() - startTime;
        TimeUnit tu = null;
        double avg = Double.NaN;
        if (timeUnit == null) {
            for (TimeUnit u : TimeUnit.values()) {
                avg = calcAvg(hitsCount, msTime, u);
                if (!Double.isNaN(avg) && avg >= 10) {
                    tu = u;
                    break;
                }
            }
            if (tu == null) {
                tu = DEFAULT_TIME_UNIT;
                avg = Double.NaN;
            }
        } else {
            tu = timeUnit;
            avg = calcAvg(hitsCount, msTime, tu);
        }
        out.append(' ');
        out.append(formatStatistics(avg, tu));
        out.append('\n');
    }

    protected String formatStatistics(final double avg, final TimeUnit timeUnit) {
        final String tus = getTimeUnitName(timeUnit);
        return String.format(getLocale(), "( %d %s, %,.2f %s/%s )", hitsCount, legend, avg, legend, tus);
    }

    private static double calcAvg(final long hits, final long timeMs, final TimeUnit timeUnit) {
        final long factor = timeUnit.toMillis(1);
        if (factor == 0)
            return Double.NaN;
        final double unitTime = (double)timeMs / factor;
        return hits / unitTime;
    }

    public static String getTimeUnitName(final TimeUnit timeUnit) {
        switch (timeUnit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "mks";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "sec";
            case MINUTES:
                return "min";
            case HOURS:
                return "hr";
            case DAYS:
                return "days";
            default:
                throw new IllegalStateException();
        }
    }
}
