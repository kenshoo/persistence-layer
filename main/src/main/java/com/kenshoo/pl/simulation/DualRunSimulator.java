package com.kenshoo.pl.simulation;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.simulation.internal.*;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Seq;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;


/**
 * This utility helps to migrate from an old mutation code to Persistence Layer by running them side-by-side and
 * compare their results.
 *
 * <b>Terminology</b>
 * + ActualMutator - This is the old code.
 * + Simulated - The persistence layer commands.
 *
 * <b>Implementation</b>
 * This simulator runs both the old mutator along with the simulated commands (PL). However, the simulated commands
 * run without DbOutputGenerator, meaning, they don't really affect the database. PL simulated commands only run
 * enrichers, validators, etc.
 * After the real (old) mutator changed the DB, we fetch the final state of the entities to compare the actual
 * modification with the content of the simulated commands.
 *
 * <b>Limitations</b>
 * + One-to-many relations: Child commands are not supported.
 * + ID must be provided to all commands, including CREATE commands, to enable matching them with the actual DB mutation.
 *
 * @param <E> EntityType
 * @param <ID> ID type
 */
public class DualRunSimulator<E extends EntityType<E>, ID extends Identifier<E>> {

    private final Collection<EntityField<E, ?>> inspectedFields;
    private final ChangeFlowConfig.Builder<E> flowToSimulate;
    private final PersistenceLayer<E> pl;
    private final ActualResultFetcher<E, ID> actualResultFetcher;
    private final ResultComparator<E, ID> resultComparator;

    public DualRunSimulator(PLContext plContext, ChangeFlowConfig.Builder<E> flowToSimulate, Collection<EntityField<E, ?>> inspectedFields) {
        this.inspectedFields = inspectedFields;
        this.flowToSimulate = flowToSimulate;
        this.actualResultFetcher = new ActualResultFetcher<>(new EntitiesFetcher(plContext.dslContext()), inspectedFields);
        this.pl = new PersistenceLayer<>(plContext);
        this.resultComparator = new ResultComparator<>(inspectedFields);
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

        final var actualResults = actualResultFetcher.fetch(idsOf(simulatedResults), actualErrors, this::emptyOriginalState);

        return resultComparator.findMismatches(simulatedResults, actualResults);
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

        final var actualResults = actualResultFetcher.fetch(idsOf(simulatedResults), actualErrors, originalStateRecorder::get);

        return resultComparator.findMismatches(simulatedResults, actualResults);
    }

    private void populateIdsInCreateCommands(Collection<? extends Pair<ID, ? extends CreateEntityCommand<E>>> commands) {
        commands.forEach(cmd -> populateIdToCommandValues(cmd.getKey(), cmd.getValue()));
    }

    private List<ID> idsOf(Collection<SimulatedResult<E, ID>> simulatedResults) {
        return seq(simulatedResults).map(SimulatedResult::getId).toList();
    }

    private <K, V> List<? extends V> values(Collection<? extends Pair<K, ? extends V>> pairs) {
        return seq(pairs).map(Pair::getValue).toList();
    }

    private void populateIdToCommandValues(ID id, CreateEntityCommand<E> command) {
        Seq.of(id.getUniqueKey().getFields()).forEach(field -> populateFieldValueToCommand(field, id, command));
    }

    private <T> void populateFieldValueToCommand(EntityField<E, T> field, ID id, CreateEntityCommand<E> command) {
        command.set(field, id.get(field));
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
