package org.echosoft.common.json;

import java.io.StringWriter;
import java.util.HashMap;

import org.echosoft.common.json.beans.Data;
import org.echosoft.common.model.TreeNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class PrintableJsonWriterTest {

    private static StringWriter sw;
    private static JsonWriter jw;

    @Before
    public void before() {
        sw = new StringWriter();
        jw = new PrintableJsonWriter(new JsonContext(), sw);
    }

    @After
    public void after() {
        System.out.println("\n   ---   \n");
    }

    @Test
    public void testArray1() throws Exception {
        jw.beginArray();
        jw.beginObject();
        jw.endObject();
        jw.beginObject();
        jw.endObject();
        jw.beginObject();
        jw.endObject();
        jw.endArray();
        System.out.println(sw.getBuffer().toString());
    }

    @Test
    public void testArray2() throws Exception {
        jw.beginArray();
        jw.writeObject( new Object() );
        jw.writeObject( new Object() );
        jw.writeObject( new Object() );
        jw.endArray();
        System.out.println(sw.getBuffer().toString());
    }

    @Test
    public void testComplex1() throws Exception {
        jw.getOutputWriter().write("var data = ");
        jw.beginObject();
        jw.writeProperty("a", "A");
        jw.writeProperty("b", "B");
        jw.writeComplexProperty("c");
        jw.beginObject();
        jw.writeProperty("d", "D");
        jw.writeProperty("e", "E");
        jw.writeComplexProperty("f");
        jw.beginObject();
        jw.writeComplexProperty("g");
        jw.beginObject();
        jw.writeProperty("h", "H");
        jw.endObject();
        jw.writeProperty("i", "I");
        jw.endObject();
        jw.writeProperty("j", "J");
        jw.writeComplexProperty("k");
        jw.beginObject();
        jw.writeProperty("l", "L");
        jw.writeComplexProperty("m");
        jw.beginObject();
        jw.endObject();
        jw.writeComplexProperty("n");
        jw.beginObject();
        jw.writeProperty("o", "O");
        jw.endObject();
        jw.endObject();
        jw.endObject();
        jw.endObject();
        System.out.println(sw.getBuffer().toString());
    }

    @Test
    public void testComplex2() throws Exception {
        jw.getOutputWriter().write("var data = ");
        jw.beginObject();
        jw.writeProperty("a", "A");
        jw.writeComplexProperty("b");
        jw.beginObject();
        jw.writeProperty("c", "C");
        jw.writeComplexProperty("d");
        jw.beginArray();
        jw.beginObject();
        jw.writeProperty("e", "E");
        jw.endObject();
        jw.beginObject();
        jw.writeProperty("f", "F");
        jw.endObject();
        jw.writeObject("G");
        jw.writeObject("H");
        jw.beginArray();
        jw.writeObject("I");
        jw.beginObject();
        jw.endObject();
        jw.beginObject();
        jw.endObject();
        jw.writeObject(new HashMap());
        jw.beginArray();
        jw.endArray();
        jw.writeObject(new Object[0]);
        jw.endArray();
        jw.endArray();
        jw.endObject();
        jw.endObject();
        System.out.println(sw.getBuffer().toString());
    }

    @Test
    public void testWriter2() throws Exception {
        jw.writeObject(Data.data);
        System.out.println("\n\n"+sw.getBuffer().toString());
    }

    @Test
    public void testTree() throws Exception {
        final TreeNode<String> root = new TreeNode<String>("", null);
        root.addChildNode("n1", "node1");
        root.addChildNode("n2", "node2");
        root.findNode("n1").addChildNode("n11", "node1.1");
        System.out.println(root.debugInfo());
        jw.writeObject(root);
        System.out.println("\n\n"+sw.getBuffer().toString());
    }

}
