package org.echosoft.common.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Contains tests for {@link PreferencesLoader} class.
 * 
 * @author Anton Sharapov
 */
public class PreferencesLoaderTest {

    private static final String TEST_DATA_1 =
            "\n"+
            " #start works...\n"+
            "user:sandbox.utils.version=0.7\n"+
            "sandbox.utils.test1.key11=k11\n"+
            "sandbox.utils.test1.key12=k12\n"+
            "sandbox.utils.test1.key13=\n"+
            "sandbox.utils.test2.key21=k21\n"+
            "sandbox.utils.test2.key22=k22\n"+
            "sandbox.utils.test2.key23=k23\\./\n"+
            "sandbox.utils.test\\.3.key=key31\n"+
            "sandbox.utils.test\\.3.key\\.=key32\n"+
            "sandbox.utils.test\\.3.\\.key\\.=key33\n"+
            "sandbox.utils.test4\\..\\.key\\.=key41\n"+
            "sandbox.utils.test4\\..\\.=key42\n"+
            "sandbox.utils.\\..\\.=key.1\n"+
            "sandbox.utils.\\..\\.\\.=key.2\n";

    private static final String TEST_DATA_2 =
            "test1=x\n"+
            "user:test2=y\n"+
            "user:sandbox.utils.version=0.7\n"+
            "sandbox.utils.test5.key1=${user:sandbox.utils.version}\n"+
            "sandbox.utils.test5.key2=[${sandbox.utils.version}]\n"+
            "sandbox.utils.test5.key3=${test1}[${sandbox.utils.version}]${test2}\n";


    @Before
    public void setUp() throws BackingStoreException {
        Preferences.userRoot().remove("test1");
        Preferences.userRoot().remove("test2");
        if (Preferences.userRoot().nodeExists("sandbox/utils")) {
            final Preferences node = Preferences.userRoot().node("sandbox/utils");
            node.removeNode();
        }
    }

    @Test
    public void testImport1() throws IOException, BackingStoreException {
        final StringReader reader = new StringReader(TEST_DATA_1);
        PreferencesLoader.importPreferences(reader, true);

        assertTrue("node sandbox/utils not exists", Preferences.userRoot().nodeExists("sandbox/utils"));
        final Preferences node = Preferences.userRoot().node("sandbox/utils");
        assertTrue("node 'test1' does not exists", node.nodeExists("test1"));
        assertTrue("node 'test2' does not exists", node.nodeExists("test2"));
        assertTrue("node 'test.3' does not exists", node.nodeExists("test.3"));
        assertTrue("node 'test4.' does not exists", node.nodeExists("test4."));
        assertTrue("node '.' does not exists", node.nodeExists("."));
        assertTrue("property 'version' has wrong value", "0.7".equals(node.get("version",null)));
        assertTrue("property 'test1/key11' has wrong value",  "k11".equals(node.node("test1").get("key11",null)));
        assertTrue("property 'test1/key12' has wrong value",  "k12".equals(node.node("test1").get("key12",null)));
        assertTrue("property 'test1/key13' has wrong value",  "".equals(node.node("test1").get("key13",null)));
        assertTrue("property 'test2/key21' has wrong value",  "k21".equals(node.node("test2").get("key21",null)));
        assertTrue("property 'test2/key22' has wrong value",  "k22".equals(node.node("test2").get("key22",null)));
        assertTrue("property 'test2/key23' has wrong value",  "k23\\./".equals(node.node("test2").get("key23",null)));
        assertTrue("property 'test.3/key' has wrong value",  "key31".equals(node.node("test.3").get("key",null)));
        assertTrue("property 'test.3/key.' has wrong value",  "key32".equals(node.node("test.3").get("key.",null)));
        assertTrue("property 'test.3/.key.' has wrong value",  "key33".equals(node.node("test.3").get(".key.",null)));
        assertTrue("property 'test4./.key.' has wrong value",  "key41".equals(node.node("test4.").get(".key.",null)));
        assertTrue("property 'test4./.' has wrong value",  "key42".equals(node.node("test4.").get(".",null)));
        assertTrue("property './.' has wrong value",  "key.1".equals(node.node(".").get(".",null)));
        assertTrue("property './..' has wrong value",  "key.2".equals(node.node(".").get("..",null)));
    }

    @Test
    public void testWriterExport() throws IOException, BackingStoreException {
        final StringReader reader = new StringReader(TEST_DATA_1);
        PreferencesLoader.importPreferences(reader, true);

        final StringWriter writer = new StringWriter();
        PreferencesLoader.exportPreferences(Preferences.userRoot().node("sandbox"), writer);

        final TreeSet<String> plan = parseLines(TEST_DATA_1);
        final TreeSet<String> fact = parseLines(writer.toString());
        assertTrue("import and export operations produce different data", checkDifference(plan,fact));
    }

    @Test
    public void testImport2() throws IOException, BackingStoreException {
        final StringReader reader = new StringReader(TEST_DATA_2);
        PreferencesLoader.importPreferences(reader, true);

        assertTrue("property test1 has wrong value", "x".equals(Preferences.userRoot().get("test1",null)));
        assertTrue("property test2 has wrong value", "y".equals(Preferences.userRoot().get("test2",null)));
        assertTrue("node sandbox/utils/test5 not exists", Preferences.userRoot().nodeExists("sandbox/utils/test5"));
        final Preferences node = Preferences.userRoot().node("sandbox/utils/test5");
        assertTrue("property 'test5/key1' has wrong value", "0.7".equals(node.get("key1",null)));
        assertTrue("property 'test5/key2' has wrong value", "[0.7]".equals(node.get("key2",null)));
        assertTrue("property 'test5/key3' has wrong value", "x[0.7]y".equals(node.get("key3",null)));
    }

    @Test
    public void testMapExport() throws BackingStoreException, IOException {
        final StringReader reader = new StringReader(TEST_DATA_1);
        PreferencesLoader.importPreferences(reader, true);

        final TreeMap<String,String> result = new TreeMap<String,String>();
        PreferencesLoader.exportPreferences(Preferences.userRoot().node("sandbox"), result, false);
        assertTrue("result map is empty", result.size()>0);
        final TreeMap<String,String> plan = parseMap(TEST_DATA_1, null);
        assertTrue("import and export operations produce different data", checkDifference(plan,result));
    }

    @Test
    public void testMapExport2() throws BackingStoreException, IOException {
        final StringReader reader = new StringReader(TEST_DATA_1);
        PreferencesLoader.importPreferences(reader, true);

        final TreeMap<String,String> result = new TreeMap<String,String>();
        PreferencesLoader.exportPreferences(Preferences.userRoot().node("sandbox/utils"), result, true);
        assertTrue("result map is empty", result.size()>0);
        final TreeMap<String,String> plan = parseMap(TEST_DATA_1, "sandbox.utils.");
        assertTrue("import and export operations produce different data", checkDifference(plan,result));
    }

    @Test
    public void testGetPreferenceValue() throws BackingStoreException, IOException {
        final StringReader reader = new StringReader(TEST_DATA_1);
        PreferencesLoader.importPreferences(reader, true);
        final TreeMap<String,String> plan = parseMap(TEST_DATA_1, null);
        for (Map.Entry<String,String> entry : plan.entrySet()) {
            final String realValue = PreferencesLoader.getPreferenceValue(entry.getKey());
            final boolean result = entry.getValue()!=null ? entry.getValue().equals(realValue) : realValue!=null;
            assertTrue("preference ["+entry.getKey()+"] value ["+entry.getValue()+"] != ["+realValue+"] ", result);
        }
    }




    private static TreeSet<String> parseLines(String text) {
        final TreeSet<String> result = new TreeSet<String>();
        if (text==null || text.length()==0)
            return result;
        for (StringTokenizer st = new StringTokenizer(text, "\n"); st.hasMoreTokens(); ) {
            String token = st.nextToken();
            if (token.trim().length()==0 || token.trim().startsWith("#"))
                continue;
            if (token.startsWith("user:"))
                token = token.substring("user:".length());
            result.add( token );
        }
        return result;
    }
    private static TreeMap<String,String> parseMap(String text, String skippedPrefix) {
        skippedPrefix = skippedPrefix==null ? "" : skippedPrefix;
        int prefixLength = skippedPrefix.length();

        final TreeMap<String,String> result = new TreeMap<String,String>();
        if (text==null || text.length()==0)
            return result;
        for (StringTokenizer st = new StringTokenizer(text, "\n"); st.hasMoreTokens(); ) {
            String token = st.nextToken();
            if (token.trim().length()==0 || token.trim().startsWith("#"))
                continue;
            if (token.startsWith("user:"))
                token = token.substring("user:".length());
            if (token.startsWith(skippedPrefix))
                token = token.substring(prefixLength);
            final int eq = token.indexOf('=');
            result.put(token.substring(0,eq), token.substring(eq+1));
        }
        return result;
    }

    private static boolean checkDifference(Set<String> plan, Set<String> fact) {
        final TreeSet<String> absent = new TreeSet<String>();
        final TreeSet<String> odd = new TreeSet<String>();
        for (String line : plan) {
            if (!fact.contains(line))
                absent.add(line);
        }
        for (String line: fact) {
            if (!plan.contains(line))
                odd.add(line);
        }
        if (!absent.isEmpty()) {
            System.out.println("absent expressions:");
            for (String line: absent) {
                System.out.println("  " + line);
            }
        }
        if (!odd.isEmpty()) {
            System.out.println("odd expressions:");
            for (String line : odd) {
                System.out.println("  " + line);
            }
        }
        return absent.isEmpty() && odd.isEmpty();
    }

    private static boolean checkDifference(Map<String,String> plan, Map<String,String> fact) {
        final TreeMap<String,String> absent = new TreeMap<String,String>();
        final TreeMap<String,String> odd = new TreeMap<String,String>();
        for (String key : plan.keySet()) {
            final String value = plan.get(key);
            if (!fact.containsKey(key)) {
                absent.put(key, value);
            } else {
                final String realValue = fact.get(key);
                if ((value != null && !value.equals(realValue)) || (value == null && realValue != null)) {
                    System.out.println("  " + key + "=" + value + " / " + realValue);
                }
            }
        }
        for (String key : fact.keySet()) {
            final String value = fact.get(key);
            if (!plan.containsKey(key))
                odd.put(key, value);
        }
        if (!absent.isEmpty()) {
            System.out.println("absent expressions:");
            for (String key : absent.keySet()) {
                System.out.println("  " + key + "=" + absent.get(key));
            }
        }
        if (!odd.isEmpty()) {
            System.out.println("odd expressions:");
            for (String key : odd.keySet()) {
                System.out.println("  " + key + "=" + odd.get(key));
            }
        }
        return plan.equals(fact);
    }

}
