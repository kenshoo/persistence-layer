package com.kenshoo.pl.entity.converters;

import com.kenshoo.pl.entity.ValueConverter;

/**
 * Converts from/to Enum using its string representation (Enum.name())
 */
public class EnumAsStringValueConverter<E extends Enum<E>> implements ValueConverter<E, String> {

    private final Class<E> cls;
    
    public EnumAsStringValueConverter(Class<E> cls) {
        this.cls = cls;
    }
    
    public static <E extends Enum<E>> ValueConverter<E, String> create(Class<E> cls) {
        return new EnumAsStringValueConverter<>(cls);
    }

    @Override
    public String convertTo(E value) {
        if (value == null) {
            return null;
        }
        return value.name();
    }

    @Override
    public E convertFrom(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(cls, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public Class<E> getValueClass() {
        return cls;
    }
}
