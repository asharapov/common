package org.echosoft.common.cli.display;

import java.text.DecimalFormat;
import java.util.Date;

import org.echosoft.common.utils.StringUtil;

/**
 * @author Anton Sharapov
 */
public class CellFormatters {

    public static final CellValueFormatter OBJECT =
            new CellValueFormatter() {
                @Override
                public String format(final Object obj) {
                    return StringUtil.valueOf(obj);
                }
            };

    public static final CellValueFormatter STRING =
            new CellValueFormatter() {
                @Override
                public String format(final Object obj) {
                    return obj != null ? StringUtil.valueOf(obj) : "";
                }
            };

    public static final CellValueFormatter INTEGER =
            new CellValueFormatter() {
                @Override
                public String format(final Object obj) {
                    if (obj == null)
                        return "";
                    final DecimalFormat formatter = new DecimalFormat("##0");
                    return formatter.format(obj);
                }
            };

    public static final CellValueFormatter FLOAT =
            new CellValueFormatter() {
                @Override
                public String format(final Object obj) {
                    if (obj == null)
                        return "";
                    final DecimalFormat formatter = new DecimalFormat(",##0.00");
                    return formatter.format(obj);
                }
            };

    public static final CellValueFormatter DATE =
            new CellValueFormatter() {
                @Override
                public String format(final Object obj) {
                    return StringUtil.formatISODate((Date) obj);
                }
            };

    public static final CellValueFormatter DATETIME =
            new CellValueFormatter() {
                @Override
                public String format(final Object obj) {
                    return StringUtil.formatISODateTime((Date) obj);
                }
            };

    public static final CellValueFormatter DATETIME2 =
            new CellValueFormatter() {
                @Override
                public String format(final Object obj) {
                    return StringUtil.formatISODateTime2((Date) obj);
                }
            };
}
