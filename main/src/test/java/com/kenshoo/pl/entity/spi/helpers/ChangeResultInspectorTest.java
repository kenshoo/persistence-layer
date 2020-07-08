package com.kenshoo.pl.entity.spi.helpers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.CurrentEntityState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.Silent.class)
public class ChangeResultInspectorTest {

    public static final HashMap<Identifier<TestEntity>, CurrentEntityState> SOME_CURRENT_ENTITY_STATE = Maps.newHashMap();

    @Test
    public void testUpdate() {
        Map<Identifier<TestEntity>, CurrentEntityState> beforeEntities = Maps.newHashMap();
        UpdateTestEntityCommand command = new UpdateTestEntityCommand();
        command.set(TestEntity.FIELD_1, "value1");
        CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(TestEntity.FIELD_1, "value1");
        UpdateTestEntityChangeResult testEntityChangeResult = new UpdateTestEntityChangeResult(command);
        UpdateResult<TestEntity, TestEntity.Key> results = new UpdateResult<>(ImmutableList.of(testEntityChangeResult));

        ChangeResultInspector<TestEntity> inspector = new ChangeResultInspector.Builder<TestEntity>()
                .withInspectedFields(ImmutableList.of(TestEntity.FIELD_1))
                .inspectedFlow("Inspected flow").build();

        ObservedResult<TestEntity> observedResult = ObservedResult.of(new TestEntity.Key(1), currentState);
        inspector.inspect(beforeEntities, results, ImmutableList.of(observedResult));
        assertEquals("Identical value", observedResult.getInspectedStatus(), ObservedResult.InspectedStatus.IDENTICAL);

    }

    @Test
    public void testFalseUpdate() {
        UpdateTestEntityCommand command = new UpdateTestEntityCommand();
        CurrentEntityMutableState befoeEntity = new CurrentEntityMutableState();
        befoeEntity.set(TestEntity.FIELD_1, "value1");
        Map<Identifier<TestEntity>, CurrentEntityState> beforeEntities = Maps.newHashMap();
        beforeEntities.put(command.getIdentifier(), befoeEntity);
        CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(TestEntity.FIELD_1, "value1");
        UpdateTestEntityChangeResult testEntityChangeResult = new UpdateTestEntityChangeResult(command);
        UpdateResult<TestEntity, TestEntity.Key> results = new UpdateResult<>(ImmutableList.of(testEntityChangeResult));

        ChangeResultInspector<TestEntity> inspector = new ChangeResultInspector.Builder<TestEntity>()
                .withInspectedFields(ImmutableList.of(TestEntity.FIELD_1))
                .inspectedFlow("Inspected flow").build();

        ObservedResult<TestEntity> observedResult = ObservedResult.of(new TestEntity.Key(1), currentState);
        inspector.inspect(beforeEntities, results, ImmutableList.of(observedResult));
        assertEquals("Identical value", observedResult.getInspectedStatus(), ObservedResult.InspectedStatus.IDENTICAL);

    }

    @Test
    public void testUpdateValueMismatch() {
        Map<Identifier<TestEntity>, CurrentEntityState> beforeEntities = Maps.newHashMap();
        UpdateTestEntityCommand command = new UpdateTestEntityCommand();
        command.set(TestEntity.FIELD_1, "value1");
        UpdateTestEntityChangeResult testEntityChangeResult = new UpdateTestEntityChangeResult(command);
        UpdateResult<TestEntity, TestEntity.Key> results = new UpdateResult<>(ImmutableList.of(testEntityChangeResult));
        CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(TestEntity.FIELD_1, "value2");

        ChangeResultInspector<TestEntity> inspector = new ChangeResultInspector.Builder<TestEntity>()
                .withInspectedFields(ImmutableList.of(TestEntity.FIELD_1))
                .inspectedFlow("Inspected flow").build();

        ObservedResult<TestEntity> observedResult = ObservedResult.of(new TestEntity.Key(1),currentState);
        inspector.inspect(beforeEntities, results, ImmutableList.of(observedResult));
        assertEquals("Value mismatch", observedResult.getInspectedStatus(), ObservedResult.InspectedStatus.VALUE_MISMATCH);

    }

    @Test
    public void testUpdateWithLegacyError() {
        Map<Identifier<TestEntity>, CurrentEntityState> beforeEntities = Maps.newHashMap();
        UpdateTestEntityCommand command = new UpdateTestEntityCommand();
        command.set(TestEntity.FIELD_1, "value1");
        UpdateTestEntityChangeResult testEntityChangeResult = new UpdateTestEntityChangeResult(command);
        UpdateResult<TestEntity, TestEntity.Key> results = new UpdateResult<>(ImmutableList.of(testEntityChangeResult));

        ChangeResultInspector<TestEntity> inspector = new ChangeResultInspector.Builder<TestEntity>()
                .withInspectedFields(ImmutableList.of(TestEntity.FIELD_1))
                .inspectedFlow("Inspected flow").build();

        ObservedResult<TestEntity> observedResult = ObservedResult.error(new TestEntity.Key(1), "Validation error");
        inspector.inspect(beforeEntities, results, ImmutableList.of(observedResult));
        assertEquals("Legacy error mismatch", observedResult.getInspectedStatus(), ObservedResult.InspectedStatus.LEGACY_ERROR_MISMATCH);
    }

    @Test
    public void testUpdateWithPersistenceError() {
        Map<Identifier<TestEntity>, CurrentEntityState> beforeEntities = Maps.newHashMap();
        UpdateTestEntityCommand command = new UpdateTestEntityCommand();
        command.set(TestEntity.FIELD_1, "value1");
        UpdateTestEntityChangeResult testEntityChangeResult = new UpdateTestEntityChangeResult(command, ImmutableList.of(new ValidationError("Validation error")));
        UpdateResult<TestEntity, TestEntity.Key> results = new UpdateResult<>(ImmutableList.of(testEntityChangeResult));
        CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(TestEntity.FIELD_1, "value1");

        ChangeResultInspector<TestEntity> inspector = new ChangeResultInspector.Builder<TestEntity>()
                .withInspectedFields(ImmutableList.of(TestEntity.FIELD_1))
                .inspectedFlow("Inspected flow").build();

        ObservedResult<TestEntity> observedResult = ObservedResult.of(new TestEntity.Key(1), currentState);
        inspector.inspect(beforeEntities, results, ImmutableList.of(observedResult));
        assertEquals("Legacy error mismatch", observedResult.getInspectedStatus(), ObservedResult.InspectedStatus.PERSISTENCE_ERROR_MISMATCH);
    }


    @Test
    public void testCreate() {
        Map<Identifier<TestEntity>, CurrentEntityState> beforeEntities = Maps.newHashMap();
        CreateTestEntityCommand command = new CreateTestEntityCommand();
        command.set(TestEntity.FIELD_1, "value1");
        command.setIdentifier(new TestEntity.Key(1));
        EntityCreateResult<TestEntity, TestEntity.Key> testEntityChangeResult = new EntityCreateResult<>(command);
        CreateResult<TestEntity, TestEntity.Key> results = new CreateResult<>(ImmutableList.of(testEntityChangeResult));
        CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(TestEntity.FIELD_1, "value1");

        ChangeResultInspector<TestEntity> inspector = new ChangeResultInspector.Builder<TestEntity>()
                .withInspectedFields(ImmutableList.of(TestEntity.FIELD_1))
                .inspectedFlow("Inspected flow").build();

        ObservedResult<TestEntity> observedResult = ObservedResult.of(new TestEntity.Key(1), currentState);
        inspector.inspect(beforeEntities, results, ImmutableList.of(observedResult));
        assertEquals("Identical value", observedResult.getInspectedStatus(), ObservedResult.InspectedStatus.IDENTICAL);

    }

    @Test
    public void testCreateValueMismatch() {
        CreateTestEntityCommand command = new CreateTestEntityCommand();
        command.set(TestEntity.FIELD_1, "value1");
        command.setIdentifier(new TestEntity.Key(1));
        EntityCreateResult<TestEntity, TestEntity.Key> testEntityChangeResult = new EntityCreateResult<>(command);
        CreateResult<TestEntity, TestEntity.Key> results = new CreateResult<>(ImmutableList.of(testEntityChangeResult));
        CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(TestEntity.FIELD_1, "value2");

        ChangeResultInspector<TestEntity> inspector = new ChangeResultInspector.Builder<TestEntity>()
                .withInspectedFields(ImmutableList.of(TestEntity.FIELD_1))
                .inspectedFlow("Inspected flow").build();

        ObservedResult<TestEntity> observedResult = ObservedResult.of(new TestEntity.Key(1), currentState);
        inspector.inspect(SOME_CURRENT_ENTITY_STATE, results, ImmutableList.of(observedResult));
        assertEquals("Value mismatch", observedResult.getInspectedStatus(), ObservedResult.InspectedStatus.VALUE_MISMATCH);
    }

    @Test
    public void testCreateLegacyWithError() {
        CreateTestEntityCommand command = new CreateTestEntityCommand();
        command.set(TestEntity.FIELD_1, "value1");
        command.setIdentifier(new TestEntity.Key(1));
        EntityCreateResult<TestEntity, TestEntity.Key> testEntityChangeResult = new EntityCreateResult<>(command);
        CreateResult<TestEntity, TestEntity.Key> results = new CreateResult<>(ImmutableList.of(testEntityChangeResult));

        ChangeResultInspector<TestEntity> inspector = new ChangeResultInspector.Builder<TestEntity>()
                .withInspectedFields(ImmutableList.of(TestEntity.FIELD_1))
                .inspectedFlow("Inspected flow").build();

        ObservedResult<TestEntity> observedResult = ObservedResult.error(new TestEntity.Key(1), "Validation error");
        inspector.inspect(SOME_CURRENT_ENTITY_STATE, results, ImmutableList.of(observedResult));
        assertEquals("Legacy error mismatch", observedResult.getInspectedStatus(), ObservedResult.InspectedStatus.LEGACY_ERROR_MISMATCH);
    }

    @Test
    public void testCreatePersistenceWithError() {
        CreateTestEntityCommand command = new CreateTestEntityCommand();
        command.set(TestEntity.FIELD_1, "value1");
        EntityCreateResult<TestEntity, TestEntity.Key> testEntityChangeResult = new EntityCreateResult<>(command, ImmutableList.of(new ValidationError("Validation error")));
        CreateResult<TestEntity, TestEntity.Key> results = new CreateResult<>(ImmutableList.of(testEntityChangeResult));
        CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(TestEntity.FIELD_1, "value1");

        ChangeResultInspector<TestEntity> inspector = new ChangeResultInspector.Builder<TestEntity>()
                .withInspectedFields(ImmutableList.of(TestEntity.FIELD_1))
                .inspectedFlow("Inspected flow").build();

        ObservedResult<TestEntity> observedResult = ObservedResult.of(new TestEntity.Key(1), currentState);
        inspector.inspect(SOME_CURRENT_ENTITY_STATE, results, ImmutableList.of(observedResult));
        assertEquals("Persistence error mismatch", observedResult.getInspectedStatus(), ObservedResult.InspectedStatus.PERSISTENCE_ERROR_MISMATCH);
    }

    private static class CreateTestEntityCommand extends CreateEntityCommand<TestEntity> {
        public CreateTestEntityCommand() {
            super(TestEntity.INSTANCE);
        }
    }

    private static class UpdateTestEntityCommand extends UpdateEntityCommand<TestEntity, TestEntity.Key> {
        public UpdateTestEntityCommand() {
            super(TestEntity.INSTANCE, new TestEntity.Key(1));
        }
    }

    private static class UpdateTestEntityChangeResult extends EntityUpdateResult<TestEntity,  TestEntity.Key> {
        public UpdateTestEntityChangeResult(UpdateTestEntityCommand command) {
            super(command);
        }

        public UpdateTestEntityChangeResult(UpdateTestEntityCommand command, Collection<ValidationError> errors) {
            super(command, errors);
        }
    }
}