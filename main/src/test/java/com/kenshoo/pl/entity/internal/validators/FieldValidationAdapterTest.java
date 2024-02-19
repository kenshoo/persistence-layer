package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.FieldValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class FieldValidationAdapterTest {

    final static String STRING_VALUE = "value";

    @Mock
    private FieldValidator<TestEntity, String> validator;

    @Mock
    private ChangeEntityCommand<TestEntity> command;

    @Mock
    private EntityField<TestEntity, String> field;

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

    private FieldValidationAdapter<TestEntity, String> adapter;

    @Before
    public void setUp(){
        when(validator.validatedField()).thenReturn(field);
        adapter = new FieldValidationAdapter<>(validator);
    }

    @Test
    public void testSupportedOperation() {
        SupportedChangeOperation supportedChangeOperation = adapter.getSupportedChangeOperation();
        assertEquals("Support create or update commands", supportedChangeOperation, SupportedChangeOperation.CREATE_AND_UPDATE);
    }

    @Test
    public void testFetchFieldsInUpdate() {
        when(validator.fetchFields()).thenReturn(Stream.of(field1, field2));
        Collection<? extends EntityField<?, ?>> fields = adapter.fieldsToFetch().collect(toList());
        assertEquals("Do not fetch field", fields.size(), 2);
        assertTrue("Fetch field1", fields.contains(field1));
        assertTrue("Fetch field2", fields.contains(field2));
    }

    @Test
    public void testFetchFieldsInCreate() {
        Collection<? extends EntityField<?, ?>> fields = adapter.fieldsToFetch().collect(toList());
        assertEquals("Do not fetch field", fields.size(), 0);
    }

    @Test
    public void testTriggeredByFields() {
        assertTrue("Triggered by field", adapter.trigger().triggeredByFields(List.of(this.field)));
    }

    @Test
    public void testValidateValue() {
        when(entityChange.isFieldChanged(field)).thenReturn(true);
        when(validator.validateWhen()).thenReturn(value -> true);
        when(entityChange.get(field)).thenReturn(STRING_VALUE);
        adapter.validate(entityChange, currentState, finalState);
        verify(validator).validate(STRING_VALUE);
    }

    @Test
    public void testNoValueToValidate() {
        when(entityChange.isFieldChanged(field)).thenReturn(false);
        adapter.validate(entityChange, currentState, finalState);
        verify(validator, never()).validate(STRING_VALUE);
    }

    @Test
    public void testSkipValidateValue() {
        when(entityChange.isFieldChanged(field)).thenReturn(true);
        when(validator.validateWhen()).thenReturn(value -> false);
        when(entityChange.get(field)).thenReturn(STRING_VALUE);
        adapter.validate(entityChange, currentState, finalState);
        verify(validator, never()).validate(STRING_VALUE);
    }

}