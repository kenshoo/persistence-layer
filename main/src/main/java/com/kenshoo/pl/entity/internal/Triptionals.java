package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.Triptional;

import java.util.Arrays;
import java.util.function.Supplier;

import static org.jooq.lambda.Seq.seq;

public final class Triptionals {

    @SafeVarargs
    public static <T> Triptional<T> firstPresent(final Supplier<Triptional<T>>... suppliers) {
        return seq(Arrays.stream(suppliers))
            .map(Supplier::get)
            .findFirst(Triptional::isPresent)
            .orElse(Triptional.absent());
    }

    private Triptionals() {
        // singleton
    }
}
