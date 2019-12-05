package com.kenshoo.pl.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.jooq.lambda.Seq;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.jooq.lambda.Seq.seq;

public class ChangeContext {

    private final Multimap<EntityChange, ValidationError> validationErrors = HashMultimap.create();
    private final Map<EntityChange, Entity> entities = new IdentityHashMap<>();
    private final PersistentLayerStats stats = new PersistentLayerStats();
    private final Set<FieldFetchRequest> fieldsToFetchRequests = Sets.newHashSet();
    private final Hierarchy hierarchy;
    private final FeatureSet features;

    @VisibleForTesting
    public ChangeContext() {
        hierarchy = null;
        features = FeatureSet.EMPTY;
    }

    public ChangeContext(Hierarchy hierarchy, FeatureSet features) {
        this.hierarchy = hierarchy;
        this.features = features;
    }

    public boolean isEnabled(Feature feature) {
        return features.isEnabled(feature);
    }

    public Entity getEntity(EntityChange entityChange) {
        return entities.get(entityChange);
    }

    public void addEntity(EntityChange change, Entity entity) {
        this.entities.put(change, entity);
    }

    public void addValidationError(EntityChange<? extends EntityType<?>> entityChange, ValidationError error) {
        validationErrors.put(entityChange, error);
    }

    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }

    public Seq<ValidationError> getValidationErrors(EntityChange cmd) {
        final Seq<ValidationError> parentErrors = seq(validationErrors.get(cmd));
        if (cmd.getChildren().findAny().isPresent()) {
            Stream<ChangeEntityCommand> children = cmd.getChildren();
            return parentErrors.concat(children.flatMap(this::getValidationErrors));
        } else {
            return parentErrors;
        }
    }

    public boolean containsError(EntityChange entityChange) {
        return getValidationErrors(entityChange).isNotEmpty();
    }

    public boolean containsErrorNonRecursive(EntityChange entityChange) {
        return validationErrors.containsKey(entityChange);
    }

    public PersistentLayerStats getStats() {
        return stats;
    }

    void addFetchRequests(Collection<FieldFetchRequest> fetchRequests) {
        this.fieldsToFetchRequests.addAll(fetchRequests);
    }

    public Collection<FieldFetchRequest> getFetchRequests() {
        return fieldsToFetchRequests;
    }

    public Hierarchy getHierarchy() {
        return hierarchy;
    }

}
