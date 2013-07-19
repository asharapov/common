package org.echosoft.common.data.misc;

import java.util.Comparator;

import org.echosoft.common.data.Predicate;
import org.echosoft.common.data.misc.TreeNode;
import org.echosoft.common.utils.StringUtil;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class TreeNodeTest {

    private static final Predicate<TreeNode<String, String>> P2 =
            new Predicate<TreeNode<String, String>>() {
                public boolean accept(final TreeNode<String, String> node) {
                    return node.getData() == null;
                }
            };
    private static final Comparator<TreeNode<String, String>> STR_CMP =
            new Comparator<TreeNode<String, String>>() {
                public int compare(final TreeNode<String, String> n1, final TreeNode<String, String> n2) {
                    return StringUtil.compareNullableStrings(n1.getData(), n2.getData());
                }
            };

    @Test
    public void testTree() throws Exception {
        System.out.println("\nentire tree: ");
        final TreeNode<String, String> tree = new TreeNode<String, String>("", "<root>");
        final TreeNode<String, String> n1 = tree.addChildNode("n1", "Data for N1");
        final TreeNode<String, String> n2 = tree.addChildNode("n2", "Data for N2");
        final TreeNode<String, String> n3 = tree.addChildNode("n3", "Data for N3");
        final TreeNode<String, String> n4 = tree.addChildNode("n4", "Data for N4");
        final TreeNode<String, String> n11 = n1.addChildNode("n11", "Data for N11");
        n11.addChildNode("n111", "Data for N111");
        n11.addChildNode("n112", "Data for N112");
        n11.addChildNode("n113", null);
        final TreeNode<String, String> n31 = n3.addChildNode("n31", null);
        n31.addChildNode("n311", "Data for N311");
        final TreeNode<String, String> n32 = n3.addChildNode("n32", "Data for N32");
        n32.addChildNode("n321", "Data for N321");
        final TreeNode<String, String> n322 = n32.addChildNode("n322", "Data for N322");
        n322.addChildNode("n3221", "Data for N3221");
        tree.debugInfo(System.out);
        System.out.println("\ntraverse tree: ");
        for (TreeNode<String, String> node : tree.traverseNodes(false)) {
            System.out.println(node);
        }
    }
}
