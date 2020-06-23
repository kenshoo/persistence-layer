package com.kenshoo.pl.entity;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.kenshoo.pl.entity.Triptional.State.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

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
    public static <T> Triptional<T> nul() {
        return (Triptional<T>) NULL_INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Triptional<T> absent() {
        return (Triptional<T>)ABSENT_INSTANCE;
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
        return mapToOptional(identity());
    }

    public <U> Optional<U> mapToOptional(final Function<? super T, ? extends U> mapper) {
        return mapToOptional(mapper, () -> null);
    }

    public <U> Optional<U> mapToOptional(final Function<? super T, ? extends U> filledMapper,
                                         final Supplier<? extends U> nullReplacer) {
        switch (state) {
            case FILLED:
                return Optional.ofNullable(filledMapper.apply(value));
            case NULL:
                return Optional.ofNullable(nullReplacer.get());
            default:
                return Optional.empty();
        }
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
}
