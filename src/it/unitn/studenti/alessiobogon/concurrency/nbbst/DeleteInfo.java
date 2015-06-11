package it.unitn.studenti.alessiobogon.concurrency.nbbst;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
class DeleteInfo extends Info{
    final InternalNode p;
    final Leaf l;
    final InternalNode gp;
    final AtomicReference<Update> pupdate;

    DeleteInfo(InternalNode gp, InternalNode p, Leaf l, Update pupdate) {
        this.p = p;
        this.l = l;
        this.gp = gp;
        this.pupdate = new AtomicReference<Update>(pupdate);
    }
}
