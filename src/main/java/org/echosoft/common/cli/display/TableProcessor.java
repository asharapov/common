package org.echosoft.common.cli.display;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.echosoft.common.utils.BeanUtil;
import org.echosoft.common.utils.ObjectUtil;

/**
 * @author Anton Sharapov
 */
public class TableProcessor {

    public static String render(final TableModel model, final Object data) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        final int widths[] = calculateColumnWidths(model, data);
        int totalWidth = 1 + widths.length;
        for (int width : widths) totalWidth += width;

        final StringBuilder out = new StringBuilder(4096);
        final char[] CRLF = model.getNewLineFormat().getChars();
        if (model.isHeadersVisible()) {
            // Отрисовка заголовка таблицы ...
            fill(out, '-', totalWidth);
            out.append(CRLF);
            out.append('|');
            for (int i = 0; i < widths.length; i++) {
                final TableModel.Column col = model.getColumns().get(i);
                printValueInCell(col, widths[i], col.getTitle(), out);
                out.append('|');
            }
            out.append(CRLF);
        }

        // Отрисовка строк с данными таблицы ...
        fill(out, '-', totalWidth);
        out.append(CRLF);
        for (Iterator rows = ObjectUtil.makeIterator(data); rows.hasNext(); ) {
            final Object row = rows.next();
            out.append('|');
            for (int i = 0; i < widths.length; i++) {
                final TableModel.Column col = model.getColumns().get(i);
                final String value = getFormattedValue(col, row);
                printValueInCell(col, widths[i], value, out);
                out.append('|');
            }
            out.append(CRLF);
        }
        fill(out, '-', totalWidth);
        out.append(CRLF);

        return out.toString();
    }

    public static String renderCompact(final TableModel model, final Object data) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        final int widths[] = calculateColumnWidths(model, data);
        int totalWidth = widths.length - 1;
        for (int width : widths) totalWidth += width;

        final StringBuilder out = new StringBuilder(4096);
        final char[] CRLF = model.getNewLineFormat().getChars();
        if (model.isHeadersVisible()) {
            // Отрисовка заголовка таблицы ...
            for (int i = 0; i < widths.length; i++) {
                final TableModel.Column col = model.getColumns().get(i);
                printValueInCell(col, widths[i], col.getTitle(), out);
                out.append(' ');
            }
            out.append(CRLF);
        }

        for (int i = 0; i < widths.length; i++) {
            fill(out, '-', widths[i]);
            out.append(' ');
        }
        out.append(CRLF);

        // Отрисовка строк с данными таблицы ...
        for (Iterator rows = ObjectUtil.makeIterator(data); rows.hasNext(); ) {
            final Object row = rows.next();
            for (int i = 0; i < widths.length; i++) {
                final TableModel.Column col = model.getColumns().get(i);
                final String value = getFormattedValue(col, row);
                printValueInCell(col, widths[i], value, out);
                out.append(' ');
            }
            out.append(CRLF);
        }
        for (int i = 0; i < widths.length; i++) {
            fill(out, '-', widths[i]);
            out.append(' ');
        }
        out.append(CRLF);

        return out.toString();
    }

    private static int[] calculateColumnWidths(final TableModel model, final Object data) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        final List<TableModel.Column> columns = model.getColumns();
        final int widths[] = new int[columns.size()];
        boolean requiresCalculations = false;
        for (int i = widths.length - 1; i >= 0; i--) {
            final TableModel.Column col = columns.get(i);
            widths[i] = col.getWidth();
            if (widths[i] == 0) {
                widths[i] = model.isHeadersVisible()
                        ? Math.max(col.getMinWidth(), col.getLeftPadding() + col.getTitle().length() + col.getRightPadding())
                        : col.getMinWidth();
                requiresCalculations = true;
            }
        }
        if (!requiresCalculations)
            return widths;

        for (Iterator rows = ObjectUtil.makeIterator(data); rows.hasNext(); ) {
            final Object row = rows.next();
            for (int i = widths.length - 1; i >= 0; i--) {
                final TableModel.Column col = columns.get(i);
                if (col.getWidth() > 0)
                    continue;
                widths[i] = Math.max(widths[i], col.getLeftPadding() + getFormattedValue(col, row).length() + col.getRightPadding());
            }
        }

        for (int i = widths.length - 1; i >= 0; i--) {
            final TableModel.Column col = columns.get(i);
            if (col.getWidth() == 0 && col.getMaxWidth() > 0)
                widths[i] = Math.min(widths[i], col.getMaxWidth());
        }

        return widths;
    }

    private static void printValueInCell(final TableModel.Column col, final int width, final String value, final StringBuilder out) {
        final int allowedTextLength = width - col.getLeftPadding() - col.getRightPadding();
        final String effectiveValue = allowedTextLength >= value.length()
                ? value
                : value.substring(0, allowedTextLength);

        fill(out, ' ', col.getLeftPadding());
        switch (col.getAlignment()) {
            case LEFT: {
                out.append(effectiveValue);
                fill(out, ' ', allowedTextLength - effectiveValue.length());
                break;
            }
            case CENTER: {
                final int free = allowedTextLength - effectiveValue.length();
                fill(out, ' ', free / 2);
                out.append(effectiveValue);
                fill(out, ' ', free - free / 2);
                break;
            }
            case RIGHT: {
                fill(out, ' ', allowedTextLength - effectiveValue.length());
                out.append(effectiveValue);
                break;
            }
        }
        fill(out, ' ', col.getRightPadding());
    }

    private static String getFormattedValue(final TableModel.Column col, final Object row) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (col.getAttribute() == null)
            return "";
        final Object value = BeanUtil.getProperty(row, col.getAttribute());
        return col.getFormatter().format(value);
    }

    private static void fill(final StringBuilder out, final char c, final int count) {
        for (int i = count; i > 0; i--) {
            out.append(c);
        }
    }
}
