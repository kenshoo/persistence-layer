package com.kenshoo.pl.entity;

import org.jooq.lambda.Seq;

import java.util.Collection;

public interface ChangeContext {

    boolean isEnabled(Feature feature);

    Entity getEntity(EntityChange entityChange);

    void addEntity(EntityChange change, Entity entity);

    void addValidationError(EntityChange<? extends EntityType<?>> entityChange, ValidationError error);

    boolean hasValidationErrors();

    Seq<ValidationError> getValidationErrors(EntityChange cmd);

    boolean containsError(EntityChange entityChange);

    boolean containsErrorNonRecursive(EntityChange entityChange);

    PersistentLayerStats getStats();

    Collection<FieldFetchRequest> getFetchRequests();

    Hierarchy getHierarchy();

}
