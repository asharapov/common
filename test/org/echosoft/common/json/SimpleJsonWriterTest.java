package org.echosoft.common.json;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import org.echosoft.common.json.beans.Data;
import org.echosoft.common.json.beans.Item;
import org.echosoft.common.utils.StringUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleJsonWriterTest {

    private static StringWriter sw;
    private static JsonWriter jw;

    @BeforeClass
    public static void beforeClass() {
        sw = new StringWriter();
        jw = new SimpleJsonWriter(new JsonContext(), sw);
    }

    @Before
    public void setUp() {
        sw.getBuffer().setLength(0);
    }

    @Test
    public void testWriter1() throws Exception {
        jw.beginArray();
        jw.beginObject();
        jw.writeProperty("int", 1);
        jw.writeProperty("byte", (byte)2);
        jw.writeProperty("short", (short)43);
        jw.writeProperty("char", 'c');
        jw.writeProperty("dbl", 1.2);
        jw.writeProperty("flt", 2.5);
        jw.endObject();
        jw.endArray();
        Assert.assertEquals("[{int:1,byte:2,short:43,char:\"c\",dbl:1.2,flt:2.5}]", sw.getBuffer().toString());
    }

    @Test
    public void testWriter2() throws Exception {
        jw.beginObject();
        jw.writeProperty("total", 3);
        jw.writeProperty("date", StringUtil.parseDateTime("13.01.2008 23:59:59"));
        jw.writeProperty("bool", Boolean.TRUE);
        jw.writeProperty("bd", new BigDecimal(123.66666666));
        jw.endObject();
        Assert.assertEquals("{total:3,date:new Date(2008,0,13,23,59,59),bool:true,bd:123.6667}", sw.getBuffer().toString());
    }

    @Test
    public void testWriter3() throws Exception {
        final Map<String,Object> map = new TreeMap<String,Object>();
        map.put("a", 1);
        map.put("b", "B");
        map.put("c", true);
        final Object test = new Object[]{new int[]{1,2,3}, "abc", 7, null, new Object[]{'c',true,6.0}, map};

        jw.beginObject();
        jw.writeProperty("data", test);
        jw.endObject();
        Assert.assertEquals("{data:[[1,2,3],\"abc\",7,null,[\"c\",true,6.0],{\"a\":1,\"b\":\"B\",\"c\":true}]}", sw.getBuffer().toString());
    }

    @Test
    public void testWriter4() throws Exception {
        jw.beginObject();
        jw.writeProperty("id", 1);
        jw.writeComplexProperty("settings");
        jw.beginObject();
        jw.writeProperty("a", "A");
        jw.endObject();
        jw.writeProperty("name", "testname");
        jw.endObject();
        Assert.assertEquals("{id:1,settings:{a:\"A\"},name:\"testname\"}", sw.getBuffer().toString());
    }

    @Test
    public void testObjectWriter1() throws Exception {
        jw.writeObject( new Item("item2", 2, 5.3) );
        Assert.assertEquals("{cost:10.6000,name:\"item2\",quantity:2,price:5.3000}", sw.getBuffer().toString());
    }

    @Test
    public void testObjectWriter2() throws Exception {
        final JsonWriter jw = new SimpleJsonWriter(new JsonContext(), new StringWriter());
        jw.writeObject(Data.ctr11);
        System.out.println(jw.getOutputWriter().toString());
    }

    @Test
    public void testArray() throws Exception {
        jw.beginObject();
        jw.writeProperty("bytes", new Byte[]{(byte)0, (byte)3, null, (byte)7});
        jw.writeProperty("str", new String[]{"A","B",null,"C"});
        jw.writeProperty("expr", new JSExpression("2+2"));
        jw.endObject();
        Assert.assertEquals("{bytes:[0,3,null,7],str:[\"A\",\"B\",null,\"C\"],expr:2+2}", sw.getBuffer().toString());
    }
}