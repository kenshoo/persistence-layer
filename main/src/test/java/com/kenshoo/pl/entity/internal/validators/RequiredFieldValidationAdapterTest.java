package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.TestEntity;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.spi.RequiredFieldValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by talyak on 5/22/2018.
 */

@RunWith(MockitoJUnitRunner.class)
public class RequiredFieldValidationAdapterTest {

    private final static String ERROR_CODE = "error";
    private final static String VALUE = "value";

    @Mock
    private EntityChange<TestEntity> entityChange;

    @Mock
    private Entity currentState;

    @Mock
    private EntityField<TestEntity, String> fetchField;

    @Mock
    private RequiredFieldValidator<TestEntity, String> validator;

    @InjectMocks
    private RequiredFieldValidationAdapter underTest;

    @Before
    public void init() {
        when(validator.requiredField()).thenReturn(TestEntity.FIELD_1);
        when(validator.requireWhen()).thenReturn(currentState -> true);
    }

    @Test
    public void when_required_field_is_null_then_return_error_result() {
        when(entityChange.get(any())).thenReturn(null);
        when(validator.getErrorCode()).thenReturn(ERROR_CODE);

        ValidationError result = underTest.validate(entityChange, currentState);

        assertEquals(ERROR_CODE, result.getErrorCode());
        assertEquals(TestEntity.FIELD_1, result.getField());
    }

    @Test
    public void when_required_field_is_not_null_then_return_null() {
        when(entityChange.get(any())).thenReturn(VALUE);

        ValidationError result = underTest.validate(entityChange, currentState);

        assertNull(result);
    }

    @Test
    public void when_call_get_supported_operations_then_return_create() {
        SupportedChangeOperation supportedChangeOperation = underTest.getSupportedChangeOperation();
        assertEquals(supportedChangeOperation, SupportedChangeOperation.CREATE);
    }

    @Test
    public void when_call_validated_fields_then_return_required_field() {
        Optional<? extends EntityField<TestEntity, ?>> field = underTest.validatedFields().findFirst();
        assertTrue(field.isPresent());
        assertEquals(field.get(), TestEntity.FIELD_1);
    }

    @Test
    public void when_call_fields_to_fetch_then_empty() {
        Stream<? extends EntityField<?, ?>> result = underTest.fetchFields();
        assertEquals(Optional.empty(), result.findAny());
    }

    @Test
    public void when_require_value_and_predicate_false_return_null() {
        when(entityChange.get(any())).thenReturn(null);
        when(validator.requireWhen()).thenReturn(currentState -> false);

        assertNull(underTest.validate(entityChange, currentState));
    }

    @Test
    public void when_call_fields_to_fetch_then_fetched_field_only() {
        when(validator.fetchFields()).thenReturn(Stream.of(fetchField));
        Stream<? extends EntityField<?, ?>> fetchedStream = underTest.fetchFields();
        List<? extends EntityField<?, ?>> fieldsToFetch = fetchedStream.collect(Collectors.toList());

        assertFalse("Fetch validated field", fieldsToFetch.contains(TestEntity.FIELD_1));
        assertTrue("Fetch validated field", fieldsToFetch.contains(fetchField));
    }

}