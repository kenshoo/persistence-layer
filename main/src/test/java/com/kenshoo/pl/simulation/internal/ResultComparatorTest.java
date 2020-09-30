package com.kenshoo.pl.simulation.internal;

import com.kenshoo.pl.entity.*;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;

import static com.kenshoo.pl.entity.TestEntity.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;

public class ResultComparatorTest {

    private final ResultComparator<TestEntity> comparator = new ResultComparator<>(List.of(
            FIELD_1,
            FIELD_2
    ));

    private final Entity ORIGINAL_STATE = state(
            FIELD_1, "one",
            FIELD_2, "two"
    );

    private final List<ValidationError> PL_ERRORS = List.of(new ValidationError("oh no"));
    private final List<ValidationError> PL_SUCCESS = emptyList();

    @Test
    public void shouldReturnErrorWhenOnlySimulatedFailedByActualMutatorSucceeded() {

        final var errors = comparator.findMismatches(
                List.of(failedCommand()),
                List.of(actualSuccessWithoutEffectOnDB())
        );

        assertThat(errors, not(empty()));
    }

    private SimulatedResult<TestEntity> failedCommand() {
        return new SimulatedResult<>(new Command(), new Key(1), PL_ERRORS);
    }

    @Test
    public void shouldReturnErrorWhenSimulatedSucceededByActualMutatorFailed() {

        final var errors = comparator.findMismatches(
                List.of(successful(new Command())),
                List.of(new ActualError("SQL failure: 123"))
        );

        assertThat(errors, not(empty()));
    }

    @Test
    public void noErrorsWhenBothSimulatedAndActualMutatorFailed() {

        final var errors = comparator.findMismatches(
                List.of(failedCommand()),
                List.of(new ActualError("SQL failure: 123"))
        );

        assertThat(errors, empty());
    }

    @Test
    public void noErrorsWhenBothSimulatedAndActualMutatorSucceeded() {

        final var errors = comparator.findMismatches(
                List.of(successful(new Command())),
                List.of(actualSuccessWithoutEffectOnDB())
        );

        assertThat(errors, empty());
    }

    @Test
    public void shouldReturnErrorWhenFieldWasOnlyChangedInSimulatedCommand() {

        final var errors = comparator.findMismatches(
                List.of(successful(new Command().with(FIELD_1, "some new value"))),
                List.of(actualSuccessWithoutEffectOnDB())
        );

        assertThat(errors, not(empty()));
    }

    @Test
    public void shouldReturnErrorWhenFieldWasOnlyChangedInDatabase() {

        final var errors = comparator.findMismatches(
                List.of(successful(new Command())),
                List.of(actualSuccess(ORIGINAL_STATE, modifiedBy(FIELD_1, "some new value")))
        );

        assertThat(errors, not(empty()));
    }

    @Test
    public void shouldReturnErrorWhenFieldChangedDifferentlyInDBThanValueInCommand() {

        final var errors = comparator.findMismatches(
                List.of(successful(new Command().with(FIELD_1, "bad"))),
                List.of(actualSuccess(ORIGINAL_STATE, modifiedBy(FIELD_1, "good")))
        );

        assertThat(errors, not(empty()));
    }

    private SimulatedResult<TestEntity> successful(Command command) {
        return new SimulatedResult<>(command, new Key(1), PL_SUCCESS);
    }

    private ActualSuccess actualSuccessWithoutEffectOnDB() {
        return new ActualSuccess(ORIGINAL_STATE, ORIGINAL_STATE);
    }

    private ActualSuccess actualSuccess(Entity originalState, Function<Entity, Entity> modificationOnOrignalState) {
        return new ActualSuccess(originalState, modificationOnOrignalState.apply(originalState));
    }

    private <T> Function<Entity, Entity> modifiedBy(EntityField<TestEntity, T> field, T value) {
        return original -> {
            var modified = spy(original);
            when(modified.get(field)).thenReturn(value);
            return modified;
        };
    }

    private static class Command extends CreateEntityCommand<TestEntity> implements EntityCommandExt<TestEntity, Command> {
        Command() {
            super(TestEntity.INSTANCE);
        }
    }

    private <T1, T2> Entity state(
            EntityField<TestEntity, T1> field1, T1 value1,
            EntityField<TestEntity, T2> field2, T2 value2
            ) {
        final var state = new CurrentEntityMutableState();
        state.set(field1, value1);
        state.set(field2, value2);
        return state;
    }

}
