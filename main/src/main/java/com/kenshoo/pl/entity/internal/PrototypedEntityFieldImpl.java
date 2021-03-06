package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.EntityFieldDbAdapter;
import com.kenshoo.pl.entity.EntityFieldPrototype;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.PrototypedEntityField;
import com.kenshoo.pl.entity.ValueConverter;
import com.kenshoo.pl.entity.equalityfunctions.EntityValueEqualityFunction;

public class PrototypedEntityFieldImpl<E extends EntityType<E>, T> extends EntityFieldImpl<E, T> implements PrototypedEntityField<E, T> {

    private final EntityFieldPrototype<T> entityFieldPrototype;

    public PrototypedEntityFieldImpl(EntityType<E> entityType, EntityFieldPrototype<T> entityFieldPrototype, EntityFieldDbAdapter<T> dbAdapter,
                                     ValueConverter<T, String> stringValueConverter, EntityValueEqualityFunction<T> valueEqualityFunction) {
        super(entityType, dbAdapter, stringValueConverter, valueEqualityFunction);
        this.entityFieldPrototype = entityFieldPrototype;
    }

    @Override
    public EntityFieldPrototype<T> getEntityFieldPrototype() {
        return entityFieldPrototype;
    }
}
