package org.echosoft.common.json;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.echosoft.common.json.beans.Component;
import org.echosoft.common.json.beans.Data;
import org.echosoft.common.json.beans.Item;
import org.echosoft.common.json.introspect.BeanSerializer;
import org.echosoft.common.data.misc.TreeNode;
import org.echosoft.common.utils.StringUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleJsonWriterTest {

    private static StringWriter sw;
    private static JsonWriter jw;

    @Before
    public void before() {
        sw = new StringWriter();
        jw = new SimpleJsonWriter(new JsonContext(), sw);
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
        Assert.assertEquals("[{\"int\":1,\"byte\":2,\"short\":43,\"char\":\"c\",\"dbl\":1.2,\"flt\":2.5}]", sw.getBuffer().toString());
    }

    @Test
    public void testWriter2() throws Exception {
        jw.beginObject();
        jw.writeProperty("total", 3);
        jw.writeProperty("date", StringUtil.parseDateTime("13.01.2008 23:59:59"));
        jw.writeProperty("bool", Boolean.TRUE);
        jw.writeProperty("bd", new BigDecimal(123.66666666));
        jw.endObject();
        Assert.assertEquals("{\"total\":3,\"date\":\"2008-01-13T23:59:59\",\"bool\":true,\"bd\":123.666666660000004185349098406732082366943359375}", sw.getBuffer().toString());
    }

    @Test
    public void testWriter3() throws Exception {
        jw.beginArray();
        jw.writeObject("A");
        jw.writeObject(new HashMap());
        jw.writeObject( new Object[]{1,2});
        jw.endArray();
        Assert.assertEquals("[\"A\",{},[1,2]]", sw.getBuffer().toString());
    }

    @Test
    public void testWriter4() throws Exception {
        final Map<String,Object> map = new TreeMap<String,Object>();
        map.put("a", 1);
        map.put("b", "B");
        map.put("c", true);
        final Object test = new Object[]{new int[]{1,2,3}, "abc", 7, null, new Object[]{'c',true,6.0}, map};

        jw.beginObject();
        jw.writeProperty("data", test);
        jw.endObject();
        Assert.assertEquals("{\"data\":[[1,2,3],\"abc\",7,null,[\"c\",true,6.0],{\"a\":1,\"b\":\"B\",\"c\":true}]}", sw.getBuffer().toString());
    }

    @Test
    public void testWriter5() throws Exception {
        jw.beginObject();
        jw.writeProperty("id", 1);
        jw.writeComplexProperty("settings");
        jw.beginObject();
        jw.writeProperty("a", "A");
        jw.endObject();
        jw.writeProperty("name", "testname");
        jw.endObject();
        Assert.assertEquals("{\"id\":1,\"settings\":{\"a\":\"A\"},\"name\":\"testname\"}", sw.getBuffer().toString());
    }

    @Test
    public void testWriter6() throws Exception {
        jw.beginArray();
        jw.beginArray();
        jw.beginArray();
        jw.writeObject(new HashMap());
        jw.writeObject(new HashMap());
        jw.endArray();
        jw.endArray();
        jw.beginArray();
        jw.beginArray();
        jw.writeObject(new HashMap());
        jw.writeObject(new HashMap());
        jw.endArray();
        jw.endArray();
        jw.endArray();
        Assert.assertEquals("[[[{},{}]],[[{},{}]]]", sw.getBuffer().toString());
    }

    @Test
    public void testObjectWriter1() throws Exception {
        jw.writeObject( new Item("item2", 2, 5.3) );
        Assert.assertEquals("{\"cost\":10.59999999999999964472863211994990706443786621093750,\"name\":\"item2\",\"quantity\":2,\"price\":5.29999999999999982236431605997495353221893310546875}", sw.getBuffer().toString());
    }

    @Test
    public void testObjectWriter2() throws Exception {
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
        Assert.assertEquals("{\"bytes\":[0,3,null,7],\"str\":[\"A\",\"B\",null,\"C\"],\"expr\":2+2}", sw.getBuffer().toString());
    }

    @Test
    public void testEnum() throws Exception {
        jw.beginObject();
        jw.writeProperty("priority", Data.Priority.HIGH);
        jw.endObject();
        Assert.assertEquals("{\"priority\":\"HIGH\"}", sw.getBuffer().toString());
    }

    @Test
    public void testTree() throws Exception {
        final TreeNode<String, String> root = new TreeNode<String, String>("", null);
        root.addChildNode("n1", "node1");
        root.addChildNode("n2", "node2");
        root.findNodeById("n1", true).addChildNode("n11", "node1.1");
        System.out.println(root.debugInfo());
        jw.writeObject(root);
        System.out.println(sw.getBuffer().toString());
    }

    @Test
    public void testHierarchyBase1() throws Exception {
        jw.writeObject(new Data.A());
        Assert.assertEquals("{\"a\":\"A\"}", sw.getBuffer().toString());
    }

    @Test
    public void testHierachyBase2() throws Exception {
        jw.writeObject(new Data.B());
        Assert.assertEquals("{\"b\":\"B\",\"a\":\"A\"}", sw.getBuffer().toString());
    }

    @Test
    public void testHierachyBase3() throws Exception {
        jw.writeObject(new Data.C1());
        Assert.assertEquals("{\"$\":\"JSC1\",\"a\":\"A\",\"b\":\"B\",\"c1\":\"C1\"}", sw.getBuffer().toString());
    }

    @Test
    public void testHierachyBase4() throws Exception {
        jw.writeObject(new Data.C2());
        Assert.assertEquals("{\"$\":\"JSC2\",\"a\":\"A\",\"b\":\"B\",\"c2\":\"C2\"}", sw.getBuffer().toString());
    }

    @Test
    public void testHierachyBase5() throws Exception {
        jw.writeObject(new Data.D1());
        Assert.assertEquals("{\"$\":\"JSC1\",\"a\":\"A\",\"b\":\"B\",\"c1\":\"C1\"}", sw.getBuffer().toString());
    }

    @Test
    public void testHierachyBase6() throws Exception {
        jw.writeObject(new Data.D2());
        Assert.assertEquals("{\"d\":2,\"c2\":\"C2\",\"b\":\"B\",\"a\":\"A\"}", sw.getBuffer().toString());
    }

    @Test
    public void testHierarchyExt1() throws Exception {
        jw.getContext().registerSerializer(Data.C1.class, new BeanSerializer(Data.C1.class), false);
        jw.writeObject(new Data.C1());
        Assert.assertEquals("{\"c1\":\"C1\",\"b\":\"B\",\"a\":\"A\"}", sw.getBuffer().toString());
    }

    @Test
    public void testHierarchyExt2() throws Exception {
        jw.writeObject(new Data.D1());
        Assert.assertEquals("{\"$\":\"JSC1\",\"a\":\"A\",\"b\":\"B\",\"c1\":\"C1\"}", sw.getBuffer().toString());
    }

    @Test
    public void testHierarchyExt3() throws Exception {
        jw.getContext().registerSerializer(Data.C1.class, new BeanSerializer(Data.C1.class), true);
        jw.writeObject(new Data.C1());
        Assert.assertEquals("{\"c1\":\"C1\",\"b\":\"B\",\"a\":\"A\"}", sw.getBuffer().toString());
    }

    @Test
    public void testHierarchyExt4() throws Exception {
        jw.getContext().registerSerializer(Data.C1.class, new BeanSerializer(Data.C1.class), true);
        jw.writeObject(new Data.D1());
        Assert.assertEquals("{\"c1\":\"C1\",\"b\":\"B\",\"a\":\"A\"}", sw.getBuffer().toString());
    }

    @Test
    public void testDynamicDereference() throws Exception {
        jw.beginArray();
        jw.writeObject( new Component("c1", null, new Object()) );
        jw.writeObject( new Component("c2", new Component.BorderLayout("right", 20), 1) );
        jw.writeObject( new Component("c3", new Component.TableLayout(10, 20),  new Component("c31",null,null)) );
        jw.endArray();
        Assert.assertEquals("[{\"id\":\"c1\",\"ext\":{}}," +
                "{\"id\":\"c2\",\"align\":\"right\",\"length\":20,\"ext\":1}," +
                "{\"id\":\"c3\",\"rows\":10,\"cells\":20,\"ext\":{\"id\":\"c31\",\"ext\":null}}]", sw.getBuffer().toString());
    }

}