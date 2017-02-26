package org.echosoft.common.data.misc;

import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.echosoft.common.json.CompactJsonWriter;
import org.echosoft.common.json.JsonContext;
import org.echosoft.common.json.JsonWriter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Contains junit test cases for {@link Version} class.
 *
 * @author Anton Sharapov
 */
public class VersionTest {

    private final TreeMap<String, Version> cases;
    public VersionTest() {
        super();
        cases = new TreeMap<>();
        cases.put("1", new Version(1, 0));
        cases.put("1", new Version(1, 0, "0"));
        cases.put("1.2", new Version(1, 2, "0"));
        cases.put("1.2.3", new Version(1, 2, "3"));
        cases.put("1.2.3x", new Version(1, 2, null, "3x"));
        cases.put("1.2.3.", new Version(1, 2, "3", null));
        cases.put("1.2.3.4", new Version(1, 2, "3", "4"));
        cases.put("ash", null);
        cases.put("1ash", null);
        cases.put("1.2af", new Version(1, 0, "2af"));
        cases.put("1.2ash", new Version(1, 0, null, "2ash"));
        cases.put("1.2.3.ash", new Version(1, 2, "3", "ash"));
        cases.put("2", new Version(2));
        cases.put("1.3", new Version(1, 3));
        cases.put("1.3.1", new Version(1, 3, "1"));
        cases.put(" 1 .3.1 ", null /*new Version(1, 0, 0, ".3.1")*/);
        cases.put("0.0.1-test", new Version(0, 0, "1", "test"));
        cases.put("0.0.1-ash", new Version(0, 0, "1", "ash"));
        cases.put("0.0.1", new Version(0, 0, "1"));
        cases.put(" 0.0.1 ", new Version(0, 0, "1"));
    }

    @Test
    public void testX() {
        for (String src : new String[]{"1", "1.2", "1.2.3", "11.22.33AF", "0.1.f-test", "1.2.g-test", "1..2-3", ".1", "..1", "acf43", "0abc33-ash"}) {
            System.out.print("source = '" + src + "'   = ");
            final Version v = Version.parseVersion(src);
            System.out.println(v);
        }
        System.out.println("ok");
    }

    @Test
    public void testParsing() {
        for (Map.Entry<String, Version> entry : cases.entrySet()) {
            Version v;
            try {
                v = Version.parseVersion(entry.getKey());
                Assert.assertEquals(entry.getValue(), v);
            } catch (Exception e) {
                Assert.assertNull("Unable to parse version [" + entry.getKey() + "]: ", entry.getValue());
            }
        }
    }

    @Test
    public void testOrdering() {
        final TreeSet<Version> set = new TreeSet<>();
        for (Map.Entry<String, Version> entry : cases.entrySet()) {
            if (entry.getValue() != null)
                set.add(entry.getValue());
        }
        //Assert.assertEquals("Incorrect versions counts", set.size(), 17);

        System.out.println("---------");
        for (Version version : set) {
            System.out.println(version);
        }
        System.out.println("---------");
    }


    @Test
    public void testJson() throws Exception {
        final JsonContext jctx = new JsonContext();
        final StringWriter out = new StringWriter();
        final JsonWriter jw = new CompactJsonWriter(jctx, out);
        jw.beginArray();
        jw.writeObject(new Version(1));
        jw.writeObject(new Version(1, 2));
        jw.writeObject(new Version(1, 2, "3"));
        jw.writeObject(new Version(1, 2, "3", "x"));
        jw.writeObject(new Version(1, 2, null, "x"));
        jw.writeObject(new Version(1, 0, "ff"));
        jw.endArray();
        Assert.assertEquals("[{major:1},{major:1,minor:2},{major:1,minor:2,revision:\"3\"},{major:1,minor:2,revision:\"3\",extra:\"x\"},{major:1,minor:2,extra:\"x\"},{major:1,minor:0,revision:\"ff\"}]", out.toString());
    }
}
