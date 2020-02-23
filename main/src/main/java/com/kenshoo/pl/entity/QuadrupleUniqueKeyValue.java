package com.kenshoo.pl.entity;

public class QuadrupleUniqueKeyValue<E extends EntityType<E>, T1, T2, T3, T4> extends UniqueKeyValue<E> {

    public QuadrupleUniqueKeyValue(QuadrupleUniqueKey<E, T1, T2, T3, T4> uniqueKey, T1 v1, T2 v2, T3 v3, T4 v4) {
        super(uniqueKey, new Object[]{v1, v2, v3, v4});
    }

    public QuadrupleUniqueKeyValue(EntityField<E, T1> field1, EntityField<E, T2> field2, EntityField<E, T2> field3, EntityField<E, T2> field4, T1 v1, T2 v2, T3 v3, T4 v4) {
        super(new QuadrupleUniqueKey<>(field1, field2, field3, field4), new Object[]{v1, v2, v3, v4});
    }

}
