package it.unitn.studenti.alessiobogon.concurrency.nbbst;

import com.sun.tools.corba.se.idl.constExpr.Not;
import it.unitn.studenti.alessiobogon.concurrency.Set;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

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
        return result.l.key == key;
    }

    @Override
    public boolean insert(T item){
        int key = item.hashCode();

        InternalNode p, newInternal;
        Leaf l, newSibling, new_ = new Leaf(item);
        Update pupdate, result;
        InsertInfo op;

        while (true){
            SearchResult sresult = search(key);
            p = sresult.p;
            l = sresult.l;
            pupdate = sresult.pupdate;

            if (l.key == key){
                // Cannot insert duplicate key
                return false;
            }
            if (pupdate.state != State.CLEAN){
                // Help the other operation
                help(pupdate);
            }else{
                newSibling = new Leaf(l.item);
                Leaf left, right;

                if (new_.key < newSibling.key){
                    left = new_;
                    right = newSibling;
                }else{
                    left = newSibling;
                    right = new_;
                }
                newInternal = new InternalNode(
                    Math.max(key, l.key),
                    left, right,
                    new Update(State.CLEAN, null)
                );
                op = new InsertInfo(p, l, newInternal);

                boolean CASSuccess = p.update.compareAndSet(pupdate, new Update(State.IFLAG, op));
                if (CASSuccess){
                    helpInsert(op);
                    return true;
                }else{
                    // TODO: the iflag CAS failed. Help the operation that cause failure
                    // TODO: check my correctness
                    System.out.print("Insert CAS failed");
                    help(p.update.get());
                }
            }

        }
    }


    @Override
    public boolean delete(T item) {
        int key = item.hashCode();

        InternalNode grandParent, parent;
        Leaf leaf;
        Update parentUpdate, grandParentUpdate;
        DeleteInfo op;

        while (true) {
            SearchResult sresult = search(key);
            grandParent = sresult.gp;
            parent = sresult.p;
            leaf = sresult.l;
            parentUpdate = sresult.pupdate;
            grandParentUpdate = sresult.gpupdate;

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

            op = new DeleteInfo(grandParent, parent, leaf, parentUpdate);
            boolean CASSuccess = grandParent.update.compareAndSet(grandParentUpdate, new Update(State.DFLAG, op));

            if (CASSuccess){
                if (helpDelete(op))
                    return true;
            }else{
                help(grandParent.update.get());
            }


        }
    }

    private void help(Update u) {
        if (u.state == State.IFLAG) {
            helpInsert((InsertInfo) u.info);
            return;
        }

        if (u.state == State.MARK) {
            helpMarked((DeleteInfo) u.info);
            return;
        }

        if (u.state == State.DFLAG) {
            helpDelete((DeleteInfo) u.info);
            return;
        }
    }

    private void helpMarked(DeleteInfo op) {
        throw new NotImplementedException();
    }

    private boolean helpDelete(DeleteInfo op) {
        throw new NotImplementedException();
//        Update result;
//
//        result = op.p.update.compareAndSet(op.pupdate.get(), new Update(State.MARK, op));
//        if (r)
    }

    private SearchResult search(int key) {
        InternalNode gp = null,
            p = null;
        Node l = root;
        Update gpupdate = null,
            pupdate = null;

        while (l instanceof InternalNode){
            gp = p;
            p = (InternalNode) l;
            gpupdate = pupdate;
            // TODO: remove cast. Is the compiler drunk?
            pupdate = (Update) p.update.get();
            if (key < l.key){
                l = (Node) p.left.get();
            }else{
                l = (Node) p.right.get();
            }
        }
        return new SearchResult(gp, p, (Leaf) l, gpupdate, pupdate);
    }

    private void helpInsert(InsertInfo op) {
        compareAndSetChild(op.p, op.l, op.newInternal);

        Update update = op.p.update.get();
        if (update.state != State.IFLAG)
            // Someone else helped me
            return;

        // TODO: can we set the new Update to null?
        op.p.update.compareAndSet(update, new Update(State.CLEAN, op));
    }

    private boolean compareAndSetChild(InternalNode parent, Node old, Node new_) {
        AtomicReference<Node> child = new_.key < parent.key ? parent.left : parent.right;
        return child.compareAndSet(old, new_);
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
