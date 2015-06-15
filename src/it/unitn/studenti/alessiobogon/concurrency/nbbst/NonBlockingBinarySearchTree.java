package it.unitn.studenti.alessiobogon.concurrency.nbbst;

import it.unitn.studenti.alessiobogon.concurrency.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
public class NonBlockingBinarySearchTree<T> implements Set<T> {
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
        int key = item.hashCode();

        SearchResult result = search(key);
        return result.leaf.key == key;
    }

    @Override
    public boolean insert(T item){
        int key = item.hashCode();

        Leaf newLeaf = new Leaf(item);

        while (true){
            SearchResult searchResult = search(key);
            InternalNode parent = searchResult.parent;
            Leaf leaf = searchResult.leaf;
            Update parentUpdate = searchResult.parentUpdate;

            if (leaf.key == key){
                // Cannot insert duplicate key
                return false;
            }
            if (parentUpdate.state != State.CLEAN){
                // Help the other operation
                help(parentUpdate);
                continue;
            }

            Leaf newSibling = new Leaf(leaf.item);
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
                helpInsert(operation);
                return true;
            }else{
                // TODO: the iflag CAS failed. Help the operation that cause failure
                // TODO: check my correctness
                System.out.print("Insert CAS failed");
                help(parent.update.get());
            }


        }
    }


    @Override
    public boolean delete(T item) {
        int key = item.hashCode();

        while (true) {
            SearchResult searchResult = search(key);
            InternalNode grandParent = searchResult.grandParent;
            InternalNode parent = searchResult.parent;
            Leaf leaf = searchResult.leaf;
            Update parentUpdate = searchResult.parentUpdate;
            Update grandParentUpdate = searchResult.grandParentUpdate;

            if (leaf.key != key)
                return false;

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
                if (helpDelete(operation))
                    return true;
            }else{
                help(grandParent.update.get());
            }


        }
    }

    private void help(Update u) {
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
    }

    private void helpMarked(DeleteInfo operation) {
        // Set other to point to the sibling of the node to which operation.leaf points
        Node other;
        // TODO: Check if these gets are safe. They should be.
        if (operation.parent.right.get() == operation.leaf)
            other = operation.parent.left.get();
        else
            other = operation.parent.right.get();

        // Splice the node to which operation.parent points out of the tree, replacing it by other
        compareAndSetChild(operation.grandParent, operation.parent, other);             // dchild CAS
        compareAndSetUpdate(operation.grandParent.update,
            new Update(State.DFLAG, operation), new Update(State.CLEAN, operation));    // dunflag CAS
    }

    private boolean helpDelete(DeleteInfo operation) {
        boolean CASSuccess;

        CASSuccess = operation.parent.update.compareAndSet(
            operation.parentUpdate,
            new Update(State.MARK, operation));

        if (CASSuccess) { // Check the paper. Missing some condition?
            helpMarked(operation);
            return true;
        } else {
            help(operation.parent.update.get());

            // backtrack CAS
            compareAndSetUpdate(operation.grandParent.update,
                new Update(State.DFLAG, operation), new Update(State.CLEAN, operation));
            return false;
        }
    }

    private SearchResult search(int key) {
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
        return new SearchResult(grandParent, parent, (Leaf) leaf, grandParentUpdate, parentUpdate);
    }

    private void helpInsert(InsertInfo operation) {
        compareAndSetChild(operation.parent, operation.leaf, operation.newInternal);

        Update update = operation.parent.update.get();
        if (update.state != State.IFLAG)
            // Someone else helped me
            return;

        // TODO: can we set the new Update.info to null?
        operation.parent.update.compareAndSet(update, new Update(State.CLEAN, operation));
    }

    private boolean compareAndSetChild(InternalNode parent, Node oldChild, Node newChild) {
        AtomicReference<Node> childUpdater = newChild.key < parent.key ? parent.left : parent.right;
        return childUpdater.compareAndSet(oldChild, newChild);
    }

    private boolean compareAndSetUpdate(AtomicReference<Update> reference, Update expectedUpdate, Update newUpdate){
        Update tmp = reference.get();
        if (expectedUpdate.state != tmp.state || expectedUpdate.info != tmp.info)
            return false;

        return reference.compareAndSet(tmp, newUpdate);

    }

    @Override
    public String toString() {
        return root.toString();
    }

//    public static void main(String[] args) {
//        NonBlockingBinarySearchTree<Integer> bst = new NonBlockingBinarySearchTree<>();
//        bst.find(new Integer(2));
//    }
}
