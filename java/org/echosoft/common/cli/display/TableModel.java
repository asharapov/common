package org.echosoft.common.cli.display;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.echosoft.common.utils.StringUtil;

/**
 * Описание табличной формы представления данных для их отображения в текстовом виде.
 *
 * @author Anton Sharapov
 */
public class TableModel implements Serializable {

    private final List<Column> columns;
    private boolean headersVisible;
    private NewLineFormat nlf;

    public TableModel() {
        this.columns = new ArrayList<Column>();
        this.headersVisible = true;
        this.nlf = NewLineFormat.UNIX;
    }

    public boolean isHeadersVisible() {
        return headersVisible;
    }
    public void setHeadersVisible(final boolean headersVisible) {
        this.headersVisible = headersVisible;
    }

    public NewLineFormat getNewLineFormat() {
        return nlf;
    }
    public void setNewLineFormat(final NewLineFormat nlf) {
        this.nlf = nlf != null ? nlf : NewLineFormat.UNIX;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public Column addColumn(final String attr) {
        final Column column = new Column(attr, "", CellFormatters.OBJECT, Alignment.LEFT);
        this.columns.add(column);
        return column;
    }

    public Column addColumn(final String attr, final String title) {
        final Column column = new Column(attr, title, CellFormatters.OBJECT, Alignment.LEFT);
        this.columns.add(column);
        return column;
    }

    public Column addColumn(final String attr, final String title, final CellValueFormatter formatter, final Alignment align) {
        final Column column = new Column(attr, title, formatter, align);
        this.columns.add(column);
        return column;
    }


    /**
     * Описание отдельной колонки в таблице.
     */
    public static class Column implements Serializable {
        private final String attr;
        private String title;
        private Alignment align;
        private CellValueFormatter formatter;
        private int leftPadding;
        private int rightPadding;
        private int minWidth;
        private int maxWidth;
        private int width;

        private Column(final String attr, final String title, final CellValueFormatter formatter, final Alignment align) {
            this.attr = StringUtil.trim(attr);
            this.title = title != null ? title.trim() : "";
            this.formatter = formatter != null ? formatter : CellFormatters.OBJECT;
            this.align = align != null ? align : Alignment.LEFT;
            this.leftPadding = 1;
            this.rightPadding = 1;
        }

        public String getAttribute() {
            return attr;
        }

        public String getTitle() {
            return title;
        }
        public void setTitle(final String title) {
            this.title = title != null ? title.trim() : "";
        }

        public CellValueFormatter getFormatter() {
            return formatter;
        }
        public void setFormatter(final CellValueFormatter formatter) {
            this.formatter = formatter != null ? formatter : CellFormatters.OBJECT;
        }

        public Alignment getAlignment() {
            return align;
        }
        public void setAlignment(final Alignment align) {
            this.align = align != null ? align : Alignment.LEFT;
        }

        public int getLeftPadding() {
            return leftPadding;
        }
        public void setLeftPadding(final int leftPadding) {
            this.leftPadding = leftPadding > 0 ? leftPadding : 0;
        }

        public int getRightPadding() {
            return rightPadding;
        }
        public void setRightPadding(final int rightPadding) {
            this.rightPadding = rightPadding > 0 ? rightPadding : 0;
        }

        public int getMinWidth() {
            return minWidth;
        }
        public void setMinWidth(final int minWidth) {
            this.minWidth = minWidth > 0 ? minWidth : 0;
        }

        public int getMaxWidth() {
            return maxWidth;
        }
        public void setMaxWidth(final int maxWidth) {
            this.maxWidth = maxWidth > 0 ? maxWidth : 0;
        }

        public int getWidth() {
            return width;
        }
        public void setWidth(final int width) {
            this.width = width > 0 ? width : 0;
        }

        public String toString() {
            return "[Column{attr:" + attr + ", title:" + title + ", padding:" + leftPadding + ", minwidth:" + minWidth + ", maxwidth:" + maxWidth + ", width:" + width + "}]";
        }
    }

    public static enum Alignment {
        LEFT, CENTER, RIGHT
    }

    public static enum NewLineFormat {
        DOS(new char[]{'\r', '\n'}),
        UNIX(new char[]{'\n'});

        private final char[] chars;

        private NewLineFormat(final char[] newline) {
            this.chars = newline;
        }

        public char[] getChars() {
            return chars;
        }
    }
}
