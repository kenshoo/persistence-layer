package com.kenshoo.pl.entity.spi.helpers;

import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.TestEntity;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.spi.ParentConditionValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ParentConditionCompositeValidatorTest {

    static final Integer PARENT1 = 1;
    static final Integer PARENT2 = 2;
    static final Long PARENT3 = 3l;

    @Mock
    private EntityField<?, Integer> field1;

    @Mock
    private EntityField<?, Long> field2;

    @Mock
    ParentConditionValidator<Integer> validator1;

    @Mock
    ParentConditionValidator<Long> validator2;

    @Mock
    private ChangeContext changeContext;

    @Mock
    private EntityChange<TestEntity> entityChange1;

    @Mock
    private EntityChange<TestEntity> entityChange2;

    @Mock
    private CurrentEntityState currentEntityState1;

    @Mock
    private CurrentEntityState currentEntityState2;

    private Collection<EntityChange<TestEntity>> entityChanges;

    private Collection<ChangeEntityCommand<TestEntity>> commands = emptyList();

    @InjectMocks
    ParentConditionCompositeValidator<TestEntity> parentConditionCompositeValidator;

    @Before
    public void setUp(){
        //noinspection unchecked
        when(validator1.parentIdField()).thenReturn((EntityField) field1);
        //noinspection unchecked
        when(validator2.parentIdField()).thenReturn((EntityField) field2);
        when(changeContext.getEntity(entityChange1)).thenReturn(currentEntityState1);
        when(changeContext.getEntity(entityChange2)).thenReturn(currentEntityState2);
        entityChanges = ImmutableList.of(entityChange1, entityChange2);
    }


    @Test
    public void testRequiredFieldsForCreate() {
        parentConditionCompositeValidator.register(validator1);
        parentConditionCompositeValidator.register(validator2);
        Collection<EntityField<?, ?>> fields = parentConditionCompositeValidator.requiredFields(emptyList(), CREATE).collect(toSet());
        assertEquals("Required field size", fields.size(), 2);
        assertTrue("Required field1", fields.contains(field1));
        assertTrue("Required field2", fields.contains(field2));
    }

    @Test
    public void testRequiredFieldsForUpdate_new_api() {
        parentConditionCompositeValidator.register(validator1);
        parentConditionCompositeValidator.register(validator2);
        Collection<EntityField<?, ?>> fields = parentConditionCompositeValidator.requiredFields(emptyList(), ChangeOperation.UPDATE).collect(toSet());
        assertEquals("Required field size", fields.size(), 2);
        assertTrue("Required field1", fields.contains(field1));
        assertTrue("Required field2", fields.contains(field2));
    }

    @Test
    public void testRequiredFieldsForCreateAndUpdate_new_api() {
        parentConditionCompositeValidator.register(validator1);
        parentConditionCompositeValidator.register(validator2);
        Collection<EntityField<?, ?>> fields = parentConditionCompositeValidator.requiredFields(emptyList(), CREATE).collect(toSet());
        assertEquals("Required field size", fields.size(), 2);
        assertTrue("Required field1", fields.contains(field1));
        assertTrue("Required field2", fields.contains(field2));
    }

    @Test
    public void testOneValidParentToOneValidator() {
        when(currentEntityState1.get(field1)).thenReturn(PARENT1);
        when(currentEntityState2.get(field1)).thenReturn(PARENT1);
        parentConditionCompositeValidator.register(validator1);
        parentConditionCompositeValidator.validate(entityChanges, null, changeContext);
        verify(validator1, times(1)).validate(PARENT1);
    }

    @Test
    public void testTwoValidParentsToOneValidator() {
        when(currentEntityState1.get(field1)).thenReturn(PARENT1);
        when(currentEntityState2.get(field1)).thenReturn(PARENT2);
        parentConditionCompositeValidator.register(validator1);
        parentConditionCompositeValidator.validate(entityChanges, null, changeContext);
        verify(validator1, times(1)).validate(PARENT1);
        verify(validator1, times(1)).validate(PARENT2);
    }

    @Test
    public void testValidateParentsWithDifferentType() {
        when(currentEntityState1.get(field1)).thenReturn(PARENT1);
        when(currentEntityState1.get(field2)).thenReturn(PARENT3);
        when(currentEntityState2.get(field1)).thenReturn(PARENT1);
        when(currentEntityState2.get(field2)).thenReturn(PARENT3);
        parentConditionCompositeValidator.register(validator1);
        parentConditionCompositeValidator.register(validator2);
        parentConditionCompositeValidator.validate(entityChanges, null, changeContext);
        verify(validator1, times(1)).validate(PARENT1);
        verify(validator2, times(1)).validate(PARENT3);
    }

    @Test
    public void testInvalidParent() {
        when(currentEntityState1.get(field1)).thenReturn(PARENT1);
        when(currentEntityState2.get(field1)).thenReturn(PARENT1);
        ValidationError error = new ValidationError("Invalid parent operation", field1);
        when(validator1.validate(PARENT1)).thenReturn(error);
        parentConditionCompositeValidator.register(validator1);
        parentConditionCompositeValidator.validate(entityChanges, null, changeContext);
        verify(changeContext, times(1)).addValidationError(entityChange1, error);
        verify(changeContext, times(1)).addValidationError(entityChange2, error);
    }
}