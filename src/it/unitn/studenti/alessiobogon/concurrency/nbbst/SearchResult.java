package it.unitn.studenti.alessiobogon.concurrency.nbbst;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
class SearchResult{
    final InternalNode gp, p;
    final Leaf l;
    final Update gpupdate, pupdate;

    SearchResult(InternalNode gp, InternalNode p, Leaf l,
                 Update gpupdate,
                 Update pupdate) {
        this.gp = gp;
        this.p = p;
        this.l = l;
        this.gpupdate = gpupdate;
        this.pupdate = pupdate;
    }
}
