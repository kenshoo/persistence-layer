package com.kenshoo.pl.entity;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

import static java.util.Map.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class FinalEntityStateTest {

    private final EntityChange<TestEntity> SIMPLE_ENTITY_NO_CHANGES = entityChangeFor(TestEntity.INSTANCE, of());

    @Test
    public void whenFieldIsInBothCurrentStateAndChangeThenReturnValueFromChange() {
        FinalEntityState finalState = new FinalEntityState(
                currentState(of(TestEntity.FIELD_1, "from current state")),
                entityChangeFor(TestEntity.INSTANCE, of(TestEntity.FIELD_1, "from change"))
        );

        assertTrue(finalState.containsField(TestEntity.FIELD_1));
        assertThat(finalState.get(TestEntity.FIELD_1), is("from change"));
    }

    @Test
    public void whenFieldIsOnlyInCurrentStateThenReturnValueFromCurrentState() {
        FinalEntityState finalState = new FinalEntityState(
                currentState(of(TestEntity.FIELD_1, "from current state")),
                SIMPLE_ENTITY_NO_CHANGES
        );

        assertTrue(finalState.containsField(TestEntity.FIELD_1));
        assertThat(finalState.get(TestEntity.FIELD_1), is("from current state"));
    }

    @Test
    public void whenFieldIsOnlyInChangeThenReturnValueFromChange() {
        FinalEntityState finalState = new FinalEntityState(
                CurrentEntityState.EMPTY,
                entityChangeFor(TestEntity.INSTANCE, of(TestEntity.FIELD_1, "current_field_1"))
        );

        assertTrue(finalState.containsField(TestEntity.FIELD_1));
        assertThat(finalState.get(TestEntity.FIELD_1), is("current_field_1"));
    }

    @Test
    public void whenFieldInEntityChangeAndHasIrreversibleStringConverterShouldReturnCorrectValue() {
        FinalEntityState finalState = new FinalEntityState(
                CurrentEntityState.EMPTY,
                entityChangeFor(TestEntityWithIrreversibleConverters.INSTANCE,
                        of(TestEntityWithIrreversibleConverters.FIELD_VARCHAR, "abc   "))
        );

        assertTrue(finalState.containsField(TestEntityWithIrreversibleConverters.FIELD_VARCHAR));
        assertThat(finalState.get(TestEntityWithIrreversibleConverters.FIELD_VARCHAR), is("abc"));
    }

    @Test
    public void whenFieldInEntityChangeAndHasFloatingPointToIntegerConversionShouldReturnCorrectValue() {
        FinalEntityState finalState = new FinalEntityState(
                CurrentEntityState.EMPTY,
                entityChangeFor(TestEntityWithIrreversibleConverters.INSTANCE,
                        of(TestEntityWithIrreversibleConverters.FIELD_INTEGER, BigDecimal.valueOf(32.11)))
        );

        assertTrue(finalState.containsField(TestEntityWithIrreversibleConverters.FIELD_INTEGER));
        assertThat(finalState.get(TestEntityWithIrreversibleConverters.FIELD_INTEGER), is(BigDecimal.valueOf(32)));
    }

    @Test
    public void whenFieldInEntityChangeAndHasIrreversibleFloatingPointConversionShouldReturnCorrectValue() {
        FinalEntityState finalState = new FinalEntityState(
                CurrentEntityState.EMPTY,
                entityChangeFor(TestEntityWithIrreversibleConverters.INSTANCE,
                        of(TestEntityWithIrreversibleConverters.FIELD_DOUBLE, BigDecimal.valueOf(31.111999)))
        );

        assertTrue(finalState.containsField(TestEntityWithIrreversibleConverters.FIELD_DOUBLE));
        assertThat(finalState.get(TestEntityWithIrreversibleConverters.FIELD_DOUBLE), is(BigDecimal.valueOf(31.112)));
    }

    @Test
    public void containsReturnsFalseForNonExistingField() {
        FinalEntityState finalState = new FinalEntityState(CurrentEntityState.EMPTY, SIMPLE_ENTITY_NO_CHANGES);
        assertFalse(finalState.containsField(TestEntity.FIELD_1));
    }

    @Test
    public void gettingFieldThatDoesNotExistsThenReturnNull() {
        FinalEntityState finalState = new FinalEntityState(CurrentEntityState.EMPTY, SIMPLE_ENTITY_NO_CHANGES);
        assertNull(finalState.get(TestEntity.FIELD_1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void throwUnsupportedExceptionWhenTryingToGetMany() {
        FinalEntityState finalState = new FinalEntityState(CurrentEntityState.EMPTY, SIMPLE_ENTITY_NO_CHANGES);
        assertNull(finalState.getMany(TestChildEntity.INSTANCE));
    }

    private static CurrentEntityState currentState(Map<EntityField<?, ?>, Object> fieldValueMap) {
        CurrentEntityMutableState state = new CurrentEntityMutableState();
        fieldValueMap.forEach((f, v) -> set(state, f, v));
        return state;
    }

    private static <E extends EntityType<E>> EntityChange<E> entityChangeFor(E entityType, Map<EntityField<E, ?>, ?> values) {
        ChangeEntityCommand<E> cmd = new CreateEntityCommand<>(entityType);
        values.forEach((field, value) -> set(cmd, field, value));
        return cmd;
    }

    @SuppressWarnings("unchecked")
    private static <E extends EntityType<E>, T> void set(ChangeEntityCommand<E> cmd, EntityField<E, T> field, Object value) {
        cmd.set(field, (T)value);
    }

    @SuppressWarnings("unchecked")
    private static <T> void set(CurrentEntityMutableState state, EntityField<?, T> field, Object value) {
        state.set(field, (T)value);
    }
}
