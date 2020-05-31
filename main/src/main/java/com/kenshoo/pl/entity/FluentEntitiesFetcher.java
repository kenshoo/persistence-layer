package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.EntitiesFetcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

class FluentEntitiesFetcher implements FetchFromStep, FetchWhereStep, FetchFinalStep {

    private final EntitiesFetcher entitiesFetcher;
    private final EntityField<?, ?>[] fieldsToFetch;
    private EntityType<?> entityType;
    private PLCondition condition = PLCondition.TrueCondition;


    FluentEntitiesFetcher(final EntitiesFetcher entitiesFetcher,
                          final EntityField<?, ?>... fieldsToFetch) {
        this.entitiesFetcher = requireNonNull(entitiesFetcher, "entitiesFetcher is required");
        this.fieldsToFetch = fieldsToFetch;
    }

    public FetchWhereStep from(final EntityType<?> entityType) {
        this.entityType = entityType;
        return this;
    }

    public FetchFinalStep where(final PLCondition condition) {
        this.condition = condition;
        return this;
    }

    public List<Entity> fetch() {
        return entitiesFetcher.fetch(entityType, condition, fieldsToFetch);
    }

    public List<Entity> fetch(Collection<? extends Identifier<?>> keys) {
        return entitiesFetcher.fetch(entityType, keys, condition, fieldsToFetch);
    }
}
