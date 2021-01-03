package org.echosoft.common.utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Anton Sharapov
 */
public class ObjectUtilTest {

    @Test
    public void testClone() {
        final Calendar cal = Calendar.getInstance();
        final Date d1 = new Date();
        final Date d2 = ObjectUtil.clone(d1);
        Assert.assertTrue(d1!=d2);
        Assert.assertEquals(d1, d2);
    }

    @Test
    public void testLoadClass() throws Exception {
        final Date obj1 = ObjectUtil.makeInstance("java.util.Date", Date.class);
        Assert.assertNotNull(obj1);
        Assert.assertTrue(Date.class.equals(obj1.getClass()));
    }

    @Test
    public void testZipUnzip() throws Exception {
        final byte[][] testcases = {
                new byte[1024],
                "something test string for pack/unpack checking".getBytes()
        };
        for (byte[] data : testcases) {
            final byte[] packed = ObjectUtil.zipBytes(data);
            Assert.assertNotNull(packed);
            final byte[] unpacked = ObjectUtil.unzipBytes(packed);
            Assert.assertNotNull(packed);
            Assert.assertEquals(data.length, unpacked.length);
            Assert.assertTrue( Arrays.equals(data,unpacked) );
        }
    }

    @Test
    public void testDump() throws Exception {
        final byte[][] testcases = {
                new byte[1024],
                "something test string for pack/unpack checking".getBytes(),
                "мама мыла раму. Мы не рабы, рабы не мы.".getBytes()
        };

        for (byte[] data : testcases) {
            System.out.println("***");
            ObjectUtil.dump(System.out, data);
        }
        System.out.println("------------------");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIterator() throws Exception {
        final Iterator<Integer> it1 = ObjectUtil.makeIterator(Arrays.asList(1,2,3));
        Assert.assertEquals(it1.next(), new Integer(1));
        Assert.assertEquals(it1.next(), new Integer(2));
        Assert.assertEquals(it1.next(), new Integer(3));
        Assert.assertTrue(!it1.hasNext());
        final Iterator<String> it2 = ObjectUtil.makeIterator("1 ,2,3,");
        Assert.assertEquals(it2.next(), "1 ");
        Assert.assertEquals(it2.next(), "2");
        Assert.assertEquals(it2.next(), "3");
        Assert.assertTrue(!it2.hasNext());
        final Iterator<String> it3 = ObjectUtil.makeIterator("1 ,2,3, ");
        Assert.assertEquals(it3.next(), "1 ");
        Assert.assertEquals(it3.next(), "2");
        Assert.assertEquals(it3.next(), "3");
        Assert.assertEquals(it3.next(), " ");
        Assert.assertTrue(!it3.hasNext());
        final Iterator<Integer> it4 = ObjectUtil.makeIterator(new Integer[]{1,2,3});
        Assert.assertEquals(it4.next(), new Integer(1));
        Assert.assertEquals(it4.next(), new Integer(2));
        Assert.assertEquals(it4.next(), new Integer(3));
        Assert.assertTrue(!it4.hasNext());
    }


}
