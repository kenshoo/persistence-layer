package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.TestEntity;
import com.kenshoo.pl.entity.spi.PrototypeFieldValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PrototypeFieldValidationAdapterTest  {

    private static final String STRING_VALUE = "value";
    @Mock
    private PrototypeFieldValidator<String> validator;

    @Mock
    private ChangeEntityCommand<TestEntity> command;

    @Mock
    private EntityChange<TestEntity> entityChange;

    @Mock
    private Entity entity;

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
    public void testFetchFieldsInUpdate() {
        Stream<? extends EntityField<?, ?>> fields = adapter.fetchFields();
        assertFalse("Do not fetch field", fields.findFirst().isPresent());
    }

    @Test
    public void testFetchFieldsInCreate() {
        Stream<? extends EntityField<?, ?>> fields = adapter.fetchFields();
        assertFalse("Do not fetch field", fields.findFirst().isPresent());
    }

    @Test
    public void testValidatedFields() {
        Optional<EntityField<TestEntity, ?>> field = adapter.validatedFields().findFirst();
        assertTrue("Validated field", field.isPresent());
        assertEquals("Validated field", field.get(), TestEntity.FIELD_1);
    }

    @Test
    public void testValidateValue() {
        when(entityChange.isFieldChanged(TestEntity.FIELD_1)).thenReturn(true);
        when(entityChange.get(TestEntity.FIELD_1)).thenReturn(STRING_VALUE);
        adapter.validate(entityChange, entity);
        verify(validator).validate(STRING_VALUE);
    }
}