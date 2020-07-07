package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.FieldsCombinationValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FieldsCombinationValidationAdapterTest {

    final private static String STRING_VALUE1 = "value1";
    final private static String STRING_VALUE2 = "value2";

    @Mock
    private FieldsCombinationValidator<TestEntity> validator;

    @Mock
    private EntityField<TestEntity,String> field1;

    @Mock
    private EntityField<TestEntity,String> field2;

    @Mock
    private EntityField<TestEntity,String> field3;

    @Mock
    private EntityField<TestEntity,String> invalidField;

    @Mock
    private EntityChange<TestEntity> entityChange;

    @Mock
    private CurrentEntityState currentState;

    @Mock
    private FieldsCombinationValidator.Substitution<TestEntity, String> fieldSubstitution;

    @InjectMocks
    private FieldsCombinationValidationAdapter<TestEntity> adapter;

    @Before
    public void setUp(){
        when(validator.validateWhen()).thenReturn(p -> true);
        when(validator.validatedFields()).thenReturn(Stream.of(field1, field2));
        when(entityChange.getChangeOperation()).thenReturn(ChangeOperation.UPDATE);
        when(entityChange.isFieldChanged(field1)).thenReturn(true);
        when(entityChange.isFieldChanged(field2)).thenReturn(false);
        when(entityChange.get(field1)).thenReturn(STRING_VALUE1);
        when(currentState.get(field2)).thenReturn(STRING_VALUE2);
    }

    @Test
    public void testSupportedOperation() {
        SupportedChangeOperation supportedChangeOperation = adapter.getSupportedChangeOperation();
        assertEquals("Support create or update commands", supportedChangeOperation, SupportedChangeOperation.CREATE_AND_UPDATE);
    }

    @Test
    public void testFetchFieldsInUpdate() {
        Collection<? extends EntityField<?, ?>> fields = adapter.fetchFields().collect(toSet());
        assertTrue("Fetch field1", fields.contains(field1));
        assertTrue("Fetch field2", fields.contains(field2));
    }

    @Test
    public void testFetchFieldsInCreate() {
        Collection<? extends EntityField<?, ?>> fields = adapter.fetchFields().collect(toSet());
        assertTrue("Fetch field1", fields.contains(field1));
        assertTrue("Fetch field2", fields.contains(field2));;
    }

    @Test
    public void testValidateForCreate() {
        when(entityChange.getChangeOperation()).thenReturn(ChangeOperation.CREATE);
        adapter.validate(entityChange, currentState);
        verify(validator).validate(argThat(fieldCombination -> {
            assertEquals("Field1", fieldCombination.get(field1), STRING_VALUE1);
            assertEquals("Field2", fieldCombination.get(field2), null);
            return true;
        }));
    }

    @Test
    public void testValidateForUpdate() {
        adapter.validate(entityChange, currentState);
        verify(validator).validate(argThat(fieldCombination -> {
            assertEquals("Field1", fieldCombination.get(field1), STRING_VALUE1);
            assertEquals("Field2", fieldCombination.get(field2), STRING_VALUE2);
            return true;
        }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidField() {
        adapter.validate(entityChange, currentState);
        verify(validator).validate(argThat(fieldCombination -> {
            fieldCombination.get(invalidField);
            return true;
        }));
    }

    @Test
    public void testValidateOverrideForUpdate() {
        when(validator.substitutions()).thenReturn(Stream.of(fieldSubstitution)).thenReturn(Stream.of(fieldSubstitution));
        when(fieldSubstitution.overrideField()).thenReturn(field1);
        when(fieldSubstitution.overrideWhen()).thenReturn(value -> true);
        when(fieldSubstitution.overrideHow()).thenReturn(currentState -> "override");


        adapter.validate(entityChange, currentState);
        verify(validator).validate(argThat(fieldCombination -> {
            assertEquals("Field1", fieldCombination.get(field1), "override");
            return true;
        }));
    }

    @Test
    public void testValidateAndNotOverrideForUpdate() {
        when(validator.substitutions()).thenReturn(Stream.of(fieldSubstitution)).thenReturn(Stream.of(fieldSubstitution));
        when(fieldSubstitution.overrideField()).thenReturn(field1);
        when(fieldSubstitution.overrideWhen()).thenReturn(value -> false);
        when(fieldSubstitution.overrideHow()).thenReturn(currentState -> "override");


        adapter.validate(entityChange, currentState);
        verify(validator).validate(argThat(fieldCombination -> {
            assertEquals("Field1", fieldCombination.get(field1), STRING_VALUE1);
            return true;
        }));
    }

    @Test
    public void testFetchFieldsOverrideForUpdate() {
        when(validator.substitutions()).thenReturn(Stream.of(fieldSubstitution)).thenReturn(Stream.of(fieldSubstitution));
        when(validator.fetchFields()).thenReturn(Stream.of(field3));

        Collection<? extends EntityField<?, ?>> fields = adapter.fetchFields().collect(toSet());
        assertTrue("Fetch field1", fields.contains(field1));
        assertTrue("Fetch field2", fields.contains(field2));
        assertTrue("Fetch field3", fields.contains(field3));
    }

    @Test
    public void skipValidationForUpdate() {
        when(validator.validateWhen()).thenReturn(p -> false);
        adapter.validate(entityChange, currentState);
        verify(validator, never()).validate(any());
    }

    @Test
    public void skipValidationForCreate() {
        when(validator.validateWhen()).thenReturn(p -> false);
        adapter.validate(entityChange, currentState);
        verify(validator, never()).validate(any());
    }
}