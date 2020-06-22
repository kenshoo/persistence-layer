package com.kenshoo.pl.entity;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kenshoo.pl.entity.TriState.State.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

public class TriState<T> {

    enum State {
        FILLED,
        EMPTY,
        ABSENT
    }

    private static final TriState<?> EMPTY_INSTANCE = new TriState<>(EMPTY);
    private static final TriState<?> ABSENT_INSTANCE = new TriState<>(ABSENT);

    private final T value;
    private final State state;

    private TriState(final State state) {
        this(null, state);
    }

    private TriState(final T value) {
        this(value,
             value == null ? EMPTY : FILLED);
    }

    private TriState(final T value, final State state) {
        this.value = value;
        this.state = state;
    }

    public static <T> TriState<T> of(final T value) {
        return new TriState<>(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> TriState<T> empty() {
        return (TriState<T>)EMPTY_INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <T> TriState<T> absent() {
        return (TriState<T>)ABSENT_INSTANCE;
    }

    public <U> TriState<U> map(final Function<? super T, ? extends U> mapper) {
        return map(mapper, () -> null);
    }

    public <U> TriState<U> map(final Function<? super T, ? extends U> filledMapper,
                               final Supplier<? extends U> emptySupplier) {
        requireNonNull(filledMapper, "filledMapper is required");
        requireNonNull(emptySupplier, "emptySupplier is required");

        switch (state) {
            case FILLED:
                return of(filledMapper.apply(value));
            case EMPTY:
                return of(emptySupplier.get());
            default:
                return absent();
        }
    }

    public Optional<T> asOptional() {
        return mapToOptional(identity());
    }

    public <U> Optional<U> mapToOptional(final Function<? super T, ? extends U> mapper) {
        return mapToOptional(mapper, () -> null);
    }

    public <U> Optional<U> mapToOptional(final Function<? super T, ? extends U> filledMapper,
                                         final Supplier<? extends U> emptySupplier) {
        switch (state) {
            case FILLED:
                return Optional.ofNullable(filledMapper.apply(value));
            case EMPTY:
                return Optional.ofNullable(emptySupplier.get());
            default:
                return Optional.empty();
        }
    }

    public boolean isFilled() {
        return state == FILLED;
    }

    public boolean isNotFilled() {
        return !isFilled();
    }

    public boolean isEmpty() {
        return state == EMPTY;
    }

    public boolean isAbsent() {
        return state == ABSENT;
    }
}
