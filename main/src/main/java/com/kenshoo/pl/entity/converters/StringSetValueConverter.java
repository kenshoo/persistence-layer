package com.kenshoo.pl.entity.converters;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.ValueConverter;

import java.util.Set;

public class StringSetValueConverter implements ValueConverter<Set<String>, String> {

    public final static StringSetValueConverter INSTANCE = new StringSetValueConverter();

    @Override
    public String convertTo(Set<String> value) {
        return String.join(",", value);
    }

    @Override
    public Set<String> convertFrom(String value) {
        return ImmutableSet.copyOf(value.split(","));
    }

    @Override
    public Class<Set<String>> getValueClass() {
        //noinspection unchecked
        return (Class) Set.class;
    }

}
