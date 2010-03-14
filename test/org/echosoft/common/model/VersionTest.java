package org.echosoft.common.model;

import java.text.ParseException;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

/**
 * Contains junit test cases for {@link Version} class.
 *
 * @author Anton Sharapov
 */
public class VersionTest {

    private final TreeMap<String,Version> cases;
    public VersionTest() {
        super();
        cases = new TreeMap<String,Version>();
        cases.put("1", new Version(1, 0, 0));
        cases.put("1.2", new Version(1, 2, 0));
        cases.put("1.2.3", new Version(1,2,3));
        cases.put("1.2.3.", new Version(1,2,3, "."));
        cases.put("ash", null);
        cases.put("1ash", null);
        cases.put("1.2ash", null);
        cases.put("1.2.3.ash", new Version(1,2,3, ".ash"));
        cases.put("2", new Version(2,0,0));
        cases.put("1.3", new Version(1,3,0));
        cases.put("1.3.1", new Version(1,3,1));
        cases.put(" 1 .3.1 ", null);
        cases.put("0.0.1-test", new Version(0,0,1,"-test"));
        cases.put("0.0.1-ash", new Version(0,0,1,"-ash"));
        cases.put("0.0.1", new Version(0,0,1));
        cases.put(" 0.0.1 ", new Version(0,0,1));
    }

    @Test
    public void testParsing() {
        for (Map.Entry<String,Version> entry : cases.entrySet()) {
            Version v;
            try {
                v = Version.parseVersion(entry.getKey());
                Assert.assertEquals("Wrong version parsed", v, entry.getValue());
            } catch (ParseException e) {
                Assert.assertNull("Unable to parse version [" + entry.getKey() + "]: ", entry.getValue());
            }
        }
    }

    @Test
    public void testOrdering() {
        final TreeSet<Version> set = new TreeSet<Version>();
        for (Map.Entry<String,Version> entry : cases.entrySet()) {
            if (entry.getValue() != null)
                set.add(entry.getValue());
        }
        Assert.assertEquals("Incorrect versions counts", set.size(), 11);

        System.out.println("---------");
        for (Version version : set) {
            System.out.println(version);
        }
        System.out.println("---------");
    }
}
