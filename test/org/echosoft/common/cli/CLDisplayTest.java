package org.echosoft.common.cli;

import java.util.Arrays;
import java.util.List;

import org.echosoft.common.cli.display.CellFormatters;
import org.echosoft.common.cli.display.TableModel;
import org.echosoft.common.cli.display.TableProcessor;
import org.echosoft.common.utils.StringUtil;

/**
 * @author Anton Sharapov
 */
public class CLDisplayTest {

    public static void main(final String[] args) throws Exception {
        final List<TestBean> data = Arrays.asList(
                new TestBean(1, "Гуси-лебеди", StringUtil.parseISODate("2014-05-15"), 1, 21398742.777f, true),
                new TestBean(2, "ООО Ромашка", StringUtil.parseISODate("2013-30-31"), 500, 2.75f, false),
                new TestBean(3, "ООО Фармкоп", StringUtil.parseISODate("2014-01-09"), 1004, 2.75f, false),
                new TestBean(4, "Девять негритят", StringUtil.parseISODate("2014-04-01"), 32765, 2.75f, true),
                new TestBean(5, "", null, 0, 6, false),
                new TestBean(6, null, StringUtil.parseISODate("2014-05-15"), 32765, 223.654f, false),
                new TestBean(7, "Просто какая-то строка", StringUtil.parseISODate("2014-08-02"), 31415926, 7.40f, false)
        );

        final TableModel tm1 = new TableModel();
        tm1.addColumn("id", "id", CellFormatters.INTEGER, TableModel.Alignment.RIGHT);
        tm1.addColumn("name", "name", CellFormatters.OBJECT, TableModel.Alignment.LEFT);
        tm1.addColumn("date", "date", CellFormatters.DATE, TableModel.Alignment.CENTER);
        tm1.addColumn("count", "count", CellFormatters.INTEGER, TableModel.Alignment.RIGHT);
        tm1.addColumn("price", "price", CellFormatters.FLOAT, TableModel.Alignment.RIGHT);
        tm1.addColumn("total", "total", CellFormatters.FLOAT, TableModel.Alignment.RIGHT);

        String rendered = TableProcessor.render(tm1, data);
        System.out.println(rendered);
        System.out.println();

        final TableModel tm2 = new TableModel();
        tm2.setDefaultPadding(0);
        tm2.addColumn("id", "id", CellFormatters.INTEGER, TableModel.Alignment.RIGHT);
        tm2.addColumn("name", "name", CellFormatters.OBJECT, TableModel.Alignment.LEFT).setMaxWidth(20);
        tm2.addColumn("date", "date", CellFormatters.DATE, TableModel.Alignment.CENTER);
        tm2.addColumn("count", "count", CellFormatters.INTEGER, TableModel.Alignment.RIGHT);
        tm2.addColumn("price", "price", CellFormatters.FLOAT, TableModel.Alignment.RIGHT);
        tm2.addColumn("total", "total", CellFormatters.FLOAT, TableModel.Alignment.RIGHT);

        rendered = TableProcessor.renderCompact(tm2, data);
        System.out.println(rendered);
    }
}
