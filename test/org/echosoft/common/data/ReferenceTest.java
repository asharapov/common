package org.echosoft.common.data;

import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class ReferenceTest {

    @Test
    public void testReference() {
        final SimpleEntity b1 = new SimpleEntity(1, "first bean");
        final Referenceable<Long, String> b2 = new SimpleEntity(2, "second bean");

        final Reference<Long, String> r1 = b1.getReference();
        process1(r1);
        //process2(r1);  // shouldn't be compiled
    }

    private void process1(Reference<Long, String> ref) {
        System.out.println(ref);
    }
    private void process2(Reference<String, String> ref) {
        System.out.println(ref);
    }

    public class SimpleEntity implements Entity<Long>, Referenceable<Long, String> {
        public Long id;
        public String name;
        public int amount;
        private transient Reference<Long, String> ref;

        public SimpleEntity(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public Reference<Long, String> getReference() {
            if (ref == null) {
                ref = new Reference<Long, String>() {
                    @Override
                    public Long getId() {
                        return id;
                    }
                    @Override
                    public String getText() {
                        return name;
                    }
                    @Override
                    public String toString() {
                        return "[SimpleEntity.Reference{id:" + getId() + ", desc:" + getText() + "}]";
                    }
                };
            }
            return ref;
        }

        @Override
        public String toString() {
            return "[SimpleEntity{id:" + id + ", name:" + name + ", amount:" + amount + "}]";
        }
    }
}
