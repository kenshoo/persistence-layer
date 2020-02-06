package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.jooq.Condition;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class PLCondition {

    private final Condition jooqCondition;
    private final Set<? extends EntityField<?, ?>> affectedFields;

    public PLCondition(final Condition jooqCondition, final EntityField<?, ?>... affectedFields) {
        this(jooqCondition, ImmutableSet.copyOf(affectedFields));
    }

    public PLCondition(final Condition jooqCondition, final Set<? extends EntityField<?, ?>> affectedFields) {
        this.jooqCondition = requireNonNull(jooqCondition, "a Jooq condition must be provided");
        this.affectedFields = requireNonNull(affectedFields, "Affected fields must not be null (can be empty)");
    }

    public Condition getJooqCondition() {
        return jooqCondition;
    }

    public Set<? extends EntityField<?, ?>> getAffectedFields() {
        return affectedFields;
    }

    public PLCondition and(final PLCondition other) {
        requireNonNull(other, "a condition must be provided");
        return new PLCondition(jooqCondition.and(other.jooqCondition),
                               Sets.union(this.affectedFields, other.affectedFields));
    }

    public PLCondition or(final PLCondition other) {
        requireNonNull(other, "a condition must be provided");
        return new PLCondition(jooqCondition.or(other.jooqCondition),
                               Sets.union(this.affectedFields, other.affectedFields));
    }

    public static PLCondition not(final PLCondition condition) {
        requireNonNull(condition, "a condition must be provided");
        return new PLCondition(condition.jooqCondition.not(),
                               condition.affectedFields);
    }
}
