package org.echosoft.common.json;

import java.io.StringWriter;

import org.echosoft.common.json.beans.Data;
import org.echosoft.common.model.BasicReference;
import org.echosoft.common.model.TreeNode;
import org.echosoft.common.utils.StringUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class PrintableJsonWriterTest {

    private static StringWriter sw;
    private static JsonWriter jw;

    @BeforeClass
    public static void beforeClass() {
        sw = new StringWriter();
        jw = new PrintableJsonWriter(new JsonContext(), sw);
    }

    @Before
    public void setUp() {
        sw.getBuffer().setLength(0);
    }

    @Test
    public void testWriter1() throws Exception {
        jw.beginObject();
        jw.writeProperty("name", "Anton");
        jw.writeProperty("age", 32);
        jw.writeProperty("skills", new String[]{"A","B",null});
        jw.writeComplexProperty("items");
        jw.beginArray();
        jw.beginObject();
        jw.writeProperty("default", true);
        jw.writeProperty("date", StringUtil.parseDate("01.01.2009"));
        jw.endObject();
        jw.endArray();
        jw.writeComplexProperty("env");
        jw.beginObject();
        jw.writeProperty("path", "/usr/local");
        jw.writeProperty("owner", "admin");
        jw.writeComplexProperty("listeners");
        jw.beginObject();
        jw.writeProperty("oninit", new JSExpression("null"));
        jw.writeProperty("onchange", new JSExpression("function() {alert(this);}"));
        jw.writeProperty("director", new BasicReference("1", "Ivanov"));
        jw.endObject();
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
