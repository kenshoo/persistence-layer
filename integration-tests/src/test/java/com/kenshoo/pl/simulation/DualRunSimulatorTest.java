package com.kenshoo.pl.simulation;

import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.auto.inc.*;
import com.kenshoo.pl.auto.inc.TestEntity;
import com.kenshoo.pl.auto.inc.TestEntityTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.lambda.Seq;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.kenshoo.pl.auto.inc.TestEntity.ID;
import static com.kenshoo.pl.auto.inc.TestEntity.NAME;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;


public class DualRunSimulatorTest {

    private static final TestEntityTable PRIMARY_TABLE = TestEntityTable.INSTANCE;
    private static final TestSecondaryEntityTable SECONDARY_TABLE = TestSecondaryEntityTable.INSTANCE;

    private final DSLContext dslContext = TestJooqConfig.create();
    private final PLContext plContext = new PLContext.Builder(dslContext).build();
    private final ChangeFlowConfig.Builder<TestEntity> flowConfig = ChangeFlowConfigBuilderFactory
            .newInstance(plContext, TestEntity.INSTANCE);

    @Before
    public void setUp() {
        Stream.of(PRIMARY_TABLE, SECONDARY_TABLE)
                .forEach(table -> DataTableUtils.createTable(dslContext, table));
    }

    @After
    public void tearDown() {
        Stream.of(SECONDARY_TABLE, PRIMARY_TABLE)
                .forEach(table -> dslContext.deleteFrom(table).execute());
    }

    @Test
    public void testCreationReturnErrorForMismatchedFieldAndSuccessWhenMatched() {

        var simulator = new DualRunSimulator<TestEntity, TestEntity.Key>(plContext, flowConfig, List.of(
                ID,
                NAME
        ));

        var commandsToSimulate = List.of(
            Pair.of(id(1), new TestEntityCreateCommand().with(ID, 1).with(NAME, "one")),
            Pair.of(id(2), new TestEntityCreateCommand().with(ID, 2).with(NAME, "two"))
        );

        var realDatabaseChange = withoutFailures(() -> {
            dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.id, 1).set(PRIMARY_TABLE.name, "one").execute();
            dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.id, 2).set(PRIMARY_TABLE.name, "two_la_la_la").execute();
        });

        var results = simulator.runCreation(realDatabaseChange, commandsToSimulate);

        assertThat("Simulation results: " + results, failedIds(results), containsInAnyOrder(2));
    }

    @Test
    public void testCreationReturnErrorForEntityThatWasNotReallyCreatedByTheRealMutator() {

        var simulator = new DualRunSimulator<TestEntity, TestEntity.Key>(plContext, flowConfig, List.of(
                ID,
                NAME
        ));

        var commandsToSimulate = List.of(
                Pair.of(id(1), new TestEntityCreateCommand().with(ID, 1).with(NAME, "one"))
        );

        var realDatabaseChange = withoutFailures(() -> {}); // do nothing

        var results = simulator.runCreation(realDatabaseChange, commandsToSimulate);

        assertThat("Simulation results: " + results, failedIds(results), containsInAnyOrder(1));
    }

    @Test
    public void testCreationReturnErrorWhenSimulationFailedButRealMutationSucceeded() {

        flowConfig.withValidator(failAllCommands());

        var simulator = new DualRunSimulator<TestEntity, TestEntity.Key>(plContext, flowConfig, List.of(
                ID,
                NAME
        ));

        var commandsToSimulate = List.of(
                Pair.of(id(1), new TestEntityCreateCommand().with(ID, 1).with(NAME, "one"))
        );

        var realDatabaseChange = withoutFailures(() -> {
            dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.id, 1).set(PRIMARY_TABLE.name, "one").execute();
        });

        var results = simulator.runCreation(realDatabaseChange, commandsToSimulate);

        assertThat("Simulation results: " + results, failedIds(results), containsInAnyOrder(1));
    }

    @Test
    public void testCreationReturnErrorWhenSimulationSucceededButRealMutationFailed() {

        var simulator = new DualRunSimulator<TestEntity, TestEntity.Key>(plContext, flowConfig, List.of(
                ID,
                NAME
        ));

        var commandsToSimulate = List.of(
                Pair.of(id(1), new TestEntityCreateCommand().with(ID, 1).with(NAME, "one"))
        );

        var realDatabaseChange = failAllByIds(1);

        var results = simulator.runCreation(realDatabaseChange, commandsToSimulate);

        assertThat("Simulation results: " + results, failedIds(results), containsInAnyOrder(1));
    }

    @Test
    public void testIdArgumentShallOverrideIdInCreationCommand() {

        var simulator = new DualRunSimulator<TestEntity, TestEntity.Key>(plContext, flowConfig, List.of(
                ID,
                NAME
        ));

        var commandsToSimulate = List.of(
                Pair.of(id(1), new TestEntityCreateCommand().with(ID, 432123).with(NAME, "one")),
                Pair.of(id(2), new TestEntityCreateCommand().with(NAME, "two"))
        );

        var realDatabaseChange = withoutFailures(() -> {
            dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.id, 1).set(PRIMARY_TABLE.name, "one").execute();
            dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.id, 2).set(PRIMARY_TABLE.name, "two").execute();
        });

        var results = simulator.runCreation(realDatabaseChange, commandsToSimulate);

        assertThat(results, empty());
    }

    @Test
    public void testUpdateReturnErrorForMismatchedFieldAndSuccessWhenMatched() {

        // DB Setup

        dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.id, 1).set(PRIMARY_TABLE.name, "one").execute();
        dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.id, 2).set(PRIMARY_TABLE.name, "two").execute();

        // Simulation Setup

        var simulator = new DualRunSimulator<TestEntity, TestEntity.Key>(plContext, flowConfig, List.of(
                NAME
        ));

        var commandsToSimulate = List.of(
                new TestEntityUpdateCommand(1).with(NAME, "one A"),
                new TestEntityUpdateCommand(2).with(NAME, "two B")
        );

        var realDatabaseChange = withoutFailures(() -> {
            dslContext.update(PRIMARY_TABLE).set(PRIMARY_TABLE.name, "one A").where(PRIMARY_TABLE.id.eq(1)).execute();
            dslContext.update(PRIMARY_TABLE).set(PRIMARY_TABLE.name, "one B la la la").where(PRIMARY_TABLE.id.eq(2)).execute();
        });

        // Action

        var results = simulator.runUpdate(realDatabaseChange, commandsToSimulate);

        assertThat("Simulation results: " + results, failedIds(results), containsInAnyOrder(2));
    }

    @Test
    public void testUpdateReturnErrorWhenFieldIsNotInCommandButValueIsChangedInDB() {

        // DB Setup

        dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.id, 1).set(PRIMARY_TABLE.name, "one").execute();

        // Simulation Setup

        var simulator = new DualRunSimulator<TestEntity, TestEntity.Key>(plContext, flowConfig, List.of(
                NAME
        ));

        var commandsToSimulate = List.of(
                new TestEntityUpdateCommand(1)
        );

        var realDatabaseChange = withoutFailures(() -> {
            dslContext.update(PRIMARY_TABLE).set(PRIMARY_TABLE.name, "one was modified").where(PRIMARY_TABLE.id.eq(1)).execute();
        });

        // Action

        var results = simulator.runUpdate(realDatabaseChange, commandsToSimulate);

        assertThat("Simulation results: " + results, failedIds(results), containsInAnyOrder(1));
    }

    // -------------------------------------------------------------------------------------------- //
    //
    //       helper methods
    //
    // -------------------------------------------------------------------------------------------- //

    private ChangesValidator<TestEntity> failAllCommands() {
        return (cmds, op, ctx) -> cmds.forEach(cmd -> ctx.addValidationError(cmd, new ValidationError("isError")));
    }

    private List<Integer> failedIds(Collection<ComparisonMismatch<TestEntity, TestEntity.Key>> errors) {
        return errors.stream().map(e -> e.getId().get(ID)).collect(toList());
    }

    private TestEntity.Key id(int id) {
        return new TestEntity.Key(id);
    }

    private ActualDatabaseMutator<TestEntity, TestEntity.Key> withoutFailures(Runnable operation) {
        return () -> {
            operation.run();
            return emptyList();
        };
    }

    private ActualDatabaseMutator<TestEntity, TestEntity.Key> failAllByIds(Integer... ids) {
        return () -> Seq.of(ids).map(id -> new ActualMutatorError<>(id(id), "please fail")).toList();
    }


}