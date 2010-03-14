package org.echosoft.common.json.beans;

import org.echosoft.common.json.annotate.JsonField;

/**
 * @author Anton Sharapov
 */
public class Component {
    private final String id;
    private final Layout layout;
    private final Object ext;

    public Component(String id, Layout layout, Object ext) {
        this.id = id;
        this.layout = layout;
        this.ext = ext;
    }

    public String getId() {
        return id;
    }

    @JsonField(dereference = true)
    public Layout getLayout() {
        return layout;
    }

    public Object getExt() {
        return ext;
    }


    public static interface Layout {}

    public static class BorderLayout implements Layout {
        public final String align;
        public final int length;
        public BorderLayout(String align, int length) {
            this.align = align;
            this.length = length;
        }
    }

    public static class TableLayout implements Layout {
        public final int rows;
        public final int cells;
        public TableLayout(int rows, int cells) {
            this.rows = rows;
            this.cells = cells;
        }
    }

}
