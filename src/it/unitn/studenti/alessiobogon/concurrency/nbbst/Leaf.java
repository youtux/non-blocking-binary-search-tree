package it.unitn.studenti.alessiobogon.concurrency.nbbst;

/**
 * Created by Alessio Bogon on 11/06/15.
 */

/**
 * This class represents an leaf of the tree. It keeps a reference to the item it represents.
 * @param <T> the type of the element to represent
 */
class Leaf<T> extends Node {
    final T item;

    Leaf(T item) {
        super(item.hashCode());
        this.item = item;
    }

    @Override
    public String toString() {
        return "Leaf(" + key + ")";
    }
}
