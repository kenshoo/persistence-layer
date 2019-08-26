package com.kenshoo.pl.entity.spi.helpers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityFieldPrototype;
import com.kenshoo.pl.entity.PrototypeFieldsCombination;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.TestDataFieldPrototype;
import com.kenshoo.pl.entity.TestEntity;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.internal.validators.EntityChangeValidator;
import com.kenshoo.pl.entity.spi.FieldComplexValidator;
import com.kenshoo.pl.entity.spi.FieldValidator;
import com.kenshoo.pl.entity.spi.FieldsCombinationValidator;
import com.kenshoo.pl.entity.spi.ImmutableFieldValidator;
import com.kenshoo.pl.entity.spi.PrototypeFieldComplexValidator;
import com.kenshoo.pl.entity.spi.PrototypeFieldValidator;
import com.kenshoo.pl.entity.spi.PrototypeFieldsCombinationValidator;
import com.kenshoo.pl.entity.spi.RequiredFieldValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EntityChangeCompositeValidatorTest {

    public static final int ENTITY_ID = 10;
    public static final String FIELD_1_VALUE = "value1";

    @Mock
    private ChangeContext changeContext;

    @Mock
    PrototypeFieldValidator<String> prototypePrototypeFieldValidator;

    @Mock
    PrototypeFieldsCombinationValidator prototypeFieldsCombinationValidator;

    @Mock
    FieldValidator<TestEntity, String> fieldValidator;

    @Mock
    FieldComplexValidator<TestEntity, String> fieldComplexValidator;

    @Mock
    ImmutableFieldValidator<TestEntity, String> immutableFieldValidator;

    @Mock
    FieldsCombinationValidator<TestEntity> fieldsCombinationValidator;

    @Mock
    PrototypeFieldComplexValidator<String> prototypeFieldComplexValidator;

    @Mock
    EntityChangeValidator<TestEntity> entityChangeValidator;

    @Mock
    RequiredFieldValidator<TestEntity, String> requiredFieldValidator;

    @Mock
    private EntityChange<TestEntity> entityChange;

    @Mock
    private Entity entity;

    @InjectMocks
    EntityChangeCompositeValidator<TestEntity> validator;

    private Collection<EntityChange<TestEntity>> entityChanges;

    @Before
    public void setUp(){
        when(entityChange.get(TestEntity.FIELD_1)).thenReturn(FIELD_1_VALUE);
        when(changeContext.getEntity(entityChange)).thenReturn(entity);
        when(entityChange.getChangedFields()).thenReturn(Stream.of(TestEntity.FIELD_1));
        when(entityChange.isFieldChanged(TestEntity.FIELD_1)).thenReturn(true);
        entityChanges = ImmutableList.of(entityChange);
    }

    @Test
    public void registerPrototypeCombinationValidatorTest() {
        when(prototypeFieldsCombinationValidator.getPrototypes()).thenReturn(Lists.<EntityFieldPrototype<?>> newArrayList(TestDataFieldPrototype.FIELD_1));
        validator.register(TestEntity.INSTANCE, prototypeFieldsCombinationValidator);
        validator.validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(prototypeFieldsCombinationValidator).validate(any(PrototypeFieldsCombination.class));
    }

    @Test
    public void registerPrototypeValidatorTest() {
        when(prototypePrototypeFieldValidator.getPrototype()).thenReturn(TestDataFieldPrototype.FIELD_1);
        validator.register(TestEntity.INSTANCE, prototypePrototypeFieldValidator);
        validator.validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(prototypePrototypeFieldValidator).validate(FIELD_1_VALUE);
    }

    @Test
    public void registerComplexPrototypeValidatorTest() {
        when(prototypeFieldComplexValidator.getPrototype()).thenReturn(TestDataFieldPrototype.FIELD_1);
        validator.register(TestEntity.INSTANCE, prototypeFieldComplexValidator);
        validator.validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(prototypeFieldComplexValidator).validate(FIELD_1_VALUE, entity);
    }

    @Test
    public void registerFieldValidatorTest() {
        when(fieldValidator.validatedField()).thenReturn(TestEntity.FIELD_1);
        validator.register(fieldValidator);
        validator.validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(fieldValidator).validate(FIELD_1_VALUE);
    }

    @Test
    public void registerFieldComplexValidatorTest() {
        when(fieldComplexValidator.validatedField()).thenReturn(TestEntity.FIELD_1);
        validator.register(fieldComplexValidator);
        validator.validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(fieldComplexValidator).validate(FIELD_1_VALUE, entity);
    }

    @Test
    public void registerImmutableFieldValidatorTest() {
        when(immutableFieldValidator.immutableWhen()).thenReturn(when-> true);
        when(immutableFieldValidator.immutableField()).thenReturn(TestEntity.FIELD_1);
        validator.register(immutableFieldValidator);
        validator.validate(entityChanges, ChangeOperation.UPDATE, changeContext);
        verify(changeContext).addValidationError(eq(entityChange), any(ValidationError.class));
    }

    @Test
    public void registerFieldsCombinationValidatorTest() {
        when(fieldsCombinationValidator.validateWhen()).thenReturn(p->true);
        when(fieldsCombinationValidator.validatedFields()).thenReturn(Stream.of(TestEntity.FIELD_1)).thenReturn(Stream.of(TestEntity.FIELD_1));
        validator.register(fieldsCombinationValidator);
        validator.validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(fieldsCombinationValidator).validate(any());
    }

    @Test
    public void registerEntityChangeValidatorTest() {
        when(entityChangeValidator.getSupportedChangeOperation()).thenReturn(SupportedChangeOperation.CREATE_AND_UPDATE);
        when(entityChangeValidator.getValidatedFields()).thenReturn(Stream.of(TestEntity.FIELD_1));
        validator.register(entityChangeValidator);
        validator.validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(entityChangeValidator).validate(entityChange, entity, ChangeOperation.CREATE);
    }

    @Test
    public void registerRequiredFieldValidatorTest() {
        when(requiredFieldValidator.requiredField()).thenReturn(TestEntity.FIELD_1);
        when(entityChange.get(TestEntity.FIELD_1)).thenReturn(null);
        validator.register(requiredFieldValidator);
        validator.validate(entityChanges, ChangeOperation.CREATE, changeContext);
        verify(changeContext).addValidationError(eq(entityChange), any(ValidationError.class));
    }
}