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

    private final static Label EXCLUDABLE_LABEL_1 = new Label() {};
    private final static Label EXCLUDABLE_LABEL_2 = new Label() {};

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
                        withLabeledPostFetchCommandEnricher(enricher, EXCLUDABLE_LABEL_1).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher));
    }

    @Test
    public void add_excludable_enrichers_to_flow_config() {
        TestEnricher enricher1 = new TestEnricher();
        TestEnricher enricher2 = new TestEnricher();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledPostFetchCommandEnrichers(ImmutableList.of(enricher1, enricher2), EXCLUDABLE_LABEL_1).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher1, enricher2));
    }

    @Test
    public void add_excludable_validator_to_flow_config() {
        ChangesValidator validator = new TestValidator();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledValidator(validator, EXCLUDABLE_LABEL_1).
                        build();
        Assert.assertEquals(flow.getValidators(), ImmutableList.of(validator));
    }

    @Test
    public void add_excludable_validators_to_flow_config() {
        ChangesValidator validator1 = new TestValidator();
        ChangesValidator validator2 = new TestValidator();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledValidators(ImmutableList.of(validator1, validator2), EXCLUDABLE_LABEL_1).
                        build();
        Assert.assertEquals(flow.getValidators(), ImmutableList.of(validator1, validator2));
    }

    @Test
    public void remove_only_excludable_elements_from_flow_config() {
        ChangesValidator excludableValidator= new TestValidator();
        TestEnricher excludableEnricher = new TestEnricher();
        ChangesValidator nonExcludableValidator = new TestValidator();
        TestEnricher nonExcludableEnricher = new TestEnricher();
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
        ChangesValidator excludableValidator= new TestValidator();
        TestEnricher excludableEnricher = new TestEnricher();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledValidator(excludableValidator, EXCLUDABLE_LABEL_1).
                        withLabeledPostFetchCommandEnricher(excludableEnricher, EXCLUDABLE_LABEL_2).
                        withoutLabeledElements(ImmutableList.of(EXCLUDABLE_LABEL_1, EXCLUDABLE_LABEL_2)).
                        build();
        Assert.assertTrue(flow.getValidators().isEmpty());
        Assert.assertTrue(flow.getValidators().isEmpty());
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