package com.kenshoo.pl.entity;

public class TripleUniqueKeyValue<E extends EntityType<E>, T1, T2, T3> extends UniqueKeyValue<E> {

    public TripleUniqueKeyValue(TripleUniqueKey<E, T1, T2, T3> uniqueKey, T1 v1, T2 v2, T3 v3) {
        super(uniqueKey, new Object[]{v1, v2, v3});
    }

    public TripleUniqueKeyValue(EntityField<E, T1> field1, EntityField<E, T2> field2, EntityField<E, T2> field3, T1 v1, T2 v2, T3 v3) {
        super(new TripleUniqueKey<>(field1, field2, field3), new Object[]{v1, v2, v3});
    }

}
