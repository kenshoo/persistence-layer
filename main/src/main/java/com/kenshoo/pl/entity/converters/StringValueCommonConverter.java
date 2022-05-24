package com.kenshoo.pl.entity.converters;

import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.ValueConverter;
import org.apache.commons.lang3.BooleanUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class StringValueCommonConverter<T> implements ValueConverter<String, T> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final Map<Class<?>, Function<?, String>> TO_STRING_FUNCTION_BY_CLASS = ImmutableMap.<Class<?>, Function<?, String>>builder()
            .put(Timestamp.class, o -> Long.toString(((Timestamp) o).getTime()))
            .put(Instant.class, i -> Long.toString(((Instant) i).toEpochMilli()))
            .put(LocalDate.class, (Function<LocalDate, String>) DATE_FORMATTER::format)
            .build();

    private static final Map<Class<?>, Function<String, ?>> FROM_STRING_FUNCTION_BY_CLASS = ImmutableMap.<Class<?>, Function<String, ?>>builder()
            .put(String.class, Function.<String>identity())
            .put(Integer.class, Integer::parseInt)
            .put(Long.class, Long::parseLong)
            .put(Double.class, Double::parseDouble)
            .put(Float.class, Float::parseFloat)
            .put(Byte.class, Byte::parseByte)
            .put(Boolean.class, s -> BooleanUtils.toBoolean(s, Boolean.TRUE.toString(), Boolean.FALSE.toString()))
            .put(BigInteger.class, BigInteger::new)
            .put(BigDecimal.class, BigDecimal::new)
            .put(Timestamp.class, s -> new Timestamp(Long.parseLong(s)))
            .put(Instant.class, s -> Instant.ofEpochMilli(Long.parseLong(s)))
            .put(LocalDate.class, s -> DATE_FORMATTER.parse(s, LocalDate::from))
            .build();

    private Class<T> valueClass;

    public StringValueCommonConverter(Class<T> valueClass) {
        if (!isSupported(valueClass)) {
            throw new IllegalArgumentException("StringValueConverter does not support class '" + valueClass.getSimpleName() + "', please implement a dedicated converter.");
        }
        this.valueClass = valueClass;
    }

    public static <T> boolean isSupported(Class<T> valueClass) {
        return FROM_STRING_FUNCTION_BY_CLASS.containsKey(valueClass);
    }


    @Override
    public T convertTo(String value) {
        //noinspection unchecked
        return (T) FROM_STRING_FUNCTION_BY_CLASS.get(valueClass).apply(value);

    }

    @Override
    public String convertFrom(T value) {
        //noinspection unchecked
        return ((Function<T, String>) TO_STRING_FUNCTION_BY_CLASS.getOrDefault(valueClass, Objects::toString)).apply(value);

    }

    @Override
    public Class<String> getValueClass() {
        return String.class;
    }
}
