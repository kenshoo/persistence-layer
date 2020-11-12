package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.EntityFieldDbAdapter;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.ValueConverter;
import com.kenshoo.pl.entity.equalityfunctions.EntityValueEqualityFunction;

public class VirtualEntityFieldImpl<E extends EntityType<E>, T> extends EntityFieldImpl<E, T> {
    public VirtualEntityFieldImpl(EntityType<E> entityType, EntityFieldDbAdapter<T> dbAdapter, ValueConverter<T, String> stringValueConverter, EntityValueEqualityFunction<T> valueEqualityFunction) {
        super(entityType, dbAdapter, stringValueConverter, valueEqualityFunction);
    }

    @Override
    public boolean isVirtual() {
        return true;
    }
}
