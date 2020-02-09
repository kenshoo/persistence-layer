package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.jooq.Condition;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class PLCondition {

    private final Condition jooqCondition;
    private final Set<? extends EntityField<?, ?>> fields;

    public PLCondition(final Condition jooqCondition, final EntityField<?, ?>... fields) {
        this(jooqCondition, ImmutableSet.copyOf(fields));
    }

    public PLCondition(final Condition jooqCondition, final Set<? extends EntityField<?, ?>> fields) {
        this.jooqCondition = requireNonNull(jooqCondition, "a Jooq condition must be provided");
        this.fields = requireNonNull(fields, "Fields must not be null (can be empty)");
    }

    public Condition getJooqCondition() {
        return jooqCondition;
    }

    public Set<? extends EntityField<?, ?>> getFields() {
        return fields;
    }

    public PLCondition and(final PLCondition other) {
        requireNonNull(other, "a condition must be provided");
        return new PLCondition(jooqCondition.and(other.jooqCondition),
                               Sets.union(this.fields, other.fields));
    }

    public PLCondition or(final PLCondition other) {
        requireNonNull(other, "a condition must be provided");
        return new PLCondition(jooqCondition.or(other.jooqCondition),
                               Sets.union(this.fields, other.fields));
    }

    public static PLCondition not(final PLCondition condition) {
        requireNonNull(condition, "a condition must be provided");
        return new PLCondition(condition.jooqCondition.not(),
                               condition.fields);
    }
}
