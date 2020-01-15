package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CompoundChangesValidatorTest {

    @Mock
    private EntityField<TestEntity,String> field1;

    @Mock
    private EntityField<TestEntity,String> field2;

    @Mock
    private ChangesValidator<TestEntity> validator1;

    @Mock
    private ChangesValidator<TestEntity> validator2;

    @Mock
    private ChangeContext changeContext;

    @Mock
    private EntityChange<TestEntity> entityChange;

    private Collection<ChangeEntityCommand<TestEntity>> commands = Collections.emptyList();

    private Collection<EntityChange<TestEntity>> entityChanges  = Collections.singletonList(entityChange);

    @InjectMocks
    private CompoundChangesValidator<TestEntity> compoundChangesValidator;

    @Before
    public void setUp(){
        when(validator1.getSupportedChangeOperation()).thenReturn(SupportedChangeOperation.CREATE_UPDATE_AND_DELETE);
        when(validator2.getSupportedChangeOperation()).thenReturn(SupportedChangeOperation.CREATE_UPDATE_AND_DELETE);
        when(validator1.getRequiredFields(commands, ChangeOperation.CREATE)).thenAnswer(invocationOnMock -> Stream.of(field1));
        when(validator2.getRequiredFields(commands, ChangeOperation.CREATE)).thenAnswer(invocationOnMock -> Stream.of(field2));

    }

    @Test
    public void testRequiredFieldsForOneValidator() {
        compoundChangesValidator.register(validator1);
        Optional<EntityField<?, ?>> requiredField = compoundChangesValidator.getRequiredFields(commands, ChangeOperation.CREATE).findFirst();
        assertTrue(requiredField.isPresent());
        assertEquals("Required field", requiredField.get(), field1);
    }

    @Test
    public void testRequiredFieldsForOneValidatorNotFitOperation() {
        when(validator1.getSupportedChangeOperation()).thenReturn(SupportedChangeOperation.UPDATE);
        compoundChangesValidator.register(validator1);
        Stream<EntityField<?, ?>> requiredFields = compoundChangesValidator.getRequiredFields(commands, ChangeOperation.CREATE);
        assertEquals(0, requiredFields.count());
    }

    @Test
    public void testRequiredFieldsForMultipleValidators() {
        compoundChangesValidator.register(validator1);
        compoundChangesValidator.register(validator2);
        Collection<EntityField<?, ?>> fields = compoundChangesValidator.getRequiredFields(commands, ChangeOperation.CREATE).collect(toSet());
        assertTrue("Required field1", fields.contains(field1));
        assertTrue("Required field2", fields.contains(field2));
    }

    @Test
    public void testRequiredFieldsForMultipleValidatorsOnlyOneFitOperation() {
        when(validator1.getSupportedChangeOperation()).thenReturn(SupportedChangeOperation.UPDATE);
        when(validator2.getSupportedChangeOperation()).thenReturn(SupportedChangeOperation.CREATE);
        compoundChangesValidator.register(validator1);
        compoundChangesValidator.register(validator2);
        Collection<EntityField<?, ?>> fields = compoundChangesValidator.getRequiredFields(commands, ChangeOperation.CREATE).collect(toSet());
        assertFalse("Required field1", fields.contains(field1));
        assertTrue("Required field2", fields.contains(field2));
    }

    @Test
    public void testDelegateToOneValidator() {
        compoundChangesValidator.register(validator1);
        compoundChangesValidator.validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(validator1, times(1)).validate(entityChanges, ChangeOperation.CREATE, changeContext);
    }

    @Test
    public void testDelegateToMultipleValidators() {
        compoundChangesValidator.register(validator1);
        compoundChangesValidator.register(validator2);
        compoundChangesValidator.validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(validator1, times(1)).validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(validator2, times(1)).validate(entityChanges, ChangeOperation.CREATE, changeContext);
    }

    @Test
    public void testDelegateToMultipleValidatorsForSupportOperation() {
        when(validator2.getSupportedChangeOperation()).thenReturn(SupportedChangeOperation.CREATE);
        when(validator2.getSupportedChangeOperation()).thenReturn(SupportedChangeOperation.UPDATE);
        compoundChangesValidator.register(validator1);
        compoundChangesValidator.register(validator2);
        compoundChangesValidator.validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(validator1, times(1)).validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(validator2, never()).validate(entityChanges, ChangeOperation.CREATE, changeContext);
    }

}