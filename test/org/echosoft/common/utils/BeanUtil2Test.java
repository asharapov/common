package org.echosoft.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class BeanUtil2Test {

    private static MyBean root;

    @BeforeClass
    public void before() {
        root = new MyBean();
        root.id = "R";
        root.items = new MyItem[]{
                new MyItem("i1", "item 1"),
                new MyItem("i2", "item 2"),
                new MyItem("i3", "item 3")
        };
        root.map.put("a", "A");
        root.map.put("b", "B");
        MyBean x1 = root.append("x1");
        x1.items = new MyItem[]{
                new MyItem("x1.i1", "x1/item 1"),
                new MyItem("x1.i2", "x1/item 2"),
                new MyItem("x1.i3", "x1/item 3")
        };
        x1.map.put("x1.a", 1);
        x1.map.put("x1.b", 2.0);
        MyBean x11 = x1.append("x11");
        MyBean x12 = x1.append("x12");
        MyBean x2 = root.append("x2");
    }

    @Test
    public void testGetProperty1() throws Exception {
        Assert.assertEquals("R", BeanUtil2.getProperty(root, "id"));
        Assert.assertEquals("R", BeanUtil2.getProperty(root.children.get(0), "parent.id"));
        Assert.assertEquals(3, BeanUtil2.getProperty(root.children.get(0), "parent.children.size"));
    }


    public static class MyBean {
        public String id;
        public MyItem[] items;
        private final Map<String,Object> map = new TreeMap<String,Object>();
        private MyBean parent;
        private final List<MyBean> children = new ArrayList<MyBean>();

        public MyBean getParent() {
            return parent;
        }
        public void setParent(MyBean parent) {
            this.parent = parent;
        }

        public Map<String,Object> getMap() {
            return map;
        }

        public Object getMapValue(String key) {
            return  map.get(key);
        }

        public List<MyBean> getChildren() {
            return children;
        }
        public MyBean append(final String id) {
            final MyBean result = new MyBean();
            result.id = id;
            result.parent = this;
            children.add( result );
            return result;
        }
    }

    public static class MyItem {
        public MyBean owner;
        public String name;
        public String title;
        public MyItem(String name, String title) {
            this.name = name;
            this.title = title;
        }
    }
}
