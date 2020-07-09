package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.TestEntity;
import com.kenshoo.pl.entity.spi.FieldComplexValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    private ChangeEntityCommand<TestEntity> command;

    @Mock
    private EntityField<TestEntity, String> field;

    @Mock
    private EntityField<?, ?> fetchField;

    @Mock
    private EntityChange<TestEntity> entityChange;

    @Mock
    private CurrentEntityState currentState;

    @InjectMocks
    private FieldComplexValidationAdapter<TestEntity, String> adapter;

    @Before
    public void setUp(){
        when(validator.validatedField()).thenReturn(field);
        when(validator.fetchFields()).thenReturn(Stream.of(fetchField));
    }

    @Test
    public void testSupportedOperation() {
        SupportedChangeOperation supportedChangeOperation = adapter.getSupportedChangeOperation();
        assertEquals("Support create or update commands", supportedChangeOperation, SupportedChangeOperation.CREATE_AND_UPDATE);
    }

    @Test
    public void testFetchFieldsInCreate() {
        Optional<? extends EntityField<?, ?>> fieldToFetch = adapter.fetchFields().findFirst();
        assertTrue(fieldToFetch.isPresent());
        assertEquals("Fetch field", fieldToFetch.get(), fetchField);
    }

    @Test
    public void testValidatedFields() {
        Optional<EntityField<TestEntity, ?>> validatedField = adapter.validatedFields().findFirst();
        assertTrue(validatedField.isPresent());
        assertEquals("Validated field", validatedField.get(), field);
    }

    @Test
    public void testValidateValue() {
        when(entityChange.isFieldChanged(field)).thenReturn(true);
        when(entityChange.get(field)).thenReturn(STRING_VALUE);
        adapter.validate(entityChange, currentState);
        verify(validator).validate(STRING_VALUE, currentState);
    }
}