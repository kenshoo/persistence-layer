package com.kenshoo.pl.entity;


import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.BetaTesting;
import com.kenshoo.pl.entity.internal.FalseUpdatesPurger;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static com.kenshoo.pl.BetaTesting.Feature.AutoIncrementSupport;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class ChangeFlowConfigTest {

    private final static Label EXCLUDABLE_LABEL = new Label() {};

    @After
    public void disableBetaFeatures() {
        BetaTesting.disable(AutoIncrementSupport);
    }

    @Test
    public void add_single_enricher_to_flow_config() {
        TestEnricher enricher = new TestEnricher();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                withPostFetchCommandEnricher(enricher).
                build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher));
    }

    @Test
    public void add_enrichers_with_preserved_order_to_flow_config() {
        TestEnricher enricher1 = new TestEnricher();
        TestEnricher enricher2 = new TestEnricher();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withPostFetchCommandEnrichers(ImmutableList.of(enricher1, enricher2)).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher1, enricher2));
    }


    @Test
    public void add_excludable_enricher_to_flow_config() {
        TestEnricher enricher = new TestEnricher();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledPostFetchCommandEnricher(enricher, EXCLUDABLE_LABEL).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher));
    }

    @Test
    public void add_excludable_enrichers_to_flow_config() {
        TestEnricher enricher1 = new TestEnricher();
        TestEnricher enricher2 = new TestEnricher();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledPostFetchCommandEnrichers(ImmutableList.of(enricher1, enricher2), EXCLUDABLE_LABEL).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher1, enricher2));
    }

    @Test
    public void remove_excludable_enricher_flow_flow_config() {
        TestEnricher enricher = new TestEnricher();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledPostFetchCommandEnricher(enricher, EXCLUDABLE_LABEL).
                        withoutPostFetchCommandEnrichers(EXCLUDABLE_LABEL).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of());
    }

    @Test
    public void remove_only_excludable_enricher_flow_flow_config() {
        TestEnricher excludableEnricher = new TestEnricher();
        TestEnricher nonExcludableEnricher = new TestEnricher();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledPostFetchCommandEnricher(excludableEnricher, EXCLUDABLE_LABEL).
                        withPostFetchCommandEnricher(nonExcludableEnricher).
                        withoutPostFetchCommandEnrichers(EXCLUDABLE_LABEL).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(nonExcludableEnricher));
    }

    @Test
    public void add_excludable_validator_to_flow_config() {
        ChangesValidator validator = new TestValidator();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledValidator(validator, EXCLUDABLE_LABEL).
                        build();
        Assert.assertEquals(flow.getValidators(), ImmutableList.of(validator));
    }

    @Test
    public void add_excludable_validators_to_flow_config() {
        ChangesValidator validator1 = new TestValidator();
        ChangesValidator validator2 = new TestValidator();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledValidators(ImmutableList.of(validator1, validator2), EXCLUDABLE_LABEL).
                        build();
        Assert.assertEquals(flow.getValidators(), ImmutableList.of(validator1, validator2));
    }

    @Test
    public void remove_excludable_validator_from_flow_config() {
        ChangesValidator validator = new TestValidator();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledValidator(validator, EXCLUDABLE_LABEL).
                        withoutLabeledValidators(ImmutableList.of(EXCLUDABLE_LABEL)).
                        build();
        Assert.assertEquals(flow.getValidators(), ImmutableList.of());
    }

    @Test
    public void remove_only_excludable_validators_from_flow_config() {
        ChangesValidator excludableValidator1 = new TestValidator();
        ChangesValidator excludableValidator2 = new TestValidator();
        ChangesValidator nonExcludableValidator = new TestValidator();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledValidators(ImmutableList.of(excludableValidator1, excludableValidator2), EXCLUDABLE_LABEL).
                        withValidator(nonExcludableValidator).
                        withoutLabeledValidators(ImmutableList.of(EXCLUDABLE_LABEL)).
                        build();
        Assert.assertEquals(flow.getValidators(), ImmutableList.of(nonExcludableValidator));
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
        TestEnricher enricher = new TestEnricher();
        ChangeFlowConfig.Builder<TestEntity> flowBuilder =
                ChangeFlowConfig.builder(TestEntity.INSTANCE);
        flowBuilder.withFalseUpdatesPurger(purger);
        flowBuilder.withPostFetchCommandEnricher(enricher);
        ChangeFlowConfig<TestEntity> flow = flowBuilder.build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher, purger));
    }

    @Test
    public void get_primary_identity_field_returns_it_when_exists() {

        BetaTesting.enable(AutoIncrementSupport);

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

    private class TestEnricher implements PostFetchCommandEnricher<TestEntity> {

        @Override
        public void enrich(Collection<? extends ChangeEntityCommand<TestEntity>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
        }
        @Override
        public SupportedChangeOperation getSupportedChangeOperation() {
            return null;
        }
        @Override
        public Stream<? extends EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<TestEntity>> changeEntityCommands, ChangeOperation changeOperation) {
            return null;
        }
    }

    private class TestValidator implements ChangesValidator<TestEntity> {
        @Override
        public Stream<? extends EntityField<?, ?>> getRequiredFields(Collection collection, ChangeOperation changeOperation) {
            return null;
        }

        @Override
        public void validate(Collection collection, ChangeOperation changeOperation, ChangeContext changeContext) {

        }
    }
}