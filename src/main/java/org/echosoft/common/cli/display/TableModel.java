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
public class TableModel implements Serializable, Cloneable {

    private List<Column> columns;
    private boolean headersVisible;
    private NewLineFormat nlf;
    private int defaultLeftPadding;
    private int defaultRightPadding;

    public TableModel() {
        this.columns = new ArrayList<>();
        this.headersVisible = true;
        this.nlf = NewLineFormat.UNIX;
        this.defaultLeftPadding = 1;
        this.defaultRightPadding = 1;
    }

    public boolean isHeadersVisible() {
        return headersVisible;
    }
    public TableModel setHeadersVisible(final boolean headersVisible) {
        this.headersVisible = headersVisible;
        return this;
    }

    public NewLineFormat getNewLineFormat() {
        return nlf;
    }
    public TableModel setNewLineFormat(final NewLineFormat nlf) {
        this.nlf = nlf != null ? nlf : NewLineFormat.UNIX;
        return this;
    }

    public TableModel setDefaultPadding(final int defaultPadding) {
        this.defaultLeftPadding = defaultPadding;
        this.defaultRightPadding = defaultPadding;
        return this;
    }

    public TableModel setDefaultPadding(final int defaultLeftPadding, final int defaultRightPadding) {
        this.defaultLeftPadding = defaultLeftPadding;
        this.defaultRightPadding = defaultRightPadding;
        return this;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public Column addColumn(final String attr) {
        final Column column = new Column(attr, "", CellFormatters.OBJECT, Alignment.LEFT, defaultLeftPadding, defaultRightPadding);
        this.columns.add(column);
        return column;
    }

    public Column addColumn(final String attr, final String title) {
        final Column column = new Column(attr, title, CellFormatters.OBJECT, Alignment.LEFT, defaultLeftPadding, defaultRightPadding);
        this.columns.add(column);
        return column;
    }

    public Column addColumn(final String attr, final String title, final CellValueFormatter formatter, final Alignment align) {
        final Column column = new Column(attr, title, formatter, align, defaultLeftPadding, defaultRightPadding);
        this.columns.add(column);
        return column;
    }

    @Override
    public TableModel clone() throws CloneNotSupportedException {
        final TableModel tmc = (TableModel)super.clone();
        tmc.columns = new ArrayList<>(columns.size());
        for (Column col : columns) {
            tmc.columns.add(col.clone());
        }
        return tmc;
    }


    /**
     * Описание отдельной колонки в таблице.
     */
    public static class Column implements Serializable, Cloneable {
        private final String attr;
        private String title;
        private Alignment align;
        private CellValueFormatter formatter;
        private int leftPadding;
        private int rightPadding;
        private int minWidth;
        private int maxWidth;
        private int width;

        private Column(final String attr, final String title, final CellValueFormatter formatter, final Alignment align, final int leftPadding, final int rightPadding) {
            this.attr = StringUtil.trim(attr);
            this.title = title != null ? title.trim() : "";
            this.formatter = formatter != null ? formatter : CellFormatters.OBJECT;
            this.align = align != null ? align : Alignment.LEFT;
            this.leftPadding = leftPadding;
            this.rightPadding = rightPadding;
        }

        public String getAttribute() {
            return attr;
        }

        public String getTitle() {
            return title;
        }
        public Column setTitle(final String title) {
            this.title = title != null ? title.trim() : "";
            return this;
        }

        public CellValueFormatter getFormatter() {
            return formatter;
        }
        public Column setFormatter(final CellValueFormatter formatter) {
            this.formatter = formatter != null ? formatter : CellFormatters.OBJECT;
            return this;
        }

        public Alignment getAlignment() {
            return align;
        }
        public Column setAlignment(final Alignment align) {
            this.align = align != null ? align : Alignment.LEFT;
            return this;
        }

        public int getLeftPadding() {
            return leftPadding;
        }
        public Column setLeftPadding(final int leftPadding) {
            this.leftPadding = leftPadding > 0 ? leftPadding : 0;
            return this;
        }

        public int getRightPadding() {
            return rightPadding;
        }
        public Column setRightPadding(final int rightPadding) {
            this.rightPadding = rightPadding > 0 ? rightPadding : 0;
            return this;
        }

        public int getMinWidth() {
            return minWidth;
        }
        public Column setMinWidth(final int minWidth) {
            this.minWidth = minWidth > 0 ? minWidth : 0;
            return this;
        }

        public int getMaxWidth() {
            return maxWidth;
        }
        public Column setMaxWidth(final int maxWidth) {
            this.maxWidth = maxWidth > 0 ? maxWidth : 0;
            return this;
        }

        public int getWidth() {
            return width;
        }
        public Column setWidth(final int width) {
            this.width = width > 0 ? width : 0;
            return this;
        }

        @Override
        public Column clone() throws CloneNotSupportedException {
            return (Column)super.clone();
        }

        @Override
        public int hashCode() {
            return attr.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this)
                return true;
            if (obj == null || (!getClass().equals(obj.getClass())))
                return false;
            final Column other = (Column)obj;
            return attr.equals(other.attr) &&
                    (title != null ? title.equals(other.title) : other.title == null) &&
                    align == other.align &&
                    (formatter != null ? formatter.equals(other.formatter) : other.formatter == null) &&
                    minWidth == other.minWidth &&
                    maxWidth == other.maxWidth &&
                    width == other.width &&
                    leftPadding == other.leftPadding &&
                    rightPadding == other.rightPadding;
        }

        @Override
        public String toString() {
            return "[Column{attr:" + attr + ", title:" + title + ", padding:" + leftPadding + ", minwidth:" + minWidth + ", maxwidth:" + maxWidth + ", width:" + width + "}]";
        }
    }

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }

    public enum NewLineFormat {
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
