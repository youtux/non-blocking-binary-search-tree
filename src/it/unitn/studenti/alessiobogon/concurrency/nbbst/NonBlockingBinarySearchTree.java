package it.unitn.studenti.alessiobogon.concurrency.nbbst;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.*;

import it.unitn.studenti.alessiobogon.concurrency.Set;

/**
 * Created by Alessio Bogon on 11/06/15.
 */

/**
 * A non-blocking unbalanced Binary Search Tree. This tree performs compare-and-set operations to avoid locking.
 * This class provides a logger, <tt>it.unitn.studenti.alessiobogon.concurrency.nbbst.NonBlockingBinarySearchTree</tt>,
 * with 3 levels of verbosity: FINE, FINER and FINEST.
 *
 * @param <T> the type of elements maintained by this tree
 */
public class NonBlockingBinarySearchTree<T> implements Set<T> {
    private static final Logger logger = Logger.getLogger(NonBlockingBinarySearchTree.class.getName());

    private final Node root;

    /**
     * Constructs a new, empty binary search tree set, sorted according to the hashCode of its elements.
     */
    public NonBlockingBinarySearchTree(){
        int inf1 = Integer.MAX_VALUE - 1;
        int inf2 = Integer.MAX_VALUE;

        root = new InternalNode(
            inf2,
            new Leaf<>(inf1),
            new Leaf<>(inf2),
            new Update(State.CLEAN, null)
        );
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     *
     * @param item element whose presence is to be tested
     * @return <tt>true</tt> if this set contains the specified element
     */
    @Override
    public boolean find(T item) {
        logger.log(Level.FINE, "ENTRY {0}", item);
        int key = item.hashCode();

        SearchResult result = search(key);

        logger.log(Level.FINE, "RETURN {0}", result.leaf.key == key);
        return result.leaf.key == key;
    }

    /**
     * Adds the specified element to this set, if it is not already present.
     *
     * @param item element to be inserted
     * @return <tt>true</tt> if the set did not contain the element
     */
    @Override
    public boolean insert(T item){
        logger.log(Level.FINE, "ENTRY {0}", item);
        int key = item.hashCode();

        Leaf newLeaf = new Leaf<>(item);

        while (true){
            SearchResult searchResult = search(key);
            InternalNode parent = searchResult.parent;
            Leaf leaf = searchResult.leaf;
            Update parentUpdate = searchResult.parentUpdate;

            if (leaf.key == key){
                // Cannot insert duplicate key
                logger.log(Level.FINE, "RETURN {0}", false);
                return false;
            }
            if (parentUpdate.state != State.CLEAN){
                // Help the other operation
                help(parentUpdate);
                continue;
            }

            Leaf newSibling = new Leaf<>(leaf.item);
            Leaf left, right;

            if (newLeaf.key < newSibling.key){
                left = newLeaf;
                right = newSibling;
            }else{
                left = newSibling;
                right = newLeaf;
            }
            InternalNode newInternal = new InternalNode(
                Math.max(key, leaf.key),
                left, right,
                new Update(State.CLEAN, null)
            );
            InsertInfo operation = new InsertInfo(parent, leaf, newInternal);

            boolean CASSuccess = parent.update.compareAndSet(parentUpdate, new Update(State.IFLAG, operation));
            if (CASSuccess){
                logger.finest("iflag[key=" + parent.key + "] success");
                helpInsert(operation);

                logger.log(Level.FINE, "RETURN {0}", true);
                return true;
            }else{
                logger.finest("iflag[key=" + parent.key + "] fail");
                help(parent.update.get());
            }
        }
    }

    /**
     * Removes the specified element to this set, if it exists.
     *
     * @param item element to be deleted
     * @return <tt>true</tt> if the set did contain the element
     */
    @Override
    public boolean delete(T item) {
        logger.log(Level.FINE, "ENTRY {0}", item);
        int key = item.hashCode();

        while (true) {
            SearchResult searchResult = search(key);
            InternalNode grandParent = searchResult.grandParent;
            InternalNode parent = searchResult.parent;
            Leaf leaf = searchResult.leaf;
            Update parentUpdate = searchResult.parentUpdate;
            Update grandParentUpdate = searchResult.grandParentUpdate;

            if (leaf.key != key){
                logger.log(Level.FINE, "RETURN {0}", false);
                return false;
            }

            if (grandParentUpdate.state != State.CLEAN) {
                help(grandParentUpdate);
                continue;
            }
            if (parentUpdate.state != State.CLEAN) {
                help(parentUpdate);
                continue;
            }

            DeleteInfo operation = new DeleteInfo(grandParent, parent, leaf, parentUpdate);

            boolean CASSuccess = grandParent.update.compareAndSet(
                grandParentUpdate,
                new Update(State.DFLAG, operation)
            );

            if (CASSuccess){
                logger.finest("dflag[key=" + key + "] success");
                if (helpDelete(operation)) {
                    logger.log(Level.FINE, "RETURN {0}", true);
                    return true;
                }
            }else{
                logger.finest("dflag[key=" + key + "] fail");
                help(grandParent.update.get());
            }
        }
    }

    /**
     * Dispatch the update operation to the right method (e.g. IFLAG to helpInsert,
     * MARK to helpMarked, etc.)
     * @param u the operation to help
     */
    private void help(Update u) {
        logger.log(Level.FINER, "ENTRY {0}", u);
        switch (u.state) {
            case IFLAG:
                helpInsert((InsertInfo) u.info);
                break;
            case MARK:
                helpMarked((DeleteInfo) u.info);
                break;
            case DFLAG:
                helpDelete((DeleteInfo) u.info);
                break;
        }
        logger.log(Level.FINER, "RETURN");
    }

    /**
     * Help the item insertion of the given operation.
     * This method will perform a parent <tt>ichild</tt> and the it will <tt>iunflag</tt> the parent.
     * @param operation The <tt>insert</tt> operation to help.
     */
    private void helpInsert(InsertInfo operation) {
        logger.log(Level.FINER, "ENTRY {0}", operation);

        compareAndSetChild(operation.parent, operation.leaf, operation.newInternal);
        logger.finest("ichild[key=" + operation.parent.key + "]");

        compareAndSetUpdate(operation.parent.update,
                new Update(State.IFLAG, operation),
                new Update(State.CLEAN, operation));
        logger.finest("iunflag[key=" + operation.parent.key + "]");

        logger.log(Level.FINER, "RETURN");
    }

    /**
     * Help the physical item removal of the given operation.
     * This method will set the grandparent.parent to the right child of the parent (<tt>dchild</tt>).
     * After that, it will <tt>dunflag</tt> the grandparent.
     * @param operation The <tt>mark</tt> operation to help
     */
    private void helpMarked(DeleteInfo operation) {
        logger.log(Level.FINER, "ENTRY {0}", operation);
        // Set other to point to the sibling of the node to which operation.leaf points
        Node other;
        if (operation.parent.right.get() == operation.leaf)
            other = operation.parent.left.get();
        else
            other = operation.parent.right.get();

        // Splice the node to which operation.parent points out of the tree, replacing it by other
        compareAndSetChild(operation.grandParent, operation.parent, other);             // dchild CAS
        logger.finest("dchild[key=" + operation.grandParent.key + "]");

        compareAndSetUpdate(operation.grandParent.update,
                new Update(State.DFLAG, operation), new Update(State.CLEAN, operation));    // dunflag CAS
        logger.finest("dunflag[key=" + operation.grandParent.key + "]");

        logger.log(Level.FINER, "RETURN");
    }

    /**
     * Help the logical item removal of the given operation.
     * This method will try to <tt>MARK</tt> the parent. If the compare-and-set succeeds, or if some other thread helped
     * this operation, the precedure can continue and this method will return <tt>true</tt>, meaning that the removal
     * operation has been completed.
     * Otherwise it will perform a backtrack compare-and-set, cleaning the state of the grandparent (ensuring that its
     * state did not change), and return <tt>false</tt>, indicating that the removal procedure should be restarted.
     * @param operation The <tt>delete</tt> operation to help.
     * @return <tt>true</tt> if the element has been removed.
     */
    private boolean helpDelete(DeleteInfo operation) {
        logger.log(Level.FINER, "ENTRY {0}", operation);

        Update newUpdate = new Update(State.MARK, operation);

        boolean CASSuccess = operation.parent.update.compareAndSet(
                operation.parentUpdate,
                newUpdate);

        Update result = operation.parent.update.get();
        boolean alreadyMarked = result.equals(new Update(State.MARK, operation));

        if (CASSuccess){
            logger.finest("mark[key=" + operation.parent.key + "] success");
        }else if (alreadyMarked) {
            logger.finest("mark[key=" + operation.parent.key + "] done by another thread");
        }else {
            logger.finest("mark[key=" + operation.parent.key + "] fail");
        }

        if (CASSuccess || alreadyMarked) {
            helpMarked(operation);

            logger.log(Level.FINER, "RETURN {0}", true);
            return true;
        } else {
            help(result);

            // backtrack CAS
            compareAndSetUpdate(operation.grandParent.update,
                    new Update(State.DFLAG, operation),
                    new Update(State.CLEAN, operation));
            logger.finest("backtrack[grandParent=" + operation.grandParent.key + "]");

            logger.log(Level.FINER, "RETURN {0}", false);
            return false;
        }
    }

    /**
     * Traverse the tree looking for the element with the given key.
     * Returns a <tt>SearchResult</tt> which is a snapshot containing information about the node, its parent,
     * its grandparent and their update fields. This result may contain a leaf with key greater than the given one, in
     * case the given one is not present in the tree.
     * @param key The key to search in the set
     * @return a <tt>SearchResult</tt> containing the snapshot of the fields.
     */
    private SearchResult search(int key) {
        logger.log(Level.FINER, "ENTRY {0}", key);

        InternalNode grandParent = null,
            parent = null;
        Node leaf = root;
        Update grandParentUpdate = null,
            parentUpdate = null;

        while (leaf instanceof InternalNode){
            grandParent = parent;
            parent = (InternalNode) leaf;
            grandParentUpdate = parentUpdate;

            parentUpdate = parent.update.get();
            if (key < leaf.key){
                leaf = parent.left.get();
            }else{
                leaf = parent.right.get();
            }
        }
        SearchResult result = new SearchResult(grandParent, parent, (Leaf) leaf, grandParentUpdate, parentUpdate);

        logger.log(Level.FINER, "RETURN {0}", result);
        return result;
    }

    /**
     * Performs a compare-and-set operation on the correct child of <tt>parent</tt>.
     * @param parent The parent node
     * @param oldChild The expected old child
     * @param newChild The new child to set
     * @return
     */
    private static boolean compareAndSetChild(InternalNode parent, Node oldChild, Node newChild) {
        AtomicReference<Node> childUpdater = newChild.key < parent.key ? parent.left : parent.right;
        return childUpdater.compareAndSet(oldChild, newChild);
    }

    /**
     * Convenience method that performs a compare-and-set operation using the <tt>.equals()</tt> comparison.
     * This method will obtain a snapshot <tt>tmp</tt> of the object wrapped by <tt>reference</tt>, compare it against
     * the <tt>expectedUpdate</tt> using <tt>expectedUpdate.equals(tmp)</tt>, and if it returns true then it will
     * perform the compare-and-set operation using the <tt>tmp</tt> field as expected reference.
     * @param reference the AtomicReference on which to call the <tt>compareAndSet</tt> method
     * @param expectedUpdate the expected value of the reference (compared using <tt>.equals()</tt>)
     * @param newUpdate the new value to set
     * @return <tt>true</tt> if the operation succeeded according to the usual compare-and-set semantics.
     */
    private static boolean compareAndSetUpdate(AtomicReference<Update> reference, Update expectedUpdate, Update newUpdate){
        Update tmp = reference.get();
        if (expectedUpdate.equals(tmp)){
            return reference.compareAndSet(tmp, newUpdate);
        }
        return false;
    }

    @Override
    public String toString() {
        return root.toString();
    }

    /**
     * Returns a <tt>String</tt> containing the "dot" format representation of the tree.
     * <strong>Note that this implementation is not synchronized.</strong> You need to ensure that the tree is not
     * being modified while calling this method.
     * @return the "dot" format representation of the tree.
     */
    public String dotify() {
        String result = "strict digraph {\n";
        result += dotifyAux(root);
        result += "}";

        return result;
    }

    /**
     * Auxiliary function that traverse the tree recursively and generates the "dot" entries.
     * @param n the starting node
     * @return the dot string for the subtree induced by n
     */
    private String dotifyAux(Node n) {
        String result = "";
        if (n instanceof Leaf){
            Leaf node = (Leaf) n;
            result += "\t" + dotifyNodeMetadata(node) + ";\n";
        } else {
            InternalNode node = (InternalNode) n;
            String sameRank = "\t{rank=same ";

            Node left = node.left.get(), right = node.right.get();
            if (left != null) {
                result += "\t" + dotifyNodeName(node) + " -> " + dotifyNodeName(left) + ";\n";
                result += dotifyAux(left);
                sameRank += dotifyNodeName(left) + " -> ";
            }
            result += "\t" + dotifyNodeName(node) + " -> Space_" + node.key + " [style=invis];\n";

            sameRank += "Space_" + node.key;
            if (right != null) {
                result += "\t" + dotifyNodeName(node) + " -> " + dotifyNodeName(right) + ";\n";
                result += dotifyAux(right);
                sameRank += " -> " + dotifyNodeName(right);
            }
            sameRank += "[style=invis]}\n";

            result += "\t" + dotifyNodeMetadata(node) + ";\n";
            result += sameRank;
            result += "\t" + dotifySpaceMetadata(node) + ";\n";
        }
        return result;
    }

    /**
     * Returns the dot entry for the given node
     * @param node the node
     * @return the dot entry String
     */
    private static String dotifyNodeName(Node node){
        if (node instanceof Leaf) return "Leaf_" + node.key;
        if (node instanceof InternalNode) return "InternalNode_" + node.key;
        return "";
    }

    /**
     * Returns the dot entry metadata for the given node
     * @param node the node
     * @return the dot entry metadata String
     */
    private static String dotifyNodeMetadata(Node node) {
        if (node instanceof Leaf) {
            return dotifyNodeName(node) + " [shape=box, label=" + ((Leaf) node).item + "]";
        }
        if (node instanceof InternalNode) {
            return dotifyNodeName(node) + " [label=" + node.key + "]";
        }
        return "";
    }

    /**
     * Returns the dot entry for an empty space to be inserted between the children
     * of the parent
     * @param parent the parent
     * @return the dot entry String
     */
    private String dotifySpaceMetadata(InternalNode parent) {
        return "Space_" + parent.key + " [label=\"\",width=1,style=invis]";
    }
}
