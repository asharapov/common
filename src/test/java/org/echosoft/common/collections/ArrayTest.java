package org.echosoft.common.collections;

import java.util.Iterator;

import org.echosoft.common.collections.iterators.ArrayIterator;
import org.echosoft.common.collections.iterators.ObjectArrayIterator;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class ArrayTest {

    @Test
    public void test1() throws Exception {
        final String[] a1 = new String[]{"a", "b", "c", "d", "e"};
        final Iterator<String> i1 = new ObjectArrayIterator<>(a1);
        final Iterator<String> i2 = new ObjectArrayIterator<>("a", "b", "c", "d", "e");
        final Iterator<Integer> i3 = new ObjectArrayIterator<>(0, 1, 2, 3, 4);
        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(i1.hasNext());
            Assert.assertTrue(i2.hasNext());
            Assert.assertTrue(i3.hasNext());
            final String v1 = i1.next();
            final String v2 = i2.next();
            final int v3 = i3.next();
            Assert.assertEquals(v1, v2);
            Assert.assertEquals(i, v3);
        }
        Assert.assertFalse(i1.hasNext());
        Assert.assertFalse(i2.hasNext());
        Assert.assertFalse(i3.hasNext());
    }

    @Test
    public void test2() throws Exception {
        final Iterator<String> i1 = new ObjectArrayIterator<>(new String[0]);
        final Iterator<String> i2 = new ObjectArrayIterator<>();
        Assert.assertFalse(i1.hasNext());
        Assert.assertFalse(i2.hasNext());
    }

    @Test
    public void test3() throws Exception {
        try {
            final Iterator<String> i1 = new ObjectArrayIterator<>((String[])null);
            Assert.fail("should be NullPointerException: " + i1);
        } catch (NullPointerException e) {
        }

        try {
            final String[] array = null;
            final Iterator<String> i1 = new ObjectArrayIterator<>(array);
            Assert.fail("should be NullPointerException: " + i1);
        } catch (NullPointerException e) {
        }

        // TODO: подумать над корректностью данной семантики вызова ...
        final Iterator<String> i1 = new ObjectArrayIterator<>((String)null);
        Assert.assertTrue(i1.hasNext());
        Assert.assertEquals(null, i1.next());
        Assert.assertFalse(i1.hasNext());
    }

    @Test
    public void test4() throws Exception {
        final String[] a1 = new String[]{"a", "b", "c", "d", "e"};
        final char[] a2 = new char[]{'a', 'b', 'c', 'd', 'e'};
        final int[] a3 = new int[]{0, 1, 2, 3, 4};
        final Iterator<String> i1 = new ArrayIterator<>(a1);
        final Iterator<Character> i2 = new ArrayIterator<>(a2);
        final Iterator<Integer> i3 = new ArrayIterator<>(a3);
        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(i1.hasNext());
            Assert.assertTrue(i2.hasNext());
            Assert.assertTrue(i3.hasNext());
            final String v1 = i1.next();
            final char v2 = i2.next();
            final int v3 = i3.next();
            Assert.assertEquals(v1, String.valueOf(v2));
            Assert.assertEquals(i, v3);
        }
        Assert.assertFalse(i1.hasNext());
        Assert.assertFalse(i2.hasNext());
        Assert.assertFalse(i3.hasNext());
    }

    @Test
    public void test5() throws Exception {
        final Iterator<String> i1 = new ArrayIterator<>(new String[0]);
        final Iterator<Byte> i2 = new ArrayIterator<>(new byte[0]);
        Assert.assertFalse(i1.hasNext());
        Assert.assertFalse(i2.hasNext());
    }

    @Test
    public void test6() throws Exception {
        try {
            final Iterator<String> i1 = new ArrayIterator<>((String[])null);
            Assert.fail("should be NullPointerException: " + i1);
        } catch (NullPointerException e) {
        }

        try {
            final Iterator<String> i1 = new ArrayIterator<>((int[])null);
            Assert.fail("should be NullPointerException: " + i1);
        } catch (NullPointerException e) {
        }

        try {
            final Iterator<String> i1 = new ArrayIterator<>("test");
            Assert.fail("should be IllegalArgumentException: " + i1);
        } catch (IllegalArgumentException e) {
        }

        try {
            final Iterator<Integer> i1 = new ArrayIterator<>(42);
            Assert.fail("should be IllegalArgumentException: " + i1);
        } catch (IllegalArgumentException e) {
        }
    }

}
