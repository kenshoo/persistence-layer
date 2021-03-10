package com.kenshoo.pl.entity.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CollectionView<E> implements Collection<E> {

    private final Collection<E> delegate;
    private final Predicate<E> predicate;

    public CollectionView(Collection<E> delegate, Predicate<E> predicate) {
        this.delegate = delegate;
        this.predicate = predicate;
    }

    @Override
    public int size() {
        return (int) filteredStream().count();
    }

    @Override
    public boolean isEmpty() {
        return filteredStream().findAny().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return filteredStream().anyMatch(e -> e.equals(o));
    }

    @Override
    public Iterator<E> iterator() {
        return filteredStream().iterator();
    }

    @Override
    public Object[] toArray() {
        return filteredStream().toArray(Object[]::new);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return (T[])toArray();
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("add");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if(c.isEmpty()) return true;

        if(isEmpty()) return false;

        return filteredStream().allMatch(c::contains);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("addAll");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("removeAll");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear");
    }

    private Stream<E> filteredStream() {
        return delegate.stream().filter(predicate);
    }

}
