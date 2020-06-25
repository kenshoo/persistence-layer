package com.kenshoo.pl.entity.internal.validators;

import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PrototypeFieldsCombinationValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PrototypeFieldsCombinationValidationAdapterTest {

    final static String STRING_VALUE1 = "value1";
    final static String STRING_VALUE2 = "value2";

    @Mock
    private PrototypeFieldsCombinationValidator validator;

    @Mock
    private EntityFieldPrototype<String> invalidField;

    @Mock
    private EntityChange<TestEntity> entityChange;

    @Mock
    private Entity entity;

    @InjectMocks
    private PrototypeFieldsCombinationValidationAdapter<TestEntity> adapter;

    @Before
    public void setUp(){
        adapter = spy(new PrototypeFieldsCombinationValidationAdapter<>(validator, ImmutableMap.<EntityFieldPrototype<?>, EntityField<TestEntity, ?>>of(TestDataFieldPrototype.FIELD_1, TestEntity.FIELD_1, TestDataFieldPrototype.FIELD_2, TestEntity.FIELD_2)));

        when(entityChange.getChangeOperation()).thenReturn(ChangeOperation.UPDATE);
        when(entityChange.isFieldChanged(TestEntity.FIELD_1)).thenReturn(true);
        when(entityChange.isFieldChanged(TestEntity.FIELD_2)).thenReturn(false);
        when(entityChange.get(TestEntity.FIELD_1)).thenReturn(STRING_VALUE1);
        when(entity.get(TestEntity.FIELD_2)).thenReturn(STRING_VALUE2);
    }

    @Test
    public void testSupportedOperation() {
        SupportedChangeOperation supportedChangeOperation = adapter.getSupportedChangeOperation();
        assertEquals("Support create or update commands", supportedChangeOperation, SupportedChangeOperation.CREATE_AND_UPDATE);
    }

    @Test
    public void testFetchFieldsInUpdate() {
        Collection<? extends EntityField<?, ?>> fields = adapter.fetchFields().collect(toSet());
        assertTrue("Fetch field1", fields.contains(TestEntity.FIELD_1));
        assertTrue("Fetch field2", fields.contains(TestEntity.FIELD_2));
    }

    @Test
    public void testFetchFieldsInCreate() {
        Collection<? extends EntityField<?, ?>> fields = adapter.fetchFields().collect(toSet());
        assertTrue("Fetch field1", fields.contains(TestEntity.FIELD_1));
        assertTrue("Fetch field2", fields.contains(TestEntity.FIELD_2));
    }

    @Test
    public void testValidateForCreate() {
        when(entityChange.getChangeOperation()).thenReturn(ChangeOperation.CREATE);
        adapter.validate(entityChange, entity);
        verify(validator).validate(argThat(fieldCombination -> {
            assertEquals("Field1", fieldCombination.get(TestDataFieldPrototype.FIELD_1), STRING_VALUE1);
            assertEquals("Field2", fieldCombination.get(TestDataFieldPrototype.FIELD_2), null);
            return true;
        }));
    }

    @Test
    public void testValidateForUpdate() {
        adapter.validate(entityChange, entity);
        verify(validator).validate(argThat(fieldCombination -> {
            assertEquals("Field1", fieldCombination.get(TestDataFieldPrototype.FIELD_1), STRING_VALUE1);
            assertEquals("Field2", fieldCombination.get(TestDataFieldPrototype.FIELD_2), STRING_VALUE2);
            return true;
        }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidField() {
        adapter.validate(entityChange, entity);
        verify(validator).validate(argThat(fieldCombination -> {
            fieldCombination.get(invalidField);
            return true;
        }));
    }

}