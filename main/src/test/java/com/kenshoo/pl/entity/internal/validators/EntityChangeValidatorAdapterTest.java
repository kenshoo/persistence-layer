package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class EntityChangeValidatorAdapterTest {

    @Mock
    private EntityChangeValidator<TestEntity> validator;

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

    private EntityChangeValidatorAdapter<TestEntity> adapter;

    @Before
    public void setUp() {
        when(validator.getSupportedChangeOperation()).thenReturn(SupportedChangeOperation.CREATE_AND_UPDATE);
        when(validator.validatedFields()).thenReturn(Stream.of(field));
        Mockito.doReturn(Stream.of(fetchField)).when(validator).fetchFields();
        adapter = new EntityChangeValidatorAdapter<>(validator);
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
        adapter.validate(entityChange, currentState, finalState);
        verify(validator).validate(entityChange, currentState);
    }

}