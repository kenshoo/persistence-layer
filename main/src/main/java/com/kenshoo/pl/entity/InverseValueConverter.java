package com.kenshoo.pl.entity;

/**
 * Inverse any ValueConverter.
 * If you have String to Enum converter, use this implementation to have opposite one - Enum to String.
 */
public class InverseValueConverter<T, T2> implements ValueConverter<T, T2>{

    private final ValueConverter<T2, T> converter;
    private final Class<T> targetClass;

    public InverseValueConverter(ValueConverter<T2, T> converter, Class<T> targetClass) {
        this.converter = converter;
        this.targetClass = targetClass;
    }

    @Override
    public T2 convertTo(T value) {
        return converter.convertFrom(value);
    }

    @Override
    public T convertFrom(T2 value) {
        return converter.convertTo(value);
    }

    @Override
    public Class<T> getValueClass() {
        return targetClass;
    }
}
