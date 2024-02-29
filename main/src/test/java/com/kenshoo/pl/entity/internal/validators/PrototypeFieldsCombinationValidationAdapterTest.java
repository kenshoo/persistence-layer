package com.kenshoo.pl.entity.internal.validators;

import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PrototypeFieldsCombinationValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PrototypeFieldsCombinationValidationAdapterTest {

    final static String STRING_VALUE1 = "value1";
    final static String STRING_VALUE2 = "value2";

    @Mock
    private PrototypeFieldsCombinationValidator validator;

    @Mock
    private EntityFieldPrototype<String> invalidField;

    @Mock
    private EntityChange<TestEntity> entityChange;

    @Mock
    private CurrentEntityState currentState;

    @Mock
    private FinalEntityState finalState;

    @Mock
    private EntityField<TestEntity,String> field1;

    @Mock
    private EntityField<TestEntity,String> field2;

    private PrototypeFieldsCombinationValidationAdapter<TestEntity> adapter;

    @Before
    public void setUp(){
        adapter = new PrototypeFieldsCombinationValidationAdapter<>(validator, ImmutableMap.of(TestDataFieldPrototype.FIELD_1, TestEntity.FIELD_1, TestDataFieldPrototype.FIELD_2, TestEntity.FIELD_2));

        when(entityChange.getChangeOperation()).thenReturn(ChangeOperation.UPDATE);
        when(entityChange.isFieldChanged(TestEntity.FIELD_1)).thenReturn(true);
        when(entityChange.isFieldChanged(TestEntity.FIELD_2)).thenReturn(false);
        when(entityChange.get(TestEntity.FIELD_1)).thenReturn(STRING_VALUE1);
        when(currentState.get(TestEntity.FIELD_2)).thenReturn(STRING_VALUE2);
    }

    @Test
    public void testSupportedOperation() {
        SupportedChangeOperation supportedChangeOperation = adapter.getSupportedChangeOperation();
        assertEquals("Support create or update commands", supportedChangeOperation, SupportedChangeOperation.CREATE_AND_UPDATE);
    }

    @Test
    public void testFetchFieldsWithoutParentFields() {
        when(validator.fetchFields()).thenReturn(Stream.of());
        Collection<? extends EntityField<?, ?>> fields = adapter.fieldsToFetch().collect(Collectors.toUnmodifiableList());
        assertTrue("Fetch field1", fields.contains(TestEntity.FIELD_1));
        assertTrue("Fetch field2", fields.contains(TestEntity.FIELD_2));
    }

    @Test
    public void testFetchFieldsWithParentFields() {
        when(validator.fetchFields()).thenReturn(Stream.of(field1, field2));
        Collection<? extends EntityField<?, ?>> fields = adapter.fieldsToFetch().collect(Collectors.toUnmodifiableList());
        assertTrue("Fetch field1", fields.contains(TestEntity.FIELD_1));
        assertTrue("Fetch field2", fields.contains(TestEntity.FIELD_2));
        assertTrue("Fetch field2", fields.contains(field1));
        assertTrue("Fetch field2", fields.contains(field2));
    }

    @Test
    public void testValidateForCreate() {
        when(validator.validateWhen()).thenReturn(value -> true);
        when(entityChange.getChangeOperation()).thenReturn(ChangeOperation.CREATE);
        adapter.validate(entityChange, currentState, finalState);
        verify(validator).validate(argThat(fieldCombination -> {
            assertEquals("Field1", fieldCombination.get(TestDataFieldPrototype.FIELD_1), STRING_VALUE1);
            assertNull("Field2", fieldCombination.get(TestDataFieldPrototype.FIELD_2));
            return true;
        }));
    }

    @Test
    public void testValidateForUpdate() {
        when(validator.validateWhen()).thenReturn(value -> true);
        adapter.validate(entityChange, currentState, finalState);
        verify(validator).validate(argThat(fieldCombination -> {
            assertEquals("Field1", fieldCombination.get(TestDataFieldPrototype.FIELD_1), STRING_VALUE1);
            assertEquals("Field2", fieldCombination.get(TestDataFieldPrototype.FIELD_2), STRING_VALUE2);
            return true;
        }));
    }
    @Test
    public void testSkipValidateValue() {
        when(validator.validateWhen()).thenReturn(value -> false);
        adapter.validate(entityChange, currentState, finalState);
        verify(validator,  never()).validate(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidField() {
        when(validator.validateWhen()).thenReturn(value -> true);
        adapter.validate(entityChange, currentState,finalState);
        verify(validator).validate(argThat(fieldCombination -> {
            fieldCombination.get(invalidField);
            return true;
        }));
    }

    @Test
    public void testTriggeredByFields() {
        assertTrue(adapter.trigger().triggeredByFields(List.of(TestEntity.FIELD_1)));
    }

}