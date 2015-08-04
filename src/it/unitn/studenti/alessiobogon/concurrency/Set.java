package it.unitn.studenti.alessiobogon.concurrency;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
public interface Set<T> {
    boolean find(T item);
    boolean insert(T item);
    boolean delete(T item);
}
