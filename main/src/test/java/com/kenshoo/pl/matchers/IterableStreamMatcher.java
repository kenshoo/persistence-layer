package com.kenshoo.pl.matchers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.mockito.ArgumentMatcher;

import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.ArgumentMatchers.argThat;

/**
 * A Mockito matcher for Streams, which collects the actual Stream using a given collector and then compares it to an expected Iterable.
 * Due to Stream semantics, the matcher guards against multiple calls by JUnit.
 */
public class IterableStreamMatcher<T, I extends Iterable<T>> implements ArgumentMatcher<Stream<T>> {

    private final I expectedIterable;
    private final Collector<T, ?, I> collector;
    private I actualIterable;

    public IterableStreamMatcher(final I expectedIterable,
                                 final Collector<T, ?, I> collector) {
        this.expectedIterable = requireNonNull(expectedIterable, "An expected iterable");
        this.collector = requireNonNull(collector, "A collector must be provided");;
    }

    @Override
    public synchronized boolean matches(Stream<T> stream) {
        // This is to protect against JUnit calling this more than once
        actualIterable = actualIterable == null ? stream.collect(collector) : actualIterable;
        return actualIterable.equals(expectedIterable);
    }

    public static <T> Stream<T> eqStreamAsList(final Iterable<T> expectedIterable) {
        return argThat(new IterableStreamMatcher<>(ImmutableList.copyOf(expectedIterable), toList()));
    }

    public static <T> Stream<T> eqStreamAsSet(final Iterable<T> expectedIterable) {
        return argThat(new IterableStreamMatcher<>(ImmutableSet.copyOf(expectedIterable), toSet()));
    }
}
