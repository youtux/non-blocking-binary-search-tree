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

    @Override
    public String toString() {
        return String.format("SearchResult[grandParentKey=%s, parentKey=%s, leafKey=%s, grandParentUpdate=%s, parentUpdate=%s)",
                grandParent != null ? grandParent.key : "null",
                parent != null ? parent.key : "null",
                leaf != null ? leaf.key : "null",
                grandParentUpdate != null ? grandParentUpdate : "null",
                parentUpdate != null ? parentUpdate : "null"
        );
    }
}
