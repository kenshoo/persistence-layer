package com.kenshoo.pl.entity.converters;

import com.kenshoo.pl.entity.ValueConverter;

public class IdentityValueConverter<T> implements ValueConverter<T, T> {

    private final Class<T> valueClass;

    public static <T> ValueConverter<T, T> getInstance(Class<T> valueClass) {
        return new IdentityValueConverter<>(valueClass);
    }

    public IdentityValueConverter(Class<T> valueClass) {
        this.valueClass = valueClass;
    }

    @Override
    public T convertTo(T value) {
        return value;
    }

    @Override
    public T convertFrom(T value) {
        return value;
    }

    @Override
    public Class<T> getValueClass() {
        return valueClass;
    }
}
