package com.kenshoo.pl.entity;

public class SextupleUniqueKeyValue<E extends EntityType<E>, T1, T2, T3, T4, T5, T6> extends UniqueKeyValue<E> {

    protected SextupleUniqueKeyValue(SextupleUniqueKey<E, T1, T2, T3, T4, T5, T6> uniqueKey,
                                     T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6) {
        super(uniqueKey, new Object[]{v1, v2, v3, v4, v5, v6});
    }
}
