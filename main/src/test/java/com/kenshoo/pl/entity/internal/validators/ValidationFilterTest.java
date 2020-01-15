package com.kenshoo.pl.entity.internal.validators;

import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class ValidationFilterTest {

    @Mock
    private ChangesValidator<TestEntity> changesValidator;

    @Mock
    private ChangeContext changeContext;

    private ValidationFilter<TestEntity> validationFilter;

    @Before
    public void setup() {
        when(changesValidator.getSupportedChangeOperation()).thenReturn(SupportedChangeOperation.CREATE_UPDATE_AND_DELETE);
        validationFilter = new ValidationFilter<>(ImmutableList.of(changesValidator));
    }

    @Test
    public void required_entity_for_create_new_api() {
        doReturn(Stream.of(TestEntity.FIELD_1)).when(changesValidator).requiredFields(Collections.emptyList(), ChangeOperation.CREATE);
        List<? extends EntityField<?, ?>> fields = validationFilter.requiredFields(Collections.emptyList(), ChangeOperation.CREATE).collect(Collectors.toList());
        assertThat(fields.size(), is(1));
        assertThat(fields, containsInAnyOrder(TestEntity.FIELD_1));
    }

    @Test
    public void required_entity_for_create_old_api() {
        doReturn(Stream.of(TestEntity.FIELD_1)).when(changesValidator).getRequiredFields(Collections.emptyList(), ChangeOperation.CREATE);
        List<? extends EntityField<?, ?>> fields = validationFilter.getRequiredFields(Collections.emptyList(), ChangeOperation.CREATE).collect(Collectors.toList());
        assertThat(fields.size(), is(1));
        assertThat(fields, containsInAnyOrder(TestEntity.FIELD_1));
    }

    @Test
    public void required_entity_for_update_new_api() {
        doReturn(Stream.of(TestEntity.FIELD_1)).when(changesValidator).requiredFields(Collections.emptyList(), ChangeOperation.UPDATE);
        List<? extends EntityField<?, ?>> fields = validationFilter.requiredFields(Collections.emptyList(), ChangeOperation.UPDATE).collect(Collectors.toList());
        assertThat(fields.size(), is(1));
        assertThat(fields, containsInAnyOrder(TestEntity.FIELD_1));
    }

    @Test
    public void required_entity_for_update_old_api() {
        doReturn(Stream.of(TestEntity.FIELD_1)).when(changesValidator).getRequiredFields(Collections.emptyList(), ChangeOperation.UPDATE);
        List<? extends EntityField<?, ?>> fields = validationFilter.getRequiredFields(Collections.emptyList(), ChangeOperation.UPDATE).collect(Collectors.toList());
        assertThat(fields.size(), is(1));
        assertThat(fields, containsInAnyOrder(TestEntity.FIELD_1));
    }

    @Test
    public void delegate_to_validator_for_support_operation() {
        List<CreateEntityCommand<TestEntity>> commands = Collections.emptyList();
        validationFilter.filter(commands, ChangeOperation.CREATE, changeContext);
        verify(changesValidator, times(1)).validate(commands, ChangeOperation.CREATE, changeContext);
    }

    @Test
    public void dont_delegate_to_validator_for_unsupport_operation() {
        when(changesValidator.getSupportedChangeOperation()).thenReturn(SupportedChangeOperation.UPDATE);
        List<CreateEntityCommand<TestEntity>> commands = Collections.emptyList();
        validationFilter.filter(commands, ChangeOperation.CREATE, changeContext);
        verify(changesValidator, never()).validate(commands, ChangeOperation.CREATE, changeContext);
    }
}