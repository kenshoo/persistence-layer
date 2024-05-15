package com.kenshoo.pl.entity;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public interface PLBaseCondition<C extends PLBaseCondition<C>> {

    Set<? extends EntityField<?, ?>> getFields();

    C and(final C other);

    C or(final C other);
}
