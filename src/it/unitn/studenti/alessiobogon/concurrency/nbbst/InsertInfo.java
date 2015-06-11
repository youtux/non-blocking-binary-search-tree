package it.unitn.studenti.alessiobogon.concurrency.nbbst;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
class InsertInfo extends Info {
    final InternalNode p;
    final Leaf l;
    final InternalNode newInternal;

    InsertInfo(InternalNode p, Leaf l, InternalNode newInternal) {
        this.p = p;
        this.l = l;
        this.newInternal = newInternal;
    }
}
