package org.echosoft.common.json;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;
import java.util.HashMap;

import org.echosoft.common.json.beans.Data;
import org.echosoft.common.data.misc.TreeNode;
import org.junit.After;
import org.junit.Assert;
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
        jw.getOutputWriter().write("result = ");
        jw.beginArray();
        jw.endArray();
        validateScript(sw.getBuffer().toString());
    }

    @Test
    public void testArray2() throws Exception {
        jw.getOutputWriter().write("result = ");
        jw.beginArray();
        jw.beginObject();
        jw.endObject();
        jw.beginObject();
        jw.endObject();
        jw.beginObject();
        jw.endObject();
        jw.endArray();
        validateScript(sw.getBuffer().toString());
    }

    @Test
    public void testArray3() throws Exception {
        jw.getOutputWriter().write("result = ");
        jw.beginArray();
        jw.writeObject( new Object() );
        jw.writeObject( new Object() );
        jw.writeObject( new Object() );
        jw.endArray();
        validateScript(sw.getBuffer().toString());
    }

    @Test
    public void testArray4() throws Exception {
        jw.getOutputWriter().write("result = ");
        jw.beginArray();
        jw.beginArray();
        jw.beginArray();
        jw.endArray();
        jw.endArray();
        jw.endArray();
        validateScript(sw.getBuffer().toString());
    }

    @Test
    public void testObject1() throws Exception {
        jw.getOutputWriter().write("result = ");
        jw.beginObject();
        jw.endObject();
        validateScript(sw.getBuffer().toString());
    }

    @Test
    public void testObject2() throws Exception {
        jw.getOutputWriter().write("result = ");
        jw.beginObject();
        jw.writeComplexProperty("a");
        jw.beginArray();
        jw.beginArray();
        jw.writeObject("A");
        jw.writeObject("B");
        jw.writeObject("C");
        jw.endArray();
        jw.endArray();
        jw.endObject();
        validateScript(sw.getBuffer().toString());
    }

    @Test
    public void testComplex1() throws Exception {
        jw.getOutputWriter().write("result = ");
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
        validateScript(sw.getBuffer().toString());
    }

    @Test
    public void testComplex2() throws Exception {
        jw.getOutputWriter().write("result = ");
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
        validateScript(sw.getBuffer().toString());
    }

    @Test
    public void testWriter2() throws Exception {
        jw.getOutputWriter().write("result = ");
        jw.writeObject(Data.data);
        validateScript("\n\n"+sw.getBuffer().toString());
    }

    @Test
    public void testTree() throws Exception {
        final TreeNode<String, String> root = new TreeNode<String, String>("", null);
        root.addChildNode("n1", "node1");
        root.addChildNode("n2", "node2");
        root.findNodeById("n1", true).addChildNode("n11", "node1.1");
        System.err.println(root.debugInfo());
        jw.getOutputWriter().write("result = ");
        jw.writeObject(root);
        validateScript("\n\n"+sw.getBuffer().toString());
    }


    private static Object validateScript(final String script) throws ScriptException {
        System.out.println(script);
        try {
            final ScriptEngineManager factory = new ScriptEngineManager();
            final ScriptEngine engine = factory.getEngineByName("JavaScript");
            final Object result = engine.eval(script);
            return result;
        } catch (Exception e) {
            Assert.fail("expression failed:\n "+script);
            return null;
        }
    }
}
