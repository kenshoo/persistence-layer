package com.kenshoo.pl.entity;

import org.junit.Test;
import java.util.Map;
import static com.google.common.collect.ImmutableMap.of;
import static com.kenshoo.pl.entity.TestEntity.FIELD_1;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class FinalEntityStateTest {

    private final EntityChange<TestEntity> NO_CHANGES = entityChange(emptyMap());

    @Test
    public void whenFieldIsInBothCurrentStateAndChangeThenReturnValueFromChange() {
        FinalEntityState finalState = new FinalEntityState(
                currentState(of(FIELD_1, "from current state")),
                entityChange(of(FIELD_1, "from change"))
        );

        assertTrue(finalState.containsField(FIELD_1));
        assertThat(finalState.get(FIELD_1), is("from change"));
    }

    @Test
    public void whenFieldIsOnlyInCurrentStateThenReturnValueFromCurrentState() {
        FinalEntityState finalState = new FinalEntityState(
                currentState(of(FIELD_1, "from current state")),
                NO_CHANGES
        );

        assertTrue(finalState.containsField(FIELD_1));
        assertThat(finalState.get(FIELD_1), is("from current state"));
    }

    @Test
    public void whenFieldIsOnlyInChangeThenReturnValueFromChange() {
        FinalEntityState finalState = new FinalEntityState(
                CurrentEntityState.EMPTY,
                entityChange(of(FIELD_1, "current_field_1"))
        );

        assertTrue(finalState.containsField(FIELD_1));
        assertThat(finalState.get(FIELD_1), is("current_field_1"));
    }

    @Test
    public void containsReturnsFalseForNonExistingField() {
        FinalEntityState finalState = new FinalEntityState(CurrentEntityState.EMPTY, NO_CHANGES);
        assertFalse(finalState.containsField(FIELD_1));
    }

    @Test
    public void gettingFieldThatDoesNotExistsThenReturnNull() {
        FinalEntityState finalState = new FinalEntityState(CurrentEntityState.EMPTY, NO_CHANGES);
        assertNull(finalState.get(FIELD_1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void throwUnsupportedExceptionWhenTryingToGetMany() {
        FinalEntityState finalState = new FinalEntityState(CurrentEntityState.EMPTY, NO_CHANGES);
        assertNull(finalState.getMany(TestChildEntity.INSTANCE));
    }

    private static CurrentEntityState currentState(Map<EntityField, Object> values) {
        CurrentEntityMutableState state = new CurrentEntityMutableState();
        values.forEach(state::set);
        return state;
    }

    private static EntityChange<TestEntity> entityChange(Map<EntityField<TestEntity, ?>, ?> values) {
        ChangeEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        values.forEach((field, value) -> set(cmd, field, value));
        return cmd;
    }

    private static <T> void set(ChangeEntityCommand<TestEntity> cmd, EntityField<TestEntity, T> field, Object value) {
        cmd.set(field, (T)value);
    }
}
