package com.kenshoo.pl.entity;

public class PairUniqueKeyValue<E extends EntityType<E>, T1, T2> extends UniqueKeyValue<E> {

    public PairUniqueKeyValue(PairUniqueKey<E, T1, T2> uniqueKey, T1 v1, T2 v2) {
        super(uniqueKey, new Object[]{v1, v2});
    }

    public PairUniqueKeyValue(EntityField<E, T1> field1, EntityField<E, T2> field2, T1 v1, T2 v2) {
        super(new PairUniqueKey<>(field1, field2), new Object[]{v1, v2});
    }
}
