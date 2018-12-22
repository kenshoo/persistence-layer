package com.kenshoo.pl.entity.converters;


import com.kenshoo.pl.entity.ValueConverter;

public class ByteBooleanValueConverter implements ValueConverter<Boolean, Byte> {

    public static final ByteBooleanValueConverter INSTANCE = new ByteBooleanValueConverter();

    private ByteBooleanValueConverter() {
    }

    @Override
    public Byte convertTo(Boolean value) {
        return (byte) (value ? 1 : 0);
    }

    @Override
    public Boolean convertFrom(Byte value) {
        return value != null && value != 0;
    }

    @Override
    public Class<Boolean> getValueClass() {
        return Boolean.class;
    }

}
