package it.unitn.studenti.alessiobogon.concurrency.nbbst;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
class DeleteInfo extends Info{
    final InternalNode parent;
    final Leaf leaf;
    final InternalNode grandParent;
    final Update parentUpdate;

    DeleteInfo(InternalNode grandParent, InternalNode parent, Leaf leaf, Update parentUpdate) {
        this.parent = parent;
        this.leaf = leaf;
        this.grandParent = grandParent;
        this.parentUpdate = parentUpdate;
    }
}
