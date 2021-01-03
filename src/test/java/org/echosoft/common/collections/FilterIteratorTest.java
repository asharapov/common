package org.echosoft.common.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.echosoft.common.model.Predicate;
import org.echosoft.common.model.Predicates;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class FilterIteratorTest {

    private static ArrayList<String> list;

    @Before
    public void setup() {
        list = new ArrayList<String>( Arrays.asList("aa", "bb", "cc", "dd", "ee", "gg", "1", "2", "ff") );
    }

    @Test
    public void testFilterAll() {
        final ArrayList<String> result = new ArrayList<String>();
        final Iterator<String> it = new FilterIterator<String>(list.iterator(), Predicates.<String>all());
        while ( it.hasNext() ) {
            result.add( it.next() );
        }
        Assert.assertEquals(list, result);
    }

    @Test
    public void testFilterNothing() {
        final ArrayList<String> result = new ArrayList<String>();
        final Iterator<String> it = new FilterIterator<String>(list.iterator(), Predicates.<String>nothing());
        while ( it.hasNext() ) {
            result.add( it.next() );
        }
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testFilterCustom() {
        final ArrayList<String> result = new ArrayList<String>();
        final Predicate<String> filter = new Predicate<String>() {
            public boolean accept(final String value) {
                return value!=null && value.length()==2;
            }
        };
        final Iterator<String> it = new FilterIterator<String>(list.iterator(), filter);
        while ( it.hasNext() ) {
            result.add( it.next() );
        }
        Assert.assertEquals(result, Arrays.asList("aa", "bb", "cc", "dd", "ee", "gg", "ff"));
    }

//    @Test
//    public void testRemove() {
//        final ArrayList<String> result = new ArrayList<String>();
//        final Iterator<String> it = new FilterIterator<String>(list.iterator(), Predicates.<String>all());
//        while ( it.hasNext() ) {
//            it.remove();
//        }
//        Assert.assertTrue(list.isEmpty());
//    }
}
