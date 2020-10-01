package com.kenshoo.pl.simulation.internal;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class ValueOrException<T> {

    private final T value;
    private final Exception exception;

    private ValueOrException(T value, Exception exception) {
        this.value = value;
        this.exception = exception;
    }

    public static <V> ValueOrException<V> of(V value) {
        return new ValueOrException<>(value, null);
    }

    public static <V> ValueOrException<V> error(Exception exception) {
        return new ValueOrException<>(null, exception);
    }

    public T value() throws Exception {
        if (exception != null) {
            throw exception;
        }
        return value;
    }

    public T orWhenException(Function<Exception, T> handler) {
        return exception == null ? value : handler.apply(exception);
    }

    public static <T> ValueOrException<T> tryGet(Callable<T> callable) {
        try {
            return ValueOrException.of(callable.call());
        } catch (Exception exception) {
            return ValueOrException.error(exception);
        }
    }

}