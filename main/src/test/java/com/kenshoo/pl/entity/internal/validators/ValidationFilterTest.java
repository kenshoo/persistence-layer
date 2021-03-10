package com.kenshoo.pl.entity.internal.validators;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class ValidationFilterTest {

    public static final String SHOW_STOPPER_VALUE = "show stopper";

    private RecorderValidator changesValidator = new RecorderValidator();

    private RecorderValidator additionalChangesValidator = new RecorderValidator();

    private ChangeContext changeContext;

    private ValidationFilter<TestEntity> validationFilter;

    @Before
    public void setup() {
        changesValidator = new RecorderValidator();
        additionalChangesValidator = new RecorderValidator();
        validationFilter = new ValidationFilter<>(ImmutableList.of(changesValidator, new ShowStopperValidator(), additionalChangesValidator));
        changeContext = new ChangeContextImpl(null, new FeatureSet());
    }

    @Test
    public void required_entity_for_create() {
        Set<? extends EntityField<?, ?>> fields = validationFilter.requiredFields(Collections.emptyList(), ChangeOperation.CREATE).collect(Collectors.toSet());
        assertThat(fields.size(), is(1));
        assertThat(fields, containsInAnyOrder(TestEntity.FIELD_1));
    }

    @Test
    public void required_entity_for_update() {
        Set<? extends EntityField<?, ?>> fields = validationFilter.requiredFields(Collections.emptyList(), ChangeOperation.UPDATE).collect(Collectors.toSet());
        assertThat(fields.size(), is(1));
        assertThat(fields, containsInAnyOrder(TestEntity.FIELD_1));
    }

    @Test
    public void delegate_to_validator_for_support_operation() {
        List<CreateEntityCommand<TestEntity>> commands = Collections.emptyList();
        validationFilter.filter(commands, ChangeOperation.CREATE, changeContext);
        assertThat(changesValidator.getNumberOfCalls(), is(1));
    }

    @Test
    public void dont_delegate_to_validator_for_unsupport_operation() {
        List<CreateEntityCommand<TestEntity>> commands = Collections.emptyList();
        validationFilter.filter(commands, ChangeOperation.DELETE, changeContext);
        assertThat(changesValidator.getNumberOfCalls(), is(0));
    }

    @Test
    public void delegate_to_all_validators_for_support_operation() {
        List<CreateEntityCommand<TestEntity>> commands = Collections.emptyList();
        validationFilter.filter(commands, ChangeOperation.CREATE, changeContext);
        assertThat(changesValidator.getNumberOfCalls(), is(1));
        assertThat(additionalChangesValidator.getNumberOfCalls(), is(1));
    }

    @Test
    public void delegate_to_all_validators_all_records() {
        CreateEntityCommand<TestEntity> cmd1 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        CreateEntityCommand<TestEntity> cmd2 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        List<CreateEntityCommand<TestEntity>> commands = ImmutableList.of(cmd1, cmd2);
        validationFilter.filter(commands, ChangeOperation.CREATE, changeContext);

        assertThat(changesValidator.getNumberOfCalls(), is(1));
        assertThat(changesValidator.recordedEntityChanges, contains(cmd1, cmd2));
        assertThat(additionalChangesValidator.getNumberOfCalls(), is(1));
        assertThat(additionalChangesValidator.recordedEntityChanges, contains(cmd1, cmd2));
    }

    @Test
    public void delegate_to_first_validators_all_records_and_second_only_not_show_stoppers() {
        CreateEntityCommand<TestEntity> cmd1 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd1.set(TestEntity.FIELD_1, SHOW_STOPPER_VALUE);
        CreateEntityCommand<TestEntity> cmd2 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        List<CreateEntityCommand<TestEntity>> commands = ImmutableList.of(cmd1, cmd2);
        validationFilter.filter(commands, ChangeOperation.CREATE, changeContext);

        assertThat(changesValidator.getNumberOfCalls(), is(1));
        assertThat(changesValidator.recordedEntityChanges, contains(cmd1, cmd2));
        assertThat(additionalChangesValidator.getNumberOfCalls(), is(1));
        assertThat(additionalChangesValidator.recordedEntityChanges(), contains(cmd2));
    }

    private class ShowStopperValidator implements ChangesValidator<TestEntity> {
        @Override
        public void validate(Collection<? extends EntityChange<TestEntity>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
            entityChanges.forEach(cmd -> {
                if (cmd.containsField(TestEntity.FIELD_1) && cmd.get(TestEntity.FIELD_1).equals(SHOW_STOPPER_VALUE)) {
                    changeContext.addValidationError(cmd, new ValidationError("show stopper error", TestEntity.FIELD_1, ImmutableMap.of(), true));
                }});
        }
    }

    private class RecorderValidator implements ChangesValidator<TestEntity> {

        private List<EntityChange<TestEntity>> recordedEntityChanges = new ArrayList();
        private int numberOfCalls = 0;

        @Override
        public void validate(Collection<? extends EntityChange<TestEntity>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
            numberOfCalls++;
            entityChanges.forEach(e -> recordedEntityChanges.add(e));
        }

        @Override
        public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<TestEntity, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
            return Stream.of(TestEntity.FIELD_1);
        }

        @Override
        public SupportedChangeOperation getSupportedChangeOperation() {
            return SupportedChangeOperation.CREATE_AND_UPDATE;
        }

        public List<EntityChange<TestEntity>> recordedEntityChanges() {
            return recordedEntityChanges;
        }

        public int getNumberOfCalls() {
            return numberOfCalls;
        }
    }
}