package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PrototypeFieldValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PrototypeFieldValidationAdapterTest  {

    private static final String STRING_VALUE = "value";
    @Mock
    private PrototypeFieldValidator<String> validator;

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

    @InjectMocks
    PrototypeFieldValidationAdapter<TestEntity, String> adapter;

    @Before
    public void setUp() {
        adapter = spy(new PrototypeFieldValidationAdapter<>(TestEntity.FIELD_1, validator));
    }

    @Test
    public void testSupportedOperation() {
        SupportedChangeOperation supportedChangeOperation = adapter.getSupportedChangeOperation();
        assertEquals("Support create or update commands", supportedChangeOperation, SupportedChangeOperation.CREATE_AND_UPDATE);
    }

    @Test
    public void testFetchFields() {
        when(validator.ancestorsFields()).thenReturn(Stream.of(field1, field2));
        Collection<? extends EntityField<?, ?>> fields = adapter.fieldsToFetch().collect(Collectors.toUnmodifiableList());
        assertEquals("Incorrect number of fields to fetch", fields.size(), 2);
        assertTrue("Fetch field1", fields.contains(field1));
        assertTrue("Fetch field2", fields.contains(field2));
    }


    @Test
    public void testTriggeredByFields() {
        assertTrue("Triggered by field",adapter.trigger().triggeredByFields(List.of(TestEntity.FIELD_1)));
    }

    @Test
    public void testValidateValue() {
        when(entityChange.isFieldChanged(TestEntity.FIELD_1)).thenReturn(true);
        when(validator.validateWhen()).thenReturn(value -> true);
        when(entityChange.get(TestEntity.FIELD_1)).thenReturn(STRING_VALUE);
        adapter.validate(entityChange, currentState, finalState);
        verify(validator).validate(STRING_VALUE);
    }

    @Test
    public void testSkipValidateValue() {
        when(entityChange.isFieldChanged(TestEntity.FIELD_1)).thenReturn(true);
        when(validator.validateWhen()).thenReturn(value -> false);
        when(entityChange.get(TestEntity.FIELD_1)).thenReturn(STRING_VALUE);
        adapter.validate(entityChange, currentState, finalState);
        verify(validator, never()).validate(any());
    }
}