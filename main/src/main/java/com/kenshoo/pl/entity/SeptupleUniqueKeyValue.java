package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.UniqueKeyValue;

public class SeptupleUniqueKeyValue<E extends EntityType<E>, T1, T2, T3, T4, T5, T6, T7> extends UniqueKeyValue<E> {

    protected SeptupleUniqueKeyValue(SeptupleUniqueKey<E, T1, T2, T3, T4, T5, T6, T7> uniqueKey, T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7) {
        super(uniqueKey, new Object[]{v1, v2, v3, v4, v5, v6, v7});
    }

    protected SeptupleUniqueKeyValue(SeptupleUniqueKey<E, T1, T2, T3, T4, T5, T6, T7> uniqueKey, Object[] values) {
        super(uniqueKey, values);
    }
}

