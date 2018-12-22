package com.kenshoo.pl.entity;

/**
 *
 */
public class PairUniqueKeyValue<E extends EntityType<E>, T1, T2> extends UniqueKeyValue<E> {

    public PairUniqueKeyValue(PairUniqueKey<E, T1, T2> uniqueKey, T1 v1, T2 v2) {
        super(uniqueKey, new Object[]{v1, v2});
    }
}
