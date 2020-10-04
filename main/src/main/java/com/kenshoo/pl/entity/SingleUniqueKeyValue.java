package com.kenshoo.pl.entity;

public class SingleUniqueKeyValue<E extends EntityType<E>, T> extends UniqueKeyValue<E> {

    protected SingleUniqueKeyValue(SingleUniqueKey<E, T> uniqueKey, T val) {
        super(uniqueKey, new Object[]{val});
    }

    public T getId() {
        //noinspection unchecked
        return (T) values[0];
    }
}
