package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.TestEntity;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FieldsCombinationValidationAdapterTest {

    final static String STRING_VALUE1 = "value1";
    final static String STRING_VALUE2 = "value2";

    @Mock
    private FieldsCombinationValidator<TestEntity> validator;

    @Mock
    private ChangeEntityCommand<TestEntity> command;

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
    private Entity entity;

    @Mock
    private FieldsCombinationValidator.Substitution<TestEntity, String> fieldSubstitution;

    @InjectMocks
    private FieldsCombinationValidationAdapter<TestEntity> adapter;

    @Before
    public void setUp(){
        when(validator.validatedFields()).thenReturn(Stream.of(field1, field2));
        when(entityChange.isFieldChanged(field1)).thenReturn(true);
        when(entityChange.isFieldChanged(field2)).thenReturn(false);
        when(entityChange.get(field1)).thenReturn(STRING_VALUE1);
        when(entity.get(field2)).thenReturn(STRING_VALUE2);
    }

    @Test
    public void testSupportedOperation() {
        SupportedChangeOperation supportedChangeOperation = adapter.getSupportedChangeOperation();
        assertEquals("Support create or update commands", supportedChangeOperation, SupportedChangeOperation.CREATE_AND_UPDATE);
    }

    @Test
    public void testFetchFieldsInUpdate() {
        Collection<? extends EntityField<?, ?>> fields = adapter.getFieldsToFetch(ChangeOperation.UPDATE).collect(toSet());
        assertTrue("Fetch field1", fields.contains(field1));
        assertTrue("Fetch field2", fields.contains(field2));
    }

    @Test
    public void testFetchFieldsInCreate() {
        Stream<? extends EntityField<?, ?>> fields = adapter.getFieldsToFetch(ChangeOperation.CREATE);
        assertFalse("Do not fetch fields", fields.findFirst().isPresent());
    }

    @Test
    public void testValidateForCreate() {
        adapter.validate(entityChange, entity, ChangeOperation.CREATE);
        verify(validator).validate(argThat(fieldCombination -> {
            assertEquals("Field1", fieldCombination.get(field1), STRING_VALUE1);
            assertEquals("Field2", fieldCombination.get(field2), null);
            return true;
        }));
    }

    @Test
    public void testValidateForUpdate() {
        adapter.validate(entityChange, entity, ChangeOperation.UPDATE);
        verify(validator).validate(argThat(fieldCombination -> {
            assertEquals("Field1", fieldCombination.get(field1), STRING_VALUE1);
            assertEquals("Field2", fieldCombination.get(field2), STRING_VALUE2);
            return true;
        }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidField() {
        adapter.validate(entityChange, entity, ChangeOperation.UPDATE);
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
        when(fieldSubstitution.overrideHow()).thenReturn(entity -> "override");


        adapter.validate(entityChange, entity, ChangeOperation.UPDATE);
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
        when(fieldSubstitution.overrideHow()).thenReturn(entity -> "override");


        adapter.validate(entityChange, entity, ChangeOperation.UPDATE);
        verify(validator).validate(argThat(fieldCombination -> {
            assertEquals("Field1", fieldCombination.get(field1), STRING_VALUE1);
            return true;
        }));
    }

    @Test
    public void testFetchFieldsOverrideForUpdate() {
        when(validator.substitutions()).thenReturn(Stream.of(fieldSubstitution)).thenReturn(Stream.of(fieldSubstitution));
        when(fieldSubstitution.fetchFields()).thenReturn(Stream.of(field3));

        Collection<? extends EntityField<?, ?>> fields = adapter.getFieldsToFetch(ChangeOperation.UPDATE).collect(toSet());
        assertTrue("Fetch field1", fields.contains(field1));
        assertTrue("Fetch field2", fields.contains(field2));
        assertTrue("Fetch field3", fields.contains(field3));

    }
}