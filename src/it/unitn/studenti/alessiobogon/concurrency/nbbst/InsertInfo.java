package it.unitn.studenti.alessiobogon.concurrency.nbbst;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
class InsertInfo extends Info {
    final InternalNode parent;
    final Leaf leaf;
    final InternalNode newInternal;

    InsertInfo(InternalNode parent, Leaf leaf, InternalNode newInternal) {
        this.parent = parent;
        this.leaf = leaf;
        this.newInternal = newInternal;
    }
}
