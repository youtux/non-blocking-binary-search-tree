package it.unitn.studenti.alessiobogon.concurrency.nbbst;

import it.unitn.studenti.alessiobogon.concurrency.Set;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
public class NonBlockingBinarySearchTree<T> implements Set<T> {
    private static final Logger log = Logger.getLogger(NonBlockingBinarySearchTree.class.getName());

    private final Node root;

    public NonBlockingBinarySearchTree(){
        int inf1 = Integer.MAX_VALUE - 1;
        int inf2 = Integer.MAX_VALUE;

        root = new InternalNode(
            inf2,
            new Leaf(inf1),
            new Leaf(inf2),
            new Update(State.CLEAN, null)
        );
    }
    @Override
    public boolean find(T item) {
        log.log(Level.FINE, "ENTRY {0}", item);
        int key = item.hashCode();

        SearchResult result = search(key);

        log.log(Level.FINE, "RETURN {0}", result.leaf.key == key);
        return result.leaf.key == key;
    }

    @Override
    public boolean insert(T item){
        log.log(Level.FINE, "ENTRY {0}", item);
        int key = item.hashCode();

        Leaf newLeaf = new Leaf<>(item);

        while (true){
            SearchResult searchResult = search(key);
            InternalNode parent = searchResult.parent;
            Leaf leaf = searchResult.leaf;
            Update parentUpdate = searchResult.parentUpdate;

            if (leaf.key == key){
                // Cannot insert duplicate key
                log.log(Level.FINE, "RETURN {0}", false);
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
                log.finest("iflag[key=" + parent.key + "] success");
                helpInsert(operation);

                log.log(Level.FINE, "RETURN {0}", true);
                return true;
            }else{
                log.finest("iflag[key=" + parent.key + "] fail");
                help(parent.update.get());
            }
        }
    }

    @Override
    public boolean delete(T item) {
        log.log(Level.FINE, "ENTRY {0}", item);
        int key = item.hashCode();

        while (true) {
            SearchResult searchResult = search(key);
            InternalNode grandParent = searchResult.grandParent;
            InternalNode parent = searchResult.parent;
            Leaf leaf = searchResult.leaf;
            Update parentUpdate = searchResult.parentUpdate;
            Update grandParentUpdate = searchResult.grandParentUpdate;

            if (leaf.key != key){
                log.log(Level.FINE, "RETURN {0}", false);
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
                log.finest("dflag[key=" + key + "] success");
                if (helpDelete(operation)) {
                    log.log(Level.FINE, "RETURN {0}", true);
                    return true;
                }
            }else{
                log.finest("dflag[key=" + key + "] fail");
                help(grandParent.update.get());
            }
        }
    }

    private void help(Update u) {
        log.log(Level.FINER, "ENTRY {0}", u);
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
        log.log(Level.FINER, "RETURN");
    }

    private void helpInsert(InsertInfo operation) {
        log.log(Level.FINER, "ENTRY {0}", operation);

        compareAndSetChild(operation.parent, operation.leaf, operation.newInternal);
        log.finest("ichild[key=" + operation.parent.key + "]");

        compareAndSetUpdate(operation.parent.update,
                new Update(State.IFLAG, operation),
                new Update(State.CLEAN, operation));
        log.finest("iunflag[key=" + operation.parent.key + "]");

        log.log(Level.FINER, "RETURN");
    }

    private void helpMarked(DeleteInfo operation) {
        log.log(Level.FINER, "ENTRY {0}", operation);
        // Set other to point to the sibling of the node to which operation.leaf points
        Node other;
        // TODO: Check if these gets are safe. They should be.
        if (operation.parent.right.get() == operation.leaf)
            other = operation.parent.left.get();
        else
            other = operation.parent.right.get();

        // Splice the node to which operation.parent points out of the tree, replacing it by other
        compareAndSetChild(operation.grandParent, operation.parent, other);             // dchild CAS
        log.finest("dchild[key=" + operation.grandParent.key + "]");

        compareAndSetUpdate(operation.grandParent.update,
                new Update(State.DFLAG, operation), new Update(State.CLEAN, operation));    // dunflag CAS
        log.finest("dunflag[key=" + operation.grandParent.key + "]");

        log.log(Level.FINER, "RETURN");
    }

    private boolean helpDelete(DeleteInfo operation) {
        log.log(Level.FINER, "ENTRY {0}", operation);

        Update newUpdate = new Update(State.MARK, operation);

        boolean CASSuccess = operation.parent.update.compareAndSet(
                operation.parentUpdate,
                newUpdate);

        Update result = operation.parent.update.get();
        boolean alreadyMarked = result.equals(operation.parentUpdate);

        if (CASSuccess){
            log.finest("mark[key=" + operation.parent.key + "] success");
        }else if (alreadyMarked) {
            log.finest("mark[key=" + operation.parent.key + "] done by another thread");
        }else {
            log.finest("mark[key=" + operation.parent.key + "] fail");
        }

        if (CASSuccess || alreadyMarked) {
            helpMarked(operation);

            log.log(Level.FINER, "RETURN {0}", true);
            return true;
        } else {
            help(result);

            // backtrack CAS
            compareAndSetUpdate(operation.grandParent.update,
                    new Update(State.DFLAG, operation),
                    new Update(State.CLEAN, operation));
            log.finest("backtrack[grandParent=" + operation.grandParent.key + "]");

            log.log(Level.FINER, "RETURN {0}", false);
            return false;
        }
    }

    private SearchResult search(int key) {
        log.log(Level.FINER, "ENTRY {0}", key);

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

        log.log(Level.FINER, "RETURN {0}", result);
        return result;
    }

    protected boolean compareAndSetChild(InternalNode parent, Node oldChild, Node newChild) {
        AtomicReference<Node> childUpdater = newChild.key < parent.key ? parent.left : parent.right;
        return childUpdater.compareAndSet(oldChild, newChild);
    }

    protected boolean compareAndSetUpdate(AtomicReference<Update> reference, Update expectedUpdate, Update newUpdate){
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

    public String dotify() {
        String result = "strict digraph {\n";
        result += dotifyAux(root);
        result += "}";

        return result;
    }

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

    private String dotifyNodeName(Node node){
        if (node instanceof Leaf) return "Leaf_" + node.key;
        if (node instanceof InternalNode) return "InternalNode_" + node.key;
        return "";
    }

    private String dotifyNodeMetadata(Node node) {
        if (node instanceof Leaf) {
            return dotifyNodeName(node) + " [shape=box, label=" + ((Leaf) node).item + "]";
        }
        if (node instanceof InternalNode) {
            return dotifyNodeName(node) + " [label=" + node.key + "]";
        }
        return "";
    }

    private String dotifySpaceMetadata(InternalNode parent) {
        return "Space_" + parent.key + " [label=\"\",width=1,style=invis]";
    }
}
