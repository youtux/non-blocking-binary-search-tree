package it.unitn.studenti.alessiobogon.concurrency;

/**
 * Created by youtux on 11/06/15.
 */
public interface Set<T> {
    public boolean find(T item);
    public boolean insert(T item);
    public boolean delete(T item);
}
