package com.kenshoo.pl.entity;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kenshoo.pl.entity.Triptional.State.*;
import static java.util.Objects.requireNonNull;

public class Triptional<T> {

    enum State {
        NOT_NULL,
        NULL,
        ABSENT
    }

    private static final Triptional<?> NULL_INSTANCE = new Triptional<>(NULL);
    private static final Triptional<?> ABSENT_INSTANCE = new Triptional<>(ABSENT);

    private final T value;
    private final State state;

    private Triptional(final State state) {
        this(null, state);
    }

    private Triptional(final T value, final State state) {
        this.value = value;
        this.state = state;
    }

    public static <T> Triptional<T> of(final T value) {
        return value == null ? nullInstance() : new Triptional<>(value, NOT_NULL);
    }

    @SuppressWarnings("unchecked")
    public static <T> Triptional<T> nullInstance() {
        return (Triptional<T>) NULL_INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Triptional<T> absent() {
        return (Triptional<T>) ABSENT_INSTANCE;
    }

    public T get() {
        if (isAbsent()) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public void ifNotNull(final Consumer<? super T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    public <U> Triptional<U> map(final Function<? super T, ? extends U> mapper) {
        return map(mapper, () -> null);
    }

    public <U> Triptional<U> map(final Function<? super T, ? extends U> notNullMapper,
                                 final Supplier<? extends U> nullReplacer) {
        requireNonNull(notNullMapper, "notNullMapper is required");
        requireNonNull(nullReplacer, "nullReplacer is required");

        switch (state) {
            case NOT_NULL:
                return of(notNullMapper.apply(value));
            case NULL:
                return of(nullReplacer.get());
            default:
                return absent();
        }
    }

    public <U> Triptional<U> flatMap(final Function<? super T, Triptional<U>> mapper) {
        return flatMap(mapper, Triptional::nullInstance);
    }

    public <U> Triptional<U> flatMap(final Function<? super T, Triptional<U>> notNullMapper,
                                     final Supplier<Triptional<U>> nullReplacer) {
        requireNonNull(notNullMapper, "notNullMapper is required");
        requireNonNull(nullReplacer, "nullReplacer is required");

        switch (state) {
            case NOT_NULL:
                return requireNonNull(notNullMapper.apply(value));
            case NULL:
                return requireNonNull(nullReplacer.get());
            default:
                return absent();
        }
    }

    public Optional<T> asOptional() {
        return Optional.ofNullable(value);
    }

    public <U> Optional<U> mapToOptional(final Function<? super T, ? extends U> mapper) {
        return mapToOptional(mapper, () -> null);
    }

    public <U> Optional<U> mapToOptional(final Function<? super T, ? extends U> notNullMapper,
                                         final Supplier<? extends U> nullReplacer) {
        return map(notNullMapper, nullReplacer).asOptional();
    }

    public boolean isPresent() {
        return !isAbsent();
    }

    public boolean isAbsent() {
        return state == ABSENT;
    }

    public boolean isNotNull() {
        return state == NOT_NULL;
    }

    public boolean isNull() {
        return state == NULL;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(final Object obj) {
        return equals(obj, Objects::equals);
    }

    public boolean equals(final Object obj, final BiFunction<T, T, Boolean> valueEqualityFunction) {
        requireNonNull(valueEqualityFunction, "A value equality function must be provided");

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Triptional)) {
            return false;
        }

        //noinspection unchecked
        final Triptional<T> other = (Triptional<T>) obj;

        if (!(isPresent() && other.isPresent())) {
            return false;
        }

        return valueEqualityFunction.apply(value, other.value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(state)
            .append(value)
            .toHashCode();
    }

    @Override
    public String toString() {
        switch (state) {
            case NOT_NULL:
                return String.format("Triptional[%s]", value);
            case NULL:
                return "Triptional.null";
            default:
                return "Triptional.absent";
        }
    }
}
