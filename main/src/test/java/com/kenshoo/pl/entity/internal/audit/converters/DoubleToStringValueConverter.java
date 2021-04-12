package com.kenshoo.pl.entity.internal.audit.converters;

import com.kenshoo.pl.entity.ValueConverter;

public class DoubleToStringValueConverter implements ValueConverter<Double, String> {

    @Override
    public String convertTo(Double value) {
        return value == null ? null : String.format("%.2f", value);
    }

    @Override
    public Double convertFrom(final String value) {
        return value == null ? null : Double.parseDouble(value);
    }

    @Override
    public Class<Double> getValueClass() {
        return Double.class;
    }
}
