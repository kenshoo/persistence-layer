package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.ImmutableFieldValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class ImmutableFieldValidationAdapterTest {

    final static String ERROR_CODE = "error";

    @Mock
    private ImmutableFieldValidator<TestEntity, String> validator;

    @Mock
    private ChangeEntityCommand<TestEntity> command;

    @Mock
    private EntityField<TestEntity, String> field;

    @Mock
    private EntityField<TestEntity, String> fetchField;

    @Mock
    private EntityChange<TestEntity> entityChange;

    @Mock
    private CurrentEntityState currentState;

    @Mock
    private FinalEntityState finalState;

    private ImmutableFieldValidationAdapter<TestEntity, String> adapter;

    @Before
    public void setUp(){
        when(validator.immutableField()).thenReturn(field);
        when(validator.getErrorCode()).thenReturn(ERROR_CODE);
        when(validator.immutableWhen()).thenReturn(currentState -> true);
        adapter = new ImmutableFieldValidationAdapter<>(validator);
    }

    @Test
    public void testSupportedOperation() {
        SupportedChangeOperation supportedChangeOperation = adapter.getSupportedChangeOperation();
        assertEquals("Support create or update commands", supportedChangeOperation, SupportedChangeOperation.UPDATE);
    }

    @Test
    public void testFetchFieldsInUpdate() {
        Optional<? extends EntityField<?, ?>> field = adapter.fieldsToFetch().findFirst();
        assertTrue("Fetch validated field", field.isPresent());
        assertEquals("Fetch validated field", field.get(), this.field);
    }

    @Test
    public void testFetchFieldsInUpdateWithWhenPredicate() {
        when(validator.fetchFields()).thenReturn(Stream.of(fetchField));
        List<EntityField<?, ?>> fieldsToFetch = adapter.fieldsToFetch().collect(Collectors.toList());
        assertTrue("Fetch validated field", fieldsToFetch.contains(field));
        assertTrue("Fetch validated field", fieldsToFetch.contains(fetchField));
    }

    @Test
    public void testTriggeredByFields() {
        assertTrue("Triggered by field",  adapter.trigger().triggeredByFields(List.of(this.field)));
    }

    @Test
    public void testValidateValueChange() {
        when(entityChange.isFieldChanged(field)).thenReturn(true);
        ValidationError validationError = adapter.validate(entityChange, currentState, finalState);
        assertNotNull("No validation error", validationError);
        assertEquals("Error code", validationError.getErrorCode(), ERROR_CODE);
    }

    @Test
    public void testValidateValueChangeWhenPredicateFalse() {
        when(entityChange.isFieldChanged(field)).thenReturn(true);
        when(validator.immutableWhen()).thenReturn(currentState -> false);
        assertNull(adapter.validate(entityChange, currentState, finalState));
    }
}