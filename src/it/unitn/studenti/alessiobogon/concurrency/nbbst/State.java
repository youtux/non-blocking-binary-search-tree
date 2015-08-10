package it.unitn.studenti.alessiobogon.concurrency.nbbst;

/**
 * Created by Alessio Bogon on 11/06/15.
 */

/**
 * This <tt>enum</tt> represents the four possible states of an internal node:
 * - CLEAN: the node has no ongoing operations
 * - IFLAG: the node is going through an insert operation. One of its children will be replaced by a new
 *          <tt>InternalNode</tt>
 * - DFLAG: the node is going through a delete operation. One of its children will be <tt>MARK</tt>ed (logical removal)
 *          and swapped with another one (physical removal)
 * - MARK: the node has been marked.
 */
enum State {
    CLEAN, MARK, DFLAG, IFLAG;
}
