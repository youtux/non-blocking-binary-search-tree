package it.unitn.studenti.alessiobogon.concurrency.nbbst;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
class SearchResult{
    final InternalNode grandParent, parent;
    final Leaf leaf;
    final Update grandParentUpdate, parentUpdate;

    SearchResult(InternalNode grandParent, InternalNode parent, Leaf leaf,
                 Update grandParentUpdate,
                 Update parentUpdate) {
        this.grandParent = grandParent;
        this.parent = parent;
        this.leaf = leaf;
        this.grandParentUpdate = grandParentUpdate;
        this.parentUpdate = parentUpdate;
    }
}
