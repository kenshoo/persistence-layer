package com.kenshoo.pl.entity.converters;

import com.kenshoo.pl.entity.ValueConverter;

import java.sql.Timestamp;
import java.time.Instant;

public class TimestampValueConverter implements ValueConverter<Instant, Timestamp> {

    public static final TimestampValueConverter INSTANCE = new TimestampValueConverter();

    private TimestampValueConverter() {
    }

    @Override
    public Timestamp convertTo(Instant value) {
        return value == null
            ? null
            : new Timestamp(value.toEpochMilli());
    }

    @Override
    public Instant convertFrom(Timestamp value) {
        return value != null ? value.toInstant() : null;
    }

    @Override
    public Class<Instant> getValueClass() {
        return Instant.class;
    }
}
