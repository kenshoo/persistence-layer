package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Set;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class PLPostFetchCondition implements PLBaseCondition<PLPostFetchCondition> {

    private final Predicate<Entity> condition;
    private final Set<? extends EntityField<?, ?>> fields;

    public PLPostFetchCondition(final Predicate<Entity> condition, final EntityField<?, ?>... fields) {
        this(condition, ImmutableSet.copyOf(fields));
    }

    public PLPostFetchCondition(final Predicate<Entity> condition, final Set<? extends EntityField<?, ?>> fields) {
        this.condition = requireNonNull(condition, "a post fetch condition must be provided");
        this.fields = requireNonNull(fields, "Fields must not be null (can be empty)");
    }

    public boolean test(final Entity entity) {
        return condition.test(entity);
    }

    public Set<? extends EntityField<?, ?>> getFields() {
        return fields;
    }

    public PLPostFetchCondition and(final PLPostFetchCondition other) {
        requireNonNull(other, "a post-fetch condition must be provided");
        return new PLPostFetchCondition(condition.and(other.condition),
                               Sets.union(this.fields, other.fields));
    }

    public PLPostFetchCondition or(final PLPostFetchCondition other) {
        requireNonNull(other, "a post-fetch condition must be provided");
        return new PLPostFetchCondition(condition.or(other.condition),
                               Sets.union(this.fields, other.fields));
    }

    public static PLPostFetchCondition not(final PLPostFetchCondition condition) {
        requireNonNull(condition, "a post-fetch condition must be provided");
        return new PLPostFetchCondition(condition.condition.negate(),
                               condition.fields);
    }

    public static PLPostFetchCondition trueCondition() {
        return new PLPostFetchCondition(entity -> true);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("postFetchCondition", condition)
                .append("fields", fields)
                .toString();
    }
}
