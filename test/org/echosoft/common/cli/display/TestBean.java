package org.echosoft.common.cli.display;

import java.util.Date;

/**
 * @author Anton Sharapov
 */
public class TestBean {

    public int id;
    public String name;
    public Date date;
    public long count;
    public float price;
    public boolean checked;

    public TestBean(final int id, final String name, final Date date, final long count, final float price, final boolean checked) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.count = count;
        this.price = price;
        this.checked = checked;
    }

    public double getTotal() {
        return count * price;
    }
}
