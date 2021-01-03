package org.echosoft.common.types;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class TypesTest {

    @Test
    public void testType1() {
        final TypeRegistry reg = new TypeRegistry();
        reg.registerType(Types.INTEGER);
        reg.registerType(Types.BIGDECIMAL, Number.class);
//        reg.registerType(Types.DATETIME, java.sql.Timestamp.class);
    }

    @Test
    public void testFindType() {
        final TypeRegistry reg = new TypeRegistry();
        final Type<String> t1 = reg.findType(String.class);
        Assert.assertEquals(Types.STRING, t1);
        final Type<Integer> t2 = reg.findType(Integer.class);
        Assert.assertEquals(Types.INTEGER, t2);
        final Type<Integer> t3 = reg.findType(int.class);
        Assert.assertEquals(Types.INTEGER, t3);
        final Type<String[]> t4 = reg.findType(String[].class);
        Assert.assertEquals(Types.STRING_ARRAY, t4);
        final Type<? extends Number> t = reg.findType(Number.class);
        Assert.assertNotNull(t);
        Assert.assertTrue(Number.class.isAssignableFrom(t.getTarget()));
    }

    @Test
    public void testStringArrays() throws Exception {
        final Object[][] testcases = {
                new Object[]{null, null},
                new Object[]{new String[]{}, ""},
                new Object[]{new String[]{"a","b","c"}, "a,b,c"},
                new Object[]{new String[]{"a",",","c"}, "a,$,,c"}
        };
        final TypeRegistry reg = new TypeRegistry();
        final Type<String[]> t = reg.findType(String[].class);
        for (Object[] testcase : testcases) {
            final String[] tcParts = (String[])testcase[0];
            final String tcJoined = (String)testcase[1];
            final String joined = t.encode(tcParts);
            Assert.assertEquals(tcJoined, joined);
            final String[] parts = t.decode(joined);
            Assert.assertArrayEquals(tcParts, parts);
        }
    }

    @Test
    public void testIntegerArrays() throws Exception {
        final Object[][] testcases = {
                new Object[]{null, null},
                new Object[]{new Integer[]{}, ""},
                new Object[]{new Integer[]{1}, "1"},
                new Object[]{new Integer[]{1,2,3}, "1,2,3"},
                new Object[]{new Integer[]{-1,0,50000}, "-1,0,50000"}
        };
        final TypeRegistry reg = new TypeRegistry();
        final Type<Integer[]> t = reg.findType(Integer[].class);
        for (Object[] testcase : testcases) {
            final Integer[] tcParts = (Integer[])testcase[0];
            final String tcJoined = (String)testcase[1];
            final String joined = t.encode(tcParts);
            Assert.assertEquals(tcJoined, joined);
            final Integer[] parts = t.decode(joined);
            Assert.assertArrayEquals(tcParts, parts);
        }
    }

}
