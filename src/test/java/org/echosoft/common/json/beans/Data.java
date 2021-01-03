package org.echosoft.common.json.beans;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.echosoft.common.json.JsonSerializer;
import org.echosoft.common.json.JsonWriter;
import org.echosoft.common.json.annotate.JsonUseSeriazer;
import org.echosoft.common.utils.StringUtil;

/**
 * @author Anton Sharapov
 */
public final class Data {

    public static final Address addr1 = new Address(null, "Moscow", "Birulevskaja st", 46);
    public static final Address addr2 = new Address("Moscow state.", "Krasnogorsk", "Mochovaja st", 34);
    public static final Address addr3 = new Address("Saratov state", "Saratov", "Moscow st.", 8);
    public static final Address addr4 = new Address("Saratov state", "Balakovo", "Lenina st.", 54);
    public static final Address addr5 = new Address("Saratov state", "Balakovo", "Zakharovych st.", 6);

    public static final Person pers1 = new Person("Ivanov", "ivanov@mail.ru", parseDate("17.05.1974"), addr1);
    public static final Person pers2 = new Person("Petrov", "petrov@mail.ru", parseDate("17.05.1974"), addr2);

    public static final Company company1 = new Company("Рога и копыта", parseDate("01.02.2003"), addr1, pers1);
    public static final Company company2 = new Company("Сукинъ и Ко", parseDate("12.04.2005"), addr3, pers2);

    public static final Contract ctr11 = new Contract(1, parseDate("13.05.2003"), Contract.State.PAYED);
    public static final Contract ctr12 = new Contract(2, parseDate("14.05.2003"), Contract.State.EXECUTED);
    public static final Contract ctr13 = new Contract(3, parseDate("15.05.2003"), Contract.State.APPROVED);
    public static final Contract ctr21 = new Contract(4, parseDate("27.08.2006"), Contract.State.DRAFT);
    public static final Contract ctr22 = new Contract(5, parseDate("14.09.2006"), Contract.State.DRAFT);
    public static final Contract ctr23 = new Contract(6, parseDate("17.09.2006"), Contract.State.APPROVED);

    public static final Collection<Company> data = new ArrayList<Company>();
    static {
        data.addAll( Arrays.asList(company1, company2) );
        company1.getContracts().addAll(Arrays.asList(ctr11, ctr12, ctr13) );
        company2.getContracts().addAll(Arrays.asList(ctr21, ctr22, ctr23) );
        ctr11.params.put("x", 1);
        ctr11.params.put("y", 2);
        ctr21.params.put("x", 2);
        ctr21.params.put("y", 4);
        ctr21.params.put("z", 8.0);

        ctr11.items.add( new Item("item1", 1, 34.567891) );
        ctr11.items.add( new Item("item2", 1, 5.3) );
        ctr11.items.add( new Item("item3", 4, 12) );
        ctr12.items.add( new Item("item1", 2, 3.7) );
        ctr12.items.add( new Item("item2", 4, 6) );
        ctr21.items.add( new Item("item1", 2, 3.5) );
        ctr21.items.add( new Item("item2", 2, 5.3) );
        ctr21.items.add( new Item("item3", 8, 12) );
        ctr22.items.add( new Item("item1", 4, 3.7) );
        ctr22.items.add( new Item("item2", 8, 6) );
    }

    private static Date parseDate(final String text) {
        try {
            return StringUtil.parseDate(text);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static enum Priority {
        HIGH, MEDIUM, LOW
    }


    public static class A {
        public String a = "A";
    }

    public static class B extends A {
        public String b = "B";
    }

    @JsonUseSeriazer(value = JSC1.class, recursive = true)
    public static class C1 extends B {
        public String c1 = "C1";
    }

    @JsonUseSeriazer(value = JSC2.class, recursive = false)
    public static class C2 extends B {
        public String c2 = "C2";
    }

    public static class D1 extends C1 {
        public int d = 1;
    }

    public static class D2 extends C2 {
        public int d = 2;
    }


    public static final class JSC1 implements JsonSerializer<C1> {
        public void serialize(C1 src, JsonWriter out) throws IOException, InvocationTargetException, IllegalAccessException {
            out.beginObject();
            out.writeProperty("$", "JSC1");
            out.writeProperty("a", src.a);
            out.writeProperty("b", src.b);
            out.writeProperty("c1", src.c1);
            out.endObject();
        }
    }
    public static final class JSC2 implements JsonSerializer<C2> {
        public void serialize(C2 src, JsonWriter out) throws IOException, InvocationTargetException, IllegalAccessException {
            out.beginObject();
            out.writeProperty("$", "JSC2");
            out.writeProperty("a", src.a);
            out.writeProperty("b", src.b);
            out.writeProperty("c2", src.c2);
            out.endObject();
        }
    }

    public static final class JSD implements JsonSerializer<D1> {
        public void serialize(D1 src, JsonWriter out) throws IOException, InvocationTargetException, IllegalAccessException {
            out.beginObject();
            out.writeProperty("$", "JSD");
            out.writeProperty("a", src.a);
            out.writeProperty("b", src.b);
            out.writeProperty("c1", src.c1);
            out.writeProperty("d", src.d);
            out.endObject();
        }
    }

}
