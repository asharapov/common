package org.echosoft.common.model;

import java.util.Comparator;
import java.util.Iterator;

import org.echosoft.common.utils.StringUtil;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class TreeNodeTest {

    private static final Predicate<TreeNode<String>> P2 =
            new Predicate<TreeNode<String>>() {
                public boolean accept(final TreeNode<String> node) {
                    return node.getData()==null;
                }
            };
    private static final Comparator<TreeNode<String>> STR_CMP =
            new Comparator<TreeNode<String>>() {
                public int compare(final TreeNode<String> n1, final TreeNode<String> n2) {
                    return StringUtil.compareNullableStrings(n1.getData(), n2.getData());
                }
            };

    @Test
    public void testTree() throws Exception {
        System.out.println("\nentire tree: ");
        final TreeNode<String> tree = new TreeNode<String>("", "<root>", STR_CMP);
        final TreeNode<String> n1 = tree.addChildNode("n1", "Data for N1");
        final TreeNode<String> n2 = tree.addChildNode("n2", "Data for N2");
        final TreeNode<String> n3 = tree.addChildNode("n3", "Data for N3");
        final TreeNode<String> n4 = tree.addChildNode("n4", "Data for N4");
        final TreeNode<String> n11 = n1.addChildNode("n11", "Data for N11");
        n11.addChildNode("n111", "Data for N111");
        n11.addChildNode("n112", "Data for N112");
        n11.addChildNode("n113", null);
        final TreeNode<String> n31 = n3.addChildNode("n31", null);
        n31.addChildNode("n311", "Data for N311");
        final TreeNode<String> n32 = n3.addChildNode("n32", "Data for N32");
        n32.addChildNode("n321", "Data for N321");
        final TreeNode<String> n322 = n32.addChildNode("n322", "Data for N322");
        n322.addChildNode("n3221", "Data for N3221");
        tree.debugInfo(System.out);
        System.out.println("\ntraverse tree: ");
        for (Iterator<TreeNode<String>> it=tree.traverseChildNodes(); it.hasNext(); ) {
            final TreeNode<String> node = it.next();
            System.out.println(node);
        }
        System.out.println("\nsuper tree (root): ");
        tree.filter(P2).debugInfo(System.out);

    }
}
