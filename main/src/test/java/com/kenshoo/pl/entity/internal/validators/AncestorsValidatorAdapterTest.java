package com.kenshoo.pl.entity.internal.validators;


import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.AncestorsValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AncestorsValidatorAdapterTest {

    @Mock
    private EntityField<TestEntity, String> field;

    @Mock
    private EntityChange<TestEntity> entityChange;

    @Mock
    private ValidationError error;

    @Mock
    private CurrentEntityState entity;

    @Mock
    private AncestorsValidator validator;

    @InjectMocks
    private AncestorsValidationAdapter<TestEntity> adapter;

    @Before
    public void setUp(){
        when(validator.ancestorsFields()).thenReturn(Stream.of(field));
    }

    @Test
    public void test_supported_operation() {
        SupportedChangeOperation supportedChangeOperation = adapter.getSupportedChangeOperation();
        assertEquals("Support create or update commands", supportedChangeOperation, SupportedChangeOperation.CREATE_UPDATE_AND_DELETE);
    }

    @Test
    public void test_fetched_fields() {
        Optional<? extends EntityField<?, ?>> field = adapter.fetchFields().findFirst();
        assertTrue("Fetch validated field", field.isPresent());
        assertEquals("Fetch validated field", field.get(), this.field);
    }

    @Test
    public void test_flow_with_error() {
        Predicate<CurrentEntityState> predicate = entity -> true;
        when(validator.ancestorsRestriction()).thenReturn(predicate);
        when(validator.errorFor(entity)).thenReturn(error);
        ValidationError error = adapter.validate(entityChange, entity);
        assertEquals("Error for mutable condition", this.error, error);
    }

    @Test
    public void test_flow_without_error() {
        Predicate<CurrentEntityState> predicate = entity -> false;
        when(validator.ancestorsRestriction()).thenReturn(predicate);
        ValidationError error = adapter.validate(entityChange, entity);
        assertNull("No error for mutable condition", error);
    }
}

