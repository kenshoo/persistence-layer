package com.kenshoo.pl.entity;

public interface ValueConverter<T, T2> {

    T2 convertTo(T value);

    T convertFrom(T2 value);

    Class<T> getValueClass();
}
