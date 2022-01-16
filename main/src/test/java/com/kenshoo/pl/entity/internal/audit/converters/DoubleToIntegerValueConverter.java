package com.kenshoo.pl.entity.internal.audit.converters;

import com.kenshoo.pl.entity.ValueConverter;

public class DoubleToIntegerValueConverter implements ValueConverter<Double, Integer> {

    @Override
    public Integer convertTo(Double value) {
        return value == null ? null : value.intValue();
    }

    @Override
    public Double convertFrom(final Integer value) {
        return value == null ? null : value.doubleValue();
    }

    @Override
    public Class<Double> getValueClass() {
        return Double.class;
    }
}
