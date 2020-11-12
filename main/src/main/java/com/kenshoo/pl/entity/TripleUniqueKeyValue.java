package com.kenshoo.pl.entity;

public class TripleUniqueKeyValue<E extends EntityType<E>, T1, T2, T3> extends UniqueKeyValue<E> {

    protected TripleUniqueKeyValue(TripleUniqueKey<E, T1, T2, T3> uniqueKey, T1 v1, T2 v2, T3 v3) {
        super(uniqueKey, new Object[]{v1, v2, v3});
    }
}
