package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.ChangeOperation;
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

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
    private Entity entity;

    @Mock
    private RequiredFieldValidator<TestEntity, String> validator;

    @InjectMocks
    private RequiredFieldValidationAdapter underTest;

    @Before
    public void init() {
        when(validator.requiredField()).thenReturn(TestEntity.FIELD_1);
    }

    @Test
    public void when_required_field_is_null_then_return_error_result() {
        when(entityChange.get(any())).thenReturn(null);
        when(validator.getErrorCode()).thenReturn(ERROR_CODE);

        ValidationError result = underTest.validate(entityChange, entity, ChangeOperation.CREATE);

        assertEquals(ERROR_CODE, result.getErrorCode());
        assertEquals(TestEntity.FIELD_1, result.getField());
    }

    @Test
    public void when_required_field_is_not_null_then_return_null() {
        when(entityChange.get(any())).thenReturn(VALUE);

        ValidationError result = underTest.validate(entityChange, entity, ChangeOperation.CREATE);

        assertNull(result);
    }

    @Test
    public void when_call_get_supported_operations_then_return_create() {
        SupportedChangeOperation supportedChangeOperation = underTest.getSupportedChangeOperation();
        assertEquals(supportedChangeOperation, SupportedChangeOperation.CREATE);
    }

    @Test
    public void when_call_validated_fields_then_return_required_field() {
        Optional<? extends EntityField<TestEntity, ?>> field = underTest.getValidatedFields().findFirst();
        assertTrue(field.isPresent());
        assertEquals(field.get(), TestEntity.FIELD_1);
    }

    @Test
    public void when_call_fields_to_fetch_then_get_empty_stream() {
        Stream<? extends EntityField<?, ?>> result = underTest.getFieldsToFetch(ChangeOperation.CREATE);
        assertEquals(Optional.empty(), result.findAny());
    }

}