package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.spi.FieldValueSupplier;

import java.util.stream.Stream;

public class FixedFieldValueSupplier<T> implements FieldValueSupplier<T> {

    private final T value;

    public FixedFieldValueSupplier(T value) {
        this.value = value;
    }

    @Override
    public T supply(Entity currentState) {
        return value;
    }

    @Override
    public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
        return Stream.empty();
    }
}
