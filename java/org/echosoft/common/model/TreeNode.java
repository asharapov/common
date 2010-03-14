package org.echosoft.common.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.echosoft.common.collections.FilterIterator;
import org.echosoft.common.json.annotate.JsonField;
import org.echosoft.common.utils.StringUtil;

/**
 * Class <code>TreeNode</code> handles a node in a tree hierarchy.
 *
 * @author Anton Sharapov
 */
public class TreeNode<T> implements Serializable {

    protected final String id;
    protected final TreeNode<T> parent;
    protected final List<TreeNode<T>> children;
    protected final Comparator<TreeNode<T>> comparator;
    protected final HashMap<String, TreeNode<T>> nodes;
    protected T data;

    /**
     * Creates root node of tree.
     *
     * @param id   unique node identifier. Mandatory argument.
     * @param data any user-related java bean that associated with this node.
     */
    public TreeNode(final String id, final T data) {
        this(id, data, null);
    }

    /**
     * Creates root node of sorted tree.
     *
     * @param id         unique node identifier. Mandatory argument.
     * @param data       any user-related java bean that associated with this node.
     * @param comparator sort order of tree node children
     */
    public TreeNode(final String id, final T data, final Comparator<TreeNode<T>> comparator) {
        if (id == null)
            throw new IllegalArgumentException("Node identifier shoud be specified");
        this.id = id;
        this.parent = null;
        this.children = new ArrayList<TreeNode<T>>(4);
        this.comparator = comparator;
        this.nodes = new HashMap<String, TreeNode<T>>();
        this.data = data;
        nodes.put(id, this);
    }

    /**
     * Creates non root node of tree.
     *
     * @param parent     parent of this node
     * @param id         unique node identifier. Mandatory argument.
     * @param data       any user-related java bean that associated with this node.
     * @param comparator sort order of tree node children
     */
    protected TreeNode(final TreeNode<T> parent, final String id, final T data, final Comparator<TreeNode<T>> comparator) {
        if (id == null)
            throw new IllegalArgumentException("Node identifier shoud be specified");
        if (parent == null)
            throw new IllegalArgumentException("Parent node should be specified");

        this.id = id;
        this.parent = parent;
        this.children = new ArrayList<TreeNode<T>>(4);
        this.comparator = comparator;
        this.nodes = this.parent.nodes;
        this.data = data;
    }

    /**
     * Returns non null reference to root node of that features tree.
     *
     * @return root  root node in tree.
     */
    @JsonField(isTransient = true)
    public TreeNode<T> getRoot() {
        TreeNode<T> root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    /**
     * Returns reference to parent node of tree.
     *
     * @return parent feature or <code>null</code> for root node.
     */
    @JsonField(isTransient = true)
    public TreeNode<T> getParent() {
        return parent;
    }

    /**
     * Returns level of the given node in tree, starting with 0.
     * Root feature in tree has level 0.
     *
     * @return node's level in tree.
     */
    public int getLevel() {
        int level = 0;
        for (TreeNode<T> p = parent; p != null; p = p.parent) level++;
        return level;
    }

    /**
     * Returns comparator that used for child nodes sorting.
     *
     * @return a comparator or <code>null</code> if it not specified.
     */
    @JsonField(isTransient = true)
    public Comparator<TreeNode<T>> getComparator() {
        return comparator;
    }

    /**
     * Returns unique identifier for this tree node.
     * This identifier should be unique for entire nodes of tree.
     *
     * @return unique identifier of node.
     */
    public String getId() {
        return id;
    }


    /**
     * Retrieves associated with this node some java bean.
     *
     * @return java bean, associated with this node.
     */
    public T getData() {
        return data;
    }

    /**
     * Sets a java beana ssociated with this node.
     *
     * @param data java bean, associated with this node.
     */
    public void setData(final T data) {
        this.data = data;
    }

    /**
     * Retrieves <code>true</code> if this node is leaf, i.e. does not have any child nodes.
     *
     * @return <code>true</code> if this node does not have any child nodes,
     *         <code>false</code> otherwise.
     */
    @JsonField(isTransient = true)
    public boolean isLeaf() {
        return children.size() == 0;
    }

    /**
     * Determines weather this node has children.
     *
     * @return <code>true</code> if this node does have child nodes,
     *         <code>false</code> otherwise.
     */
    @JsonField(isTransient = true)
    public boolean hasChildren() {
        return children.size() != 0;
    }

    /**
     * Returns an unmodified list of all direct child nodes.
     *
     * @return unmodified list of children nodes..
     */
    public List<TreeNode<T>> getChildren() {
        return Collections.unmodifiableList(children);
    }


    /**
     * Recursively walks through all child nodes.
     *
     * @return an iterator of the all children nodes (recursively).
     */
    public Iterator<TreeNode<T>> traverseChildNodes() {
        return new TreeNodeWalker<T>(this);
    }

    /**
     * Recursively walks through all child nodes.
     *
     * @param condition node
     * @return an iterator of the all children nodes (recursively).
     */
    @SuppressWarnings({"unchecked"})
    public Iterator<TreeNode<T>> traverseChildNodes(final Predicate<TreeNode<T>> condition) {
        final Iterator<TreeNode<T>> it = traverseChildNodes();
        return condition != null
                ? (Iterator<TreeNode<T>>) new FilterIterator(it, condition)
                : it;
    }

    /**
     * Find node by its unique identifier in the subtree.
     *
     * @param id unique identifier of node that should be finded in the subtree.
     * @return reference to the corresponding node with the same identifier
     *         or <code>null</code> if such node does not belong to this tree.
     */
    public TreeNode<T> findNode(final String id) {
        if (this.id.equals(id))
            return this;
        for (TreeNode<T> node : children) {
            final TreeNode<T> result = node.findNode(id);
            if (result != null)
                return result;
        }
        return null;
    }

    /**
     * Find node by its unique identifier in the subtree.
     *
     * @param id unique identifier of node that should be finded in the subtree.
     * @return reference to the corresponding node with the same identifier or <code>null</code>
     *         if such node does not belong to this tree.
     */
    public TreeNode<T> findInTree(final String id) {
        return nodes.get(id);
    }

    /**
     * @param id - node identity to check
     * @return true if tree contains the node with given identity
     */
    public boolean containsInTree(final String id) {
        return nodes.containsKey(id);
    }

    /**
     * @param node - node to check
     * @return true if tree contains given node
     */
    public boolean containsInTree(final TreeNode<T> node) {
        return nodes.containsKey(node.id);
    }

    /**
     * This method removes given node and all children of him from tree.
     * If given node is root node then only children nodes will be removed.
     */
    public void remove() {
        if (parent != null) {
            parent.children.remove(this);
        } else {
            children.clear();
        }
    }

    /**
     * Add new child feature with specified id. If features tree already contains group with the same id then exception will be thrown.
     *
     * @param id   unique id of child feature to be created. Mandatory argument.
     * @param data any user-related java bean that associated with this node.
     * @return new child node.
     * @throws IllegalArgumentException if tree already contains node with same id.
     */
    public TreeNode<T> addChildNode(final String id, final T data) {
        return addChildNode(id, data, comparator);
    }

    /**
     * Add new child feature with specified id. If features tree already contains group with the same id then exception will be thrown.
     *
     * @param id         unique id of child feature to be created. Mandatory argument.
     * @param data       any user-related java bean that associated with this node.
     * @param comparator specifies sort order for children nodes.
     * @return new child node.
     * @throws IllegalArgumentException if tree already contains node with same id.
     */
    public TreeNode<T> addChildNode(final String id, final T data, final Comparator<TreeNode<T>> comparator) {
        if (containsInTree(id))
            throw new IllegalArgumentException("Node with id [" + id + "] already exists in tree");
        final TreeNode<T> node = new TreeNode<T>(this, id, data, comparator);
        children.add(node);
        nodes.put(id, node);
        if (this.comparator != null && children.size() > 1) {
            Collections.sort(children, this.comparator);
        }
        return node;
    }

    /**
     * Returns the copy of a supertree containing given nodes
     *
     * @param condition  node filter. Specifies what nodes should be copied to new structure. Can be <code>null</code>.
     * @return copy of a supertree containing given nodes
     */
    public TreeNode<T> filter(final Predicate<TreeNode<T>> condition) {
        final TreeNode<T> subtree = new TreeNode<T>(id, data, comparator);
        final LinkedList<TreeNode<T>> stack = new LinkedList<TreeNode<T>>();
        for (Iterator<TreeNode<T>> it = traverseChildNodes(condition); it.hasNext(); ) {
            // this stack contains node from source tree
            TreeNode<T> node = it.next();
            while (!id.equals(node.getId())) {
                stack.push(node);
                node = node.parent;
            }
            // do not change !
            TreeNode<T> parent = subtree;
            while (!stack.isEmpty()) {
                final TreeNode<T> child = stack.pop();
                node=subtree.findInTree(child.id);
                parent = node!=null
                        ? node
                        : parent.addChildNode(child.id, child.data);
            }
        }
        return subtree;
    }


    /**
     * Pretty prints nodes ierarchy, starting with the current node. Can be used for debug purposes.
     *
     * @return string representation of this subtree.
     */
    public String debugInfo() {
        try {
            final StringBuilder out = new StringBuilder(512);
            debugInfo(out);
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public void debugInfo(final Appendable out) throws IOException {
        final String prefix = StringUtil.leadLeft("", ' ', getLevel() * 2);
        out.append(prefix);
        out.append("TreeNode{id:");
        out.append(id);
        out.append(", data:");
        out.append(data != null ? data.toString() : "");
        out.append("}\n");
        for (TreeNode<T> node : children) {
            node.debugInfo(out);
        }
    }

    public int hashCode() {
        return id.hashCode();
    }

    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final TreeNode other = (TreeNode) obj;
        return id.equals(other.id) &&
                (data != null ? data.equals(other.data) : other.data == null) &&
                (parent != null ? parent.equals(other.parent) : other.parent == null);
    }

    public String toString() {
        return "[TreeNode{id:" + id + ", parent:" + (parent != null ? parent.id : "null") + ", data:" + (data != null ? data.toString() : "null") + "}]";
    }



    private static final class TreeNodeWalker<T> implements Iterator<TreeNode<T>> {
        private final int startLevel;
        private final ArrayList<Integer> stack;
        private TreeNode<T> p;
        private TreeNode<T> next;

        public TreeNodeWalker(final TreeNode<T> parent) {
            startLevel = parent.getLevel();
            p = parent;
            stack = new ArrayList<Integer>();
            stack.add(0);
            findNextGroup();
        }

        public boolean hasNext() {
            return next != null;
        }

        public TreeNode<T> next() {
            if (next == null)
                throw new NoSuchElementException();
            final TreeNode<T> result = next;
            findNextGroup();
            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void findNextGroup() {
            assert p != null && (p.getLevel() + 1 - startLevel == stack.size());
            while (stack.size() > 0) {
                int pos = stack.get(stack.size() - 1);
                if (pos >= p.children.size()) {
                    stack.remove(stack.size() - 1);
                    if (stack.size() > 0) {
                        stack.set(stack.size() - 1, stack.get(stack.size() - 1) + 1);
                        p = p.parent;
                    }
                } else {
                    next = p.children.get(pos);
                    p = next;
                    stack.add(0);
                    return;
                }
            }
            next = null;
        }
    }
}
