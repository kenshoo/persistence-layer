package com.kenshoo.pl.simulation;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.simulation.internal.*;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Seq;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;


public class DualRunSimulator<E extends EntityType<E>, ID extends Identifier<E>> {

    private final Collection<EntityField<E, ?>> inspectedFields;
    private final ChangeFlowConfig.Builder<E> flowToSimulate;
    private final EntitiesFetcher fetcher;
    private final PersistenceLayer<E> pl;

    public DualRunSimulator(PLContext plContext, ChangeFlowConfig.Builder<E> flowToSimulate, Collection<EntityField<E, ?>> inspectedFields) {
        this.inspectedFields = inspectedFields;
        this.flowToSimulate = flowToSimulate;
        this.fetcher = new EntitiesFetcher(plContext.dslContext());
        this.pl = new PersistenceLayer<>(plContext);
    }

    /**
     * Note that auto-increment is not supported for simulation. IDs must be provided in order to compare simulated
     * commands with actual mutations.
     *
     * @param databaseMutator
     * @param commandsToSimulate
     * @return
     */
    public List<ComparisonMismatch<E, ID>> runCreation(
            ActualDatabaseMutator<E, ID> databaseMutator,
            Collection<? extends Pair<ID, ? extends CreateEntityCommand<E>>> commandsToSimulate) {

        final var plFlow = flowToSimulate.withoutOutputGenerators().build();

        populateIdsInCreateCommands(commandsToSimulate);

        final var simulatedResults = seq(pl.create(values(commandsToSimulate), plFlow).getChangeResults())
                .zip(commandsToSimulate)
                .map(r -> new SimulatedResult<>(r.v1.getCommand(), r.v2.getKey(), r.v1.getErrors()))
                .collect(toList());

        final var actualErrors = seq(databaseMutator.run()).toMap(__ -> __.getId());

        final var actualResults = fetchActualResults(idsOf(simulatedResults), actualErrors, this::emptyOriginalState);

        return findMismatches(simulatedResults, actualResults);
    }

    public List<ComparisonMismatch<E, ID>> runUpdate(
            ActualDatabaseMutator<E, ID> databaseMutator,
            Collection<? extends UpdateEntityCommand<E, ID>> commandsToSimulate) {

        final var originalStateRecorder = new InitialStateRecorder<>(inspectedFields);

        final var plFlow = flowToSimulate
                .withoutOutputGenerators()
                .withOutputGenerator(originalStateRecorder)
                .build();

        final var simulatedResults = seq(pl.update(commandsToSimulate, plFlow).getChangeResults())
                .map(r -> new SimulatedResult<>(r.getCommand(), r.getIdentifier(), r.getErrors()))
                .collect(toList());

        final var actualErrors = seq(databaseMutator.run()).toMap(__ -> __.getId());

        final var actualResults = fetchActualResults(idsOf(simulatedResults), actualErrors, originalStateRecorder::get);

        return findMismatches(simulatedResults, actualResults);
    }

    private Iterable<ActualResult> fetchActualResults(
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

    private List<ComparisonMismatch<E, ID>> findMismatches(
            Iterable<SimulatedResult<E, ID>> simulatedResults,
            Iterable<ActualResult> actualDbResults) {

        return seq(simulatedResults).zip(actualDbResults)
                .map(pair -> findMismatch(pair.v1, pair.v2))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private Optional<ComparisonMismatch<E, ID>> findMismatch(SimulatedResult<E, ID> simulatedResult, ActualResult actualResult) {

        if (simulatedResult.isError() && actualResult.isError()) {
            return Optional.empty();
        }

        if (simulatedResult.isSuccess() && actualResult.isError()) {
            return Optional.of(new ComparisonMismatch<>(simulatedResult.getId(), "Simulated mutation was successful but real mutation finished with the following error: " + actualResult.getErrorDescription()));
        }

        if (simulatedResult.isError() && actualResult.isSuccess()) {
            return Optional.of(new ComparisonMismatch<>(simulatedResult.getId(), "Real mutation was successful but simulated mutation finished with the following errors: " + simulatedResult.getErrors()));
        }

        final var mismatchingFields = inspectedFields.stream()
                .map(field -> getFieldMismatch(field, simulatedResult.getCommand(), actualResult))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        return mismatchingFields.isEmpty()
                ? Optional.empty()
                : Optional.of(new ComparisonMismatch<>(simulatedResult.getId(), "Found field mismatch: " + mismatchingFields));
    }

    private Optional<String> getFieldMismatch(EntityField<E, ?> field, EntityChange<E> simulated, ActualResult actualResult) {

        if (!simulated.isFieldChanged(field) && !actualResult.isReallyChanged(field)) {
            return Optional.empty();
        }

        if (!simulated.isFieldChanged(field) && actualResult.isReallyChanged(field)) {
            return Optional.of("Field \"" + field + "\" is not populated in the simulated command although it was changed in DB");
        }

        return Objects.equals(simulated.get(field), actualResult.getFinalValue(field))
                ? Optional.empty()
                : Optional.of("Field \"" + field + "\" has mismatch values. Simulated: \"" + simulated.get(field) + "\"" + ", Actual: \"" + actualResult.getFinalValue(field) + "\"");
    }

    private void populateIdsInCreateCommands(Collection<? extends Pair<ID, ? extends CreateEntityCommand<E>>> commands) {
        commands.forEach(cmd -> populateIdToCommandValues(cmd.getKey(), cmd.getValue()));
    }

    private void populateIdToCommandValues(ID id, CreateEntityCommand<E> command) {
        Seq.of(id.getUniqueKey().getFields()).forEach(field -> populateFieldValueToCommand(field, id, command));
    }

    private <T> void populateFieldValueToCommand(EntityField<E, T> field, ID id, CreateEntityCommand<E> command) {
        command.set(field, id.get(field));
    }

    private List<ID> idsOf(Collection<SimulatedResult<E, ID>> simulatedResults) {
        return seq(simulatedResults).map(SimulatedResult::getId).toList();
    }

    private <K, V> List<? extends V> values(Collection<? extends Pair<K, ? extends V>> pairs) {
        return seq(pairs).map(Pair::getValue).toList();
    }

    private Entity emptyState() {
        return new Entity() {
            @Override
            public boolean containsField(EntityField<?, ?> field) {
                return false;
            }

            @Override
            public <T> T get(EntityField<?, T> field) {
                return null;
            }
        };
    }

    private Entity emptyOriginalState(ID id) {
        return emptyState();
    }
}
