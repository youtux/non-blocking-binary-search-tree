package it.unitn.studenti.alessiobogon.concurrency;

/**
 * Created by Alessio Bogon on 11/06/15.
 */

/**
 * A collection that contains no duplicate elements.
 * @param <T> the type of elements maintained by this set
 */
public interface Set<T> {
    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     *
     * @param item element whose presence is to be tested
     * @return <tt>true</tt> if this set contains the specified element
     */
    boolean find(T item);

    /**
     * Adds the specified element to this set, if it is not already present.
     *
     * @param item element to be inserted
     * @return <tt>true</tt> if the set did not contain the element
     */
    boolean insert(T item);

    /**
     * Removes the specified element to this set, if it exists.
     *
     * @param item element to be deleted
     * @return <tt>true</tt> if the set did contain the element
     */
    boolean delete(T item);
}
