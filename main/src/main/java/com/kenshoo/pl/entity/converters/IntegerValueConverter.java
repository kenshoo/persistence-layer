package com.kenshoo.pl.entity.converters;

import com.kenshoo.pl.entity.ValueConverter;
import org.jooq.types.UInteger;

public class IntegerValueConverter implements ValueConverter<Integer, UInteger> {

    public final static IntegerValueConverter INSTANCE = new IntegerValueConverter();

    @Override
    public UInteger convertTo(Integer value) {
        return UInteger.valueOf(value);
    }

    @Override
    public Integer convertFrom(UInteger value) {
        return value.intValue();
    }

    @Override
    public Class<Integer> getValueClass() {
        return Integer.class;
    }
}
