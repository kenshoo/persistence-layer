package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.UniqueKey;

import java.util.Collection;

public class ForeignUniqueKey<E extends EntityType<E>> extends UniqueKey<E> {
    public ForeignUniqueKey(Collection<EntityField<E, ?>> foreignKeys) {
        //noinspection unchecked
        super(foreignKeys.toArray(new EntityField[foreignKeys.size()]));
    }
}
