package com.kenshoo.pl.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kenshoo.pl.entity.Triptional.State.*;
import static java.util.Objects.requireNonNull;

public class Triptional<T> {

    enum State {
        FILLED,
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

    private Triptional(final T value) {
        this(value,
             value == null ? NULL : FILLED);
    }

    private Triptional(final T value, final State state) {
        this.value = value;
        this.state = state;
    }

    public static <T> Triptional<T> of(final T value) {
        return new Triptional<>(value);
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

    public void ifFilled(final Consumer<? super T> consumer) {
        if (value != null)
            consumer.accept(value);
    }

    public <U> Triptional<U> map(final Function<? super T, ? extends U> mapper) {
        return map(mapper, () -> null);
    }

    public <U> Triptional<U> map(final Function<? super T, ? extends U> filledMapper,
                                 final Supplier<? extends U> nullReplacer) {
        requireNonNull(filledMapper, "filledMapper is required");
        requireNonNull(nullReplacer, "nullReplacer is required");

        switch (state) {
            case FILLED:
                return of(filledMapper.apply(value));
            case NULL:
                return of(nullReplacer.get());
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

    public <U> Optional<U> mapToOptional(final Function<? super T, ? extends U> filledMapper,
                                         final Supplier<? extends U> nullReplacer) {
        return map(filledMapper, nullReplacer).asOptional();
    }

    public boolean isPresent() {
        return !isAbsent();
    }

    public boolean isAbsent() {
        return state == ABSENT;
    }

    public boolean isFilled() {
        return state == FILLED;
    }

    public boolean isNotFilled() {
        return !isFilled();
    }

    public boolean isNull() {
        return state == NULL;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Triptional)) {
            return false;
        }

        final Triptional<?> other = (Triptional<?>) obj;
        return new EqualsBuilder()
            .append(state, other.state)
            .append(value, other.value)
            .isEquals();
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
            case FILLED:
                return String.format("Triptional[%s]", value);
            case NULL:
                return "Triptional.null";
            default:
                return "Triptional.absent";
        }
    }
}
