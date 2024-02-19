package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.FieldComplexValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FieldComplexValidationAdapterTest {

    final static String STRING_VALUE = "value";

    @Mock
    private FieldComplexValidator<TestEntity, String> validator;

    @Mock
    private EntityField<TestEntity, String> field;

    @Mock
    private EntityField<?, ?> fetchField;

    @Mock
    private EntityChange<TestEntity> entityChange;

    @Mock
    private CurrentEntityState currentState;

    @Mock
    private FinalEntityState finalState;

    private FieldComplexValidationAdapter<TestEntity, String> adapter;

    @Before
    public void setUp(){
        when(validator.validatedField()).thenReturn(field);
        when(validator.fetchFields()).thenReturn(Stream.of(fetchField));
        adapter = new FieldComplexValidationAdapter<>(validator);
    }

    @Test
    public void testSupportedOperation() {
        SupportedChangeOperation supportedChangeOperation = adapter.getSupportedChangeOperation();
        assertEquals("Support create or update commands", supportedChangeOperation, SupportedChangeOperation.CREATE_AND_UPDATE);
    }

    @Test
    public void testFetchFieldsInCreate() {
        Optional<? extends EntityField<?, ?>> fieldToFetch = adapter.fieldsToFetch().findFirst();
        assertTrue(fieldToFetch.isPresent());
        assertEquals("Fetch field", fieldToFetch.get(), fetchField);
    }

    @Test
    public void testTriggeredByFields() {
        assertTrue(adapter.trigger().triggeredByFields(List.of(field)));
    }

    @Test
    public void testValidateValue() {
        when(entityChange.isFieldChanged(field)).thenReturn(true);
        when(entityChange.get(field)).thenReturn(STRING_VALUE);
        adapter.validate(entityChange, currentState, finalState);
        verify(validator).validate(STRING_VALUE, currentState);
    }
}