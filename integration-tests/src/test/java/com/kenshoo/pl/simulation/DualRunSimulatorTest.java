package com.kenshoo.pl.simulation;

import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.auto.inc.*;
import com.kenshoo.pl.auto.inc.TestEntity;
import com.kenshoo.pl.auto.inc.TestEntityTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import com.kenshoo.pl.simulation.internal.ActualResultFetcher;
import com.kenshoo.pl.simulation.internal.ResultComparator;
import org.jooq.DSLContext;
import org.jooq.lambda.Seq;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.kenshoo.pl.auto.inc.TestEntity.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;


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

    private final UniqueKey<TestEntity> NAME_AS_UNIQUE_KEY = new SingleUniqueKey<>(NAME);

    @Test
    public void testCreationReturnErrorForMismatchedFieldAndSuccessWhenMatched() {

        var simulator = new DualRunSimulator<>(plContext, flowConfig, List.of(
                FIELD1
        ));

        var commandsToSimulate = List.of(
            new TestEntityCreateCommand().with(NAME, "1").with(FIELD1, "one"),
            new TestEntityCreateCommand().with(NAME, "2").with(FIELD1, "two")
        );

        var realDatabaseChange = withoutFailures(() -> {
            dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.name, "1").set(PRIMARY_TABLE.field1, "one").execute();
            dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.name, "2").set(PRIMARY_TABLE.field1, "two_la_la_la").execute();
        });

        var results = simulator.runCreation(NAME_AS_UNIQUE_KEY, realDatabaseChange, commandsToSimulate);

        assertThat("Simulation results: " + results, failedNames(results), containsInAnyOrder("2"));
    }

    @Test
    public void testCreationReturnErrorForEntityThatWasNotReallyCreatedByTheRealMutator() {

        var simulator = new DualRunSimulator<>(plContext, flowConfig, List.of(
                FIELD1
        ));

        var commandsToSimulate = List.of(
                new TestEntityCreateCommand().with(NAME, "1").with(FIELD1, "one")
        );

        var realDatabaseChange = withoutFailures(() -> {}); // do nothing

        var results = simulator.runCreation(NAME_AS_UNIQUE_KEY, realDatabaseChange, commandsToSimulate);

        assertThat("Simulation results: " + results, failedNames(results), containsInAnyOrder("1"));
    }

    @Test
    public void testCreationReturnErrorWhenSimulationFailedButRealMutationSucceeded() {

        flowConfig.withValidator(failAllCommands());

        var simulator = new DualRunSimulator<>(plContext, flowConfig, List.of(
                FIELD1
        ));

        var commandsToSimulate = List.of(
                new TestEntityCreateCommand().with(NAME, "1").with(FIELD1, "one")
        );

        var realDatabaseChange = withoutFailures(() -> {
            dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.name, "1").set(PRIMARY_TABLE.field1, "one").execute();
        });

        var results = simulator.runCreation(NAME_AS_UNIQUE_KEY, realDatabaseChange, commandsToSimulate);

        assertThat("Simulation results: " + results, failedNames(results), containsInAnyOrder("1"));
    }

    @Test
    public void testCreationReturnErrorWhenSimulationSucceededButRealMutationFailed() {

        var simulator = new DualRunSimulator<>(plContext, flowConfig, List.of(
                FIELD1
        ));

        var commandsToSimulate = List.of(
                new TestEntityCreateCommand().with(NAME, "1").with(FIELD1, "one")
        );

        var realDatabaseChange = failAllByNames("1");

        var results = simulator.runCreation(NAME_AS_UNIQUE_KEY, realDatabaseChange, commandsToSimulate);

        assertThat("Simulation results: " + results, failedNames(results), containsInAnyOrder("1"));
    }

    @Test
    public void testUpdateReturnErrorForMismatchedFieldAndSuccessWhenMatched() {

        // DB Setup

        dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.id, 1).set(PRIMARY_TABLE.name, "one").execute();
        dslContext.insertInto(PRIMARY_TABLE).set(PRIMARY_TABLE.id, 2).set(PRIMARY_TABLE.name, "two").execute();

        // Simulation Setup

        var simulator = new DualRunSimulator<>(plContext, flowConfig, List.of(
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

        var simulator = new DualRunSimulator<>(plContext, flowConfig, List.of(
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

    @Test
    public void whenPersistenceLayerThrowsExceptionThenRealMutationShouldStillExecuteAndSingleComparisionMismatchShallBeReturned() {

        flowConfig.withValidator(throwingOnValidate(new RuntimeException("Crashing ha ha")));

        var simulator = new DualRunSimulator<>(plContext, flowConfig, List.of(FIELD1));

        var commandsToSimulate = List.of(
                new TestEntityCreateCommand().with(NAME, "1").with(FIELD1, "one"),
                new TestEntityCreateCommand().with(NAME, "2").with(FIELD1, "two")
        );

        ActualDatabaseMutator<TestEntity> realDatabaseChange = mock(ActualDatabaseMutator.class);

        var results = simulator.runCreation(NAME_AS_UNIQUE_KEY, realDatabaseChange, commandsToSimulate);

        assertThat(results, hasSize(1));
        assertThat(results.get(0).getDescription(), containsString("Crashing ha ha"));
        verify(realDatabaseChange).run();
    }

    @Test
    public void whenComparatorThrowsExceptionThenSingleComparisionMismatchShallBeReturned() {

        var simulator = new DualRunSimulator<>(
                plContext,
                flowConfig,
                List.of(FIELD1),
                emptyFetcher(),
                comparatorThrowing(new RuntimeException("Let's crash"))
        );

        var commandsToSimulate = List.of(
                new TestEntityCreateCommand().with(NAME, "1").with(FIELD1, "one"),
                new TestEntityCreateCommand().with(NAME, "2").with(FIELD1, "two")
        );

        ActualDatabaseMutator<TestEntity> realDatabaseChange = mock(ActualDatabaseMutator.class);

        var results = simulator.runCreation(NAME_AS_UNIQUE_KEY, realDatabaseChange, commandsToSimulate);

        assertThat(results, hasSize(1));
        assertThat(results.get(0).getDescription(), containsString("Let's crash"));
    }

    @Test
    public void whenFetcherThrowsExceptionThenSingleComparisionMismatchShallBeReturned() {

        var simulator = new DualRunSimulator<>(
                plContext,
                flowConfig,
                List.of(FIELD1),
                fetcherThrowing(new RuntimeException("Let's crash")),
                comparatorThrowing(new RuntimeException("this should not happen"))
        );

        var commandsToSimulate = List.of(
                new TestEntityCreateCommand().with(NAME, "1").with(FIELD1, "one"),
                new TestEntityCreateCommand().with(NAME, "2").with(FIELD1, "two")
        );

        ActualDatabaseMutator<TestEntity> realDatabaseChange = mock(ActualDatabaseMutator.class);

        var results = simulator.runCreation(NAME_AS_UNIQUE_KEY, realDatabaseChange, commandsToSimulate);

        assertThat(results, hasSize(1));
        assertThat(results.get(0).getDescription(), containsString("Let's crash"));
    }

    @Test(expected = TestException.class)
    public void simulatorShouldNotCatchExceptionsOfTheRealMutator() {

        var simulator = new DualRunSimulator<>(plContext, flowConfig, List.of(FIELD1));

        var commandsToSimulate = List.of(new TestEntityCreateCommand().with(NAME, "1"));

        ActualDatabaseMutator<TestEntity> realDatabaseChange = () -> { throw new TestException(); };

        simulator.runCreation(NAME_AS_UNIQUE_KEY, realDatabaseChange, commandsToSimulate);
    }


    // -------------------------------------------------------------------------------------------- //
    //
    //       helper methods
    //
    // -------------------------------------------------------------------------------------------- //

    private static class TestException extends RuntimeException {
    }

    private ChangesValidator<TestEntity> throwingOnValidate(RuntimeException exception) {
        return (a, b, c) -> { throw exception; };
    }

    private ChangesValidator<TestEntity> failAllCommands() {
        return (cmds, op, ctx) -> cmds.forEach(cmd -> ctx.addValidationError(cmd, new ValidationError("isError")));
    }

    private List<String> failedNames(Collection<ComparisonMismatch<TestEntity>> errors) {
        return errors.stream().map(e -> e.getId().get(NAME)).collect(toList());
    }

    private List<Integer> failedIds(Collection<ComparisonMismatch<TestEntity>> errors) {
        return errors.stream().map(e -> e.getId().get(ID)).collect(toList());
    }

    private TestEntity.Key id(int id) {
        return new TestEntity.Key(id);
    }

    private ActualDatabaseMutator<TestEntity> withoutFailures(Runnable operation) {
        return () -> {
            operation.run();
            return emptyList();
        };
    }

    private ActualDatabaseMutator<TestEntity> failAllByNames(String... names) {
        return () -> Seq.of(names).map(name -> new ActualMutatorError<>(new SingleUniqueKey<>(NAME).createIdentifier(name), "please fail")).toList();
    }

    private ActualResultFetcher<TestEntity> emptyFetcher() {
        var mocked = mock(ActualResultFetcher.class);
        when(mocked.fetch(any(), any(), any())).thenReturn(emptyList());
        return mocked;
    }

    private ResultComparator<TestEntity> comparatorThrowing(Exception e) {
        var mocked = mock(ResultComparator.class);
        when(mocked.findMismatches(any(), any())).thenThrow(e);
        return mocked;
    }

    private ActualResultFetcher<TestEntity> fetcherThrowing(Exception e) {
        var mocked = mock(ActualResultFetcher.class);
        when(mocked.fetch(any(), any(), any())).thenThrow(e);
        return mocked;
    }

}