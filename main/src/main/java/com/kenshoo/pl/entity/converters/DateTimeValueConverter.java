package com.kenshoo.pl.entity.converters;

import com.kenshoo.pl.entity.ValueConverter;

import java.sql.Date;
import java.time.Instant;

/**
 * Created by peterk on 8/1/2017
 */
public class DateTimeValueConverter implements ValueConverter<Instant, Date> {

    public static final DateTimeValueConverter INSTANCE = new DateTimeValueConverter();

    @Override
    public Date convertTo(Instant value) {
        return new Date(value.toEpochMilli());
    }

    @Override
    public Instant convertFrom(Date value) {
        return  value != null ? value.toInstant() : null;
    }

    @Override
    public Class<Instant> getValueClass() {
        return Instant.class;
    }
}
