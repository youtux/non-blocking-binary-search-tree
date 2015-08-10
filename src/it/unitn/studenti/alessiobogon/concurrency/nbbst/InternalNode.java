package it.unitn.studenti.alessiobogon.concurrency.nbbst;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Alessio Bogon on 11/06/15.
 */

/**
 * This class represents an internal node. It keeps <tt>AtomicReference</tt>s to the update field and the children.
 */
class InternalNode extends Node{
    final AtomicReference<Update> update;
    final AtomicReference<Node> left, right;

    InternalNode(int key, Node left, Node right, Update update) {
        super(key);
        this.left = new AtomicReference<>(left);
        this.right = new AtomicReference<>(right);
        this.update = new AtomicReference<>(update);
    }

    @Override
    public String toString() {
        return "Node(" + key + ", " + left.get().toString() + ", " + right.get().toString() + ")";
    }
}
