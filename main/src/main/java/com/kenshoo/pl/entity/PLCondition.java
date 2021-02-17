package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jooq.Condition;
import org.jooq.impl.DSL;

import java.util.Set;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class PLCondition {

    private final Condition jooqCondition;
    private final Predicate<Entity> postFetchCondition;
    private final Set<? extends EntityField<?, ?>> fields;

    public PLCondition(final Condition jooqCondition, final Predicate<Entity> postFetchCondition, final EntityField<?, ?>... fields) {
        this(jooqCondition, postFetchCondition, ImmutableSet.copyOf(fields));
    }

    public PLCondition(final Condition jooqCondition, final Predicate<Entity> postFetchCondition, final Set<? extends EntityField<?, ?>> fields) {
        this.jooqCondition = requireNonNull(jooqCondition, "a Jooq condition must be provided");
        this.postFetchCondition = requireNonNull(postFetchCondition, "a post fetch condition must be provided");
        this.fields = requireNonNull(fields, "Fields must not be null (can be empty)");
    }

    public Condition getJooqCondition() {
        return jooqCondition;
    }

    public Predicate<Entity> getPostFetchCondition() {
        return postFetchCondition;
    }

    public Set<? extends EntityField<?, ?>> getFields() {
        return fields;
    }

    public PLCondition and(final PLCondition other) {
        requireNonNull(other, "a condition must be provided");
        return new PLCondition(jooqCondition.and(other.jooqCondition),
                               postFetchCondition.and(other.postFetchCondition),
                               Sets.union(this.fields, other.fields));
    }

    public PLCondition or(final PLCondition other) {
        requireNonNull(other, "a condition must be provided");
        return new PLCondition(jooqCondition.or(other.jooqCondition),
                               postFetchCondition.or(other.postFetchCondition),
                               Sets.union(this.fields, other.fields));
    }

    public static PLCondition not(final PLCondition condition) {
        requireNonNull(condition, "a condition must be provided");
        return new PLCondition(condition.jooqCondition.not(),
                               condition.postFetchCondition.negate(),
                               condition.fields);
    }

    public static PLCondition trueCondition() {
        return new PLCondition(DSL.trueCondition(), entity -> true);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("jooqCondition", jooqCondition)
                .append("postFetchCondition", postFetchCondition)
                .append("fields", fields)
                .toString();
    }
}
