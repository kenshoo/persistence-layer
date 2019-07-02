package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.TestEntity;
import com.kenshoo.pl.entity.spi.PrototypeFieldComplexValidator;
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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PrototypeFieldComplexValidationAdapterTest {

    final static String STRING_VALUE = "value";

    @Mock
    private EntityField<?, ?> fetchField;

    @Mock
    private ChangeEntityCommand<TestEntity> command;

    @Mock
    private EntityChange<TestEntity> entityChange;

    @Mock
    private Entity entity;

    @Mock
    private PrototypeFieldComplexValidator<String> validator;

    @InjectMocks
    private PrototypeFieldComplexValidationAdapter<TestEntity, String> adapter;

    @Before
    public void setUp() {
        adapter = spy(new PrototypeFieldComplexValidationAdapter<>(TestEntity.FIELD_1, validator));
        Stream<EntityField<?, ?>> entityFields = Stream.of(fetchField);
        when(validator.fetchFields()).thenReturn(entityFields);

    }

    @Test
    public void testSupportedOperation() {
        SupportedChangeOperation supportedChangeOperation = adapter.getSupportedChangeOperation();
        assertEquals("Support create or update commands", supportedChangeOperation, SupportedChangeOperation.CREATE_AND_UPDATE);
    }

    @Test
    public void testFetchFieldsInCreate() {
        Optional<? extends EntityField<?, ?>> fetchField = adapter.getFieldsToFetch(ChangeOperation.CREATE).findFirst();
        assertTrue(fetchField.isPresent());
        assertEquals("Fetch field", fetchField.get(), this.fetchField);
    }

    @Test
    public void testValidatedFields() {
        Optional<? extends EntityField<?, ?>> validatedField = adapter.getValidatedFields().findFirst();
        assertTrue(validatedField.isPresent());
        assertEquals("Validated field", validatedField.get(), TestEntity.FIELD_1);
    }

    @Test
    public void testValidateValue() {
        when(entityChange.isFieldChanged(TestEntity.FIELD_1)).thenReturn(true);
        when(entityChange.get(TestEntity.FIELD_1)).thenReturn(STRING_VALUE);
        adapter.validate(entityChange, entity, ChangeOperation.CREATE);
        verify(validator).validate(STRING_VALUE, entity);
    }
}