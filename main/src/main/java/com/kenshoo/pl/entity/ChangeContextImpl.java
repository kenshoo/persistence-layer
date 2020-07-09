package com.kenshoo.pl.entity;

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

public class ChangeContextImpl implements ChangeContext {

    private final Multimap<EntityChange, ValidationError> validationErrors = HashMultimap.create();
    private final Map<EntityChange, CurrentEntityState> entities = new IdentityHashMap<>();
    private final PersistentLayerStats stats = new PersistentLayerStats();
    private final Set<FieldFetchRequest> fieldsToFetchRequests = Sets.newHashSet();
    private final Hierarchy hierarchy;
    private final FeatureSet features;

    public ChangeContextImpl(Hierarchy hierarchy, FeatureSet features) {
        this.hierarchy = hierarchy;
        this.features = features;
    }

    @Override
    public boolean isEnabled(Feature feature) {
        return features.isEnabled(feature);
    }

    @Override
    public CurrentEntityState getEntity(EntityChange entityChange) {
        return entities.get(entityChange);
    }

    @Override
    public void addEntity(EntityChange change, CurrentEntityState currentState) {
        this.entities.put(change, currentState);
    }

    @Override
    public void addValidationError(EntityChange<? extends EntityType<?>> entityChange, ValidationError error) {
        validationErrors.put(entityChange, error);
    }

    @Override
    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }

    @Override
    public Seq<ValidationError> getValidationErrors(EntityChange cmd) {
        final Seq<ValidationError> parentErrors = seq(validationErrors.get(cmd));
        if (cmd.getChildren().findAny().isPresent()) {
            Stream<ChangeEntityCommand> children = cmd.getChildren();
            return parentErrors.concat(children.flatMap(this::getValidationErrors));
        } else {
            return parentErrors;
        }
    }

    @Override
    public boolean containsError(EntityChange entityChange) {
        return getValidationErrors(entityChange).isNotEmpty();
    }

    @Override
    public boolean containsErrorNonRecursive(EntityChange entityChange) {
        return validationErrors.containsKey(entityChange);
    }

    @Override
    public PersistentLayerStats getStats() {
        return stats;
    }

    void addFetchRequests(Collection<FieldFetchRequest> fetchRequests) {
        this.fieldsToFetchRequests.addAll(fetchRequests);
    }

    @Override
    public Collection<FieldFetchRequest> getFetchRequests() {
        return fieldsToFetchRequests;
    }

    @Override
    public Hierarchy getHierarchy() {
        return hierarchy;
    }
}
