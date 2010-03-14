package org.echosoft.common.model;

import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class ReferenceTest {

    @Test
    public void testReference() {
        final SimpleEntity b1 = new SimpleEntity(1, "first bean");
        final Reference<SimpleEntity> r1 = b1.getReference();
        final BasicReference<SimpleEntity> r2 = new BasicReference<SimpleEntity>(2, "second bean");
        process1(r1);
        process2(r1);
//        process3(r1);  // shouldn't be compiled
        process1(r2);
        process2(r2);
//        process3(r2);  // shouldn't be compiled
    }

    private void process1(Reference<?> ref) {
        System.out.println(ref);
    }
    private void process2(Reference<SimpleEntity> ref) {
        System.out.println(ref);
    }
    private void process3(Reference<String> ref) {
        System.out.println(ref);
    }

    public class SimpleEntity {
        public long id;
        public String name;
        public int amount;
        public SimpleEntity(long id, String name) {
            this.id = id;
            this.name = name;
        }
        public String toString() {
            return "[SimpleEntity{id:"+id+", name:"+name+", amount:"+amount+"}]";
        }
        public Reference<SimpleEntity> getReference() {
            return new Reference<SimpleEntity>() {
                public String getKey() {
                    return Long.toString(id);
                }
                public long getKeyAsLong() {
                    return id;
                }
                public String getTitle() {
                    return name;
                }
                public String toString() {
                    return "[SimpleEntity.Reference{key:"+getKey()+", title:"+getTitle()+"}]";
                }
            };
        }
    }
}
