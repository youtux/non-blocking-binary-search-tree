package it.unitn.studenti.alessiobogon.concurrency.nbbst;

/**
 * Created by Alessio Bogon on 11/06/15.
 */

/**
 * Abstract class representing a node.
 */
abstract class Node {
    final int key;

    Node(int key) {
        this.key = key;
    }

}
