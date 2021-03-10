package com.kenshoo.pl.entity;

import org.jooq.lambda.Seq;

import java.util.Collection;

public interface ChangeContext {

    boolean isEnabled(Feature feature);

    CurrentEntityState getEntity(EntityChange entityChange);

    default FinalEntityState getFinalEntity(EntityChange<? extends EntityType<?>> change) {
        return new FinalEntityState(getEntity(change), change);
    }

    void addEntity(EntityChange change, CurrentEntityState currentState);

    void addValidationError(EntityChange<? extends EntityType<?>> entityChange, ValidationError error);

    boolean hasValidationErrors();

    Seq<ValidationError> getValidationErrors(EntityChange cmd);

    boolean containsError(EntityChange entityChange);

    boolean containsErrorNonRecursive(EntityChange entityChange);

    boolean containsShowStopperErrorNonRecursive(EntityChange entityChange);

    PersistentLayerStats getStats();

    Collection<FieldFetchRequest> getFetchRequests();

    Hierarchy getHierarchy();
}
