package it.unitn.studenti.alessiobogon.concurrency.nbbst;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Alessio Bogon on 11/06/15.
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
