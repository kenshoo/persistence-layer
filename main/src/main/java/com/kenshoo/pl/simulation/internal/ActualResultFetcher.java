package com.kenshoo.pl.simulation.internal;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.simulation.ActualMutatorError;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static org.jooq.lambda.Seq.seq;

public class ActualResultFetcher<E extends EntityType<E>, ID extends Identifier<E>> {

    private final EntitiesFetcher fetcher;
    private final Collection<EntityField<E, ?>> inspectedFields;

    public ActualResultFetcher(EntitiesFetcher fetcher, Collection<EntityField<E, ?>> inspectedFields) {
        this.fetcher = fetcher;
        this.inspectedFields = inspectedFields;
    }

    public Iterable<ActualResult> fetch(
            Collection<ID> allIds,
            Map<ID, ActualMutatorError<E, ID>> errors,
            Function<ID, Entity> originalStates) {

        final var finalStates = fetcher.fetchEntitiesByIds(allIds, inspectedFields);

        return seq(allIds).map(id -> {

            final var originalState = originalStates.apply(id);
            final var finalState = finalStates.get(id);

            if (errors.containsKey(id)) {
                return new ActualError(errors.get(id).getDescription());
            }

            if (originalState == null) {
                return new ActualError("Could not find original state");
            }

            if (finalState == null) {
                return new ActualError("Could not find final state");
            }

            return new ActualSuccess(originalState, finalState);
        });
    }
}
