package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.TestEntity;
import com.kenshoo.pl.entity.spi.FieldValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Optional;

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

    @InjectMocks
    private FieldValidationAdapter<TestEntity, String> adapter;

    @Before
    public void setUp(){
        when(validator.validatedField()).thenReturn(field);
    }

    @Test
    public void testSupportedOperation() {
        SupportedChangeOperation supportedChangeOperation = adapter.getSupportedChangeOperation();
        assertEquals("Support create or update commands", supportedChangeOperation, SupportedChangeOperation.CREATE_AND_UPDATE);
    }

    @Test
    public void testFetchFieldsInUpdate() {
        Collection<? extends EntityField<?, ?>> fields = adapter.fetchFields().collect(toList());
        assertEquals("Do not fetch field", fields.size(), 0);
    }

    @Test
    public void testFetchFieldsInCreate() {
        Collection<? extends EntityField<?, ?>> fields = adapter.fetchFields().collect(toList());
        assertEquals("Do not fetch field", fields.size(), 0);
    }

    @Test
    public void testValidatedFields() {
        Optional<EntityField<TestEntity, ?>> field = adapter.validatedFields().findFirst();
        assertTrue("Validated field", field.isPresent());
        assertEquals("Validated field", field.get(), this.field);
    }

    @Test
    public void testValidateValue() {
        when(entityChange.isFieldChanged(field)).thenReturn(true);
        when(entityChange.get(field)).thenReturn(STRING_VALUE);
        adapter.validate(entityChange, currentState);
        verify(validator).validate(STRING_VALUE);
    }

    @Test
    public void testNoValueToValidate() {
        when(entityChange.isFieldChanged(field)).thenReturn(false);
        adapter.validate(entityChange, currentState);
        verify(validator, never()).validate(STRING_VALUE);
    }
}