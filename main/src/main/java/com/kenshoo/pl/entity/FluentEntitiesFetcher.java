package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.EntitiesFetcher;

import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;

class FluentEntitiesFetcher implements FetchFromStep, FetchWhereStep, FetchFinalStep {

    private final EntitiesFetcher entitiesFetcher;
    private final EntityField<?, ?>[] fieldsToFetch;
    private EntityType<?> entityType;
    private PLCondition condition = PLCondition.trueCondition();


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

    public List<CurrentEntityState> fetch() {
        return entitiesFetcher.fetch(entityType, condition, fieldsToFetch);
    }

    public List<CurrentEntityState> fetchByKeys(Collection<? extends Identifier<?>> keys) {
        return entitiesFetcher.fetch(entityType, keys, condition, fieldsToFetch);
    }
}
