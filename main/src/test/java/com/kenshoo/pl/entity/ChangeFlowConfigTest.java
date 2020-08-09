package com.kenshoo.pl.entity;


import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.internal.FalseUpdatesPurger;
import com.kenshoo.pl.entity.internal.audit.AuditRequiredFieldsCalculator;
import com.kenshoo.pl.entity.internal.audit.AuditedFieldSet;
import com.kenshoo.pl.entity.internal.audit.AuditedFieldsResolver;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_CREATE_OR_UPDATE;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class ChangeFlowConfigTest {

    private final static Label EXCLUDABLE_LABEL_1 = new Label() {};
    private final static Label EXCLUDABLE_LABEL_2 = new Label() {};

    @Mock
    private AuditedFieldsResolver auditedFieldsResolver;

    @Test
    public void add_single_enricher_to_flow_config() {
        PostFetchCommandEnricher<TestEntity> enricher = mock(PostFetchCommandEnricher.class);
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                withPostFetchCommandEnricher(enricher).
                build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher));
    }

    @Test
    public void add_enrichers_with_preserved_order_to_flow_config() {
        PostFetchCommandEnricher<TestEntity> enricher1 = mock(PostFetchCommandEnricher.class);
        PostFetchCommandEnricher<TestEntity> enricher2 = mock(PostFetchCommandEnricher.class);
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withPostFetchCommandEnrichers(ImmutableList.of(enricher1, enricher2)).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher1, enricher2));
    }


    @Test
    public void add_excludable_enricher_to_flow_config() {
        PostFetchCommandEnricher<TestEntity> enricher = mock(PostFetchCommandEnricher.class);
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledPostFetchCommandEnricher(enricher, EXCLUDABLE_LABEL_1).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher));
    }

    @Test
    public void add_excludable_enrichers_to_flow_config() {
        PostFetchCommandEnricher<TestEntity> enricher1 = mock(PostFetchCommandEnricher.class);
        PostFetchCommandEnricher<TestEntity> enricher2 = mock(PostFetchCommandEnricher.class);
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledPostFetchCommandEnrichers(ImmutableList.of(enricher1, enricher2), EXCLUDABLE_LABEL_1).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher1, enricher2));
    }

    @Test
    public void add_excludable_validator_to_flow_config() {
        ChangesValidator validator = mock(ChangesValidator.class);
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledValidator(validator, EXCLUDABLE_LABEL_1).
                        build();
        Assert.assertEquals(flow.getValidators(), ImmutableList.of(validator));
    }

    @Test
    public void add_excludable_validators_to_flow_config() {
        ChangesValidator validator1 = mock(ChangesValidator.class);
        ChangesValidator validator2 = mock(ChangesValidator.class);
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledValidators(ImmutableList.of(validator1, validator2), EXCLUDABLE_LABEL_1).
                        build();
        Assert.assertEquals(flow.getValidators(), ImmutableList.of(validator1, validator2));
    }

    @Test
    public void remove_only_excludable_elements_from_flow_config() {
        ChangesValidator excludableValidator= mock(ChangesValidator.class);
        PostFetchCommandEnricher<TestEntity> excludableEnricher = mock(PostFetchCommandEnricher.class);
        ChangesValidator nonExcludableValidator = mock(ChangesValidator.class);
        PostFetchCommandEnricher<TestEntity> nonExcludableEnricher = mock(PostFetchCommandEnricher.class);
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledValidator(excludableValidator, EXCLUDABLE_LABEL_1).
                        withLabeledPostFetchCommandEnricher(excludableEnricher, EXCLUDABLE_LABEL_1).
                        withValidator(nonExcludableValidator).
                        withPostFetchCommandEnricher(nonExcludableEnricher).
                        withoutLabeledElements(EXCLUDABLE_LABEL_1).
                        build();
        Assert.assertEquals(flow.getValidators(), ImmutableList.of(nonExcludableValidator));
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(nonExcludableEnricher));
    }

    @Test
    public void remove_excludable_elements_for_multi_labels_from_flow_config() {
        ChangesValidator excludableValidator= mock(ChangesValidator.class);
        PostFetchCommandEnricher<TestEntity> excludableEnricher = mock(PostFetchCommandEnricher.class);
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledValidator(excludableValidator, EXCLUDABLE_LABEL_1).
                        withLabeledPostFetchCommandEnricher(excludableEnricher, EXCLUDABLE_LABEL_2).
                        withoutLabeledElements(ImmutableList.of(EXCLUDABLE_LABEL_1, EXCLUDABLE_LABEL_2)).
                        build();
        Assert.assertTrue(flow.getValidators().isEmpty());
        Assert.assertTrue(flow.getPostFetchCommandEnrichers().isEmpty());
    }


    @Test
    public void add_false_update_purger_to_flow_config() {
        FalseUpdatesPurger<TestEntity> purger = new FalseUpdatesPurger.Builder<TestEntity>().build();
        ChangeFlowConfig.Builder<TestEntity> flowBuilder =
                ChangeFlowConfig.builder(TestEntity.INSTANCE);
        flowBuilder.withFalseUpdatesPurger(purger);
        ChangeFlowConfig<TestEntity> flow = flowBuilder.build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(purger));
    }

    @Test
    public void add_false_update_purger__last_to_flow_config() {
        FalseUpdatesPurger<TestEntity> purger = new FalseUpdatesPurger.Builder<TestEntity>().build();
        PostFetchCommandEnricher<TestEntity> enricher = mock(PostFetchCommandEnricher.class);
        ChangeFlowConfig.Builder<TestEntity> flowBuilder =
                ChangeFlowConfig.builder(TestEntity.INSTANCE);
        flowBuilder.withFalseUpdatesPurger(purger);
        flowBuilder.withPostFetchCommandEnricher(enricher);
        ChangeFlowConfig<TestEntity> flow = flowBuilder.build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher, purger));
    }

    @Test
    public void get_primary_identity_field_returns_it_when_exists() {

        final ChangeFlowConfig.Builder<TestEntityAutoInc> flowBuilder =
            ChangeFlowConfig.builder(TestEntityAutoInc.INSTANCE);

        final ChangeFlowConfig<TestEntityAutoInc> flow = flowBuilder.build();

        assertThat(flow.getPrimaryIdentityField(), equalTo(Optional.of(TestEntityAutoInc.ID)));
    }

    @Test
    public void get_primary_identity_field_returns_empty_when_doesnt_exist() {
        final ChangeFlowConfig.Builder<TestEntity> flowBuilder =
            ChangeFlowConfig.builder(TestEntity.INSTANCE);

        final ChangeFlowConfig<TestEntity> flow = flowBuilder.build();

        assertThat(flow.getPrimaryIdentityField(), equalTo(Optional.empty()));
    }

    @Test
    public void audit_required_fields_calculator_should_be_in_state_consumers_if_audited_fields_defined() {

        final AuditedFieldSet<TestEntity> auditedFieldSet = AuditedFieldSet.builder(TestEntity.ID)
                                                                           .withInternalFields(ON_CREATE_OR_UPDATE,
                                                                                               TestEntity.FIELD_1, TestEntity.FIELD_2)
                                                                           .build();
        doReturn(Optional.of(auditedFieldSet)).when(auditedFieldsResolver).resolve(TestEntity.INSTANCE);

        final ChangeFlowConfig<TestEntity> flowConfig = new ChangeFlowConfig.Builder<>(TestEntity.INSTANCE,
                                                                                       auditedFieldsResolver).build();

        final Set<Class<?>> currentStateConsumerTypes = flowConfig.currentStateConsumers()
                                                                  .map(Object::getClass)
                                                                  .collect(Collectors.toSet());

        assertThat("AuditRequiredFieldsCalculator should be included in state consumers",
                   currentStateConsumerTypes,
                   hasItem(AuditRequiredFieldsCalculator.class));
    }

    @Test
    public void audit_required_fields_calculator_should_not_be_in_state_consumers_if_no_audited_fields_defined() {

        when(auditedFieldsResolver.resolve(TestEntity.INSTANCE)).thenReturn(Optional.empty());

        final ChangeFlowConfig<TestEntity> flowConfig = new ChangeFlowConfig.Builder<>(TestEntity.INSTANCE,
                                                                                       auditedFieldsResolver).build();

        final Set<Class<?>> currentStateConsumerTypes = flowConfig.currentStateConsumers()
                                                                  .map(Object::getClass)
                                                                  .collect(Collectors.toSet());

        assertThat("AuditRequiredFieldsCalculator should NOT be included in state consumers",
                   currentStateConsumerTypes,
                   not(hasItem(AuditRequiredFieldsCalculator.class)));
    }

    @Test
    public void should_create_audit_record_generator_if_audited_fields_defined() {

        final AuditedFieldSet<TestEntity> auditedFieldSet = AuditedFieldSet.builder(TestEntity.ID)
                                                                           .withInternalFields(ON_CREATE_OR_UPDATE,
                                                                                               TestEntity.FIELD_1, TestEntity.FIELD_2)
                                                                           .build();
        doReturn(Optional.of(auditedFieldSet)).when(auditedFieldsResolver).resolve(TestEntity.INSTANCE);

        final ChangeFlowConfig<TestEntity> flowConfig = new ChangeFlowConfig.Builder<>(TestEntity.INSTANCE,
                                                                                       auditedFieldsResolver).build();
        assertThat("Audit record generator should exist",
                   flowConfig.auditRecordGenerator().isPresent(), is(true));
    }

    @Test
    public void should_not_create_audit_record_generator_if_no_audited_fields_defined() {

        doReturn(Optional.empty()).when(auditedFieldsResolver).resolve(TestEntity.INSTANCE);

        final ChangeFlowConfig<TestEntity> flowConfig = new ChangeFlowConfig.Builder<>(TestEntity.INSTANCE,
                                                                                       auditedFieldsResolver).build();
        assertThat("Audit record generator should not exist",
                   flowConfig.auditRecordGenerator().isPresent(), is(false));
    }
}