package com.kenshoo.pl.entity;


import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.internal.FalseUpdatesPurger;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.Collection;
import java.util.stream.Stream;


@RunWith(MockitoJUnitRunner.class)
public class ChangeFlowConfigTest {

    private final static Label EXCLUDABLE_ENRICHERS = new Label() {};

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
                        withLabeledPostFetchCommandEnricher(enricher, EXCLUDABLE_ENRICHERS).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher));
    }

    @Test
    public void add_excludable_enrichers_to_flow_config() {
        TestEnricher enricher1 = new TestEnricher();
        TestEnricher enricher2 = new TestEnricher();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledPostFetchCommandEnrichers(ImmutableList.of(enricher1, enricher2), EXCLUDABLE_ENRICHERS).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(enricher1, enricher2));
    }

    @Test
    public void remove_excludable_enricher_flow_flow_config() {
        TestEnricher enricher = new TestEnricher();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledPostFetchCommandEnricher(enricher, EXCLUDABLE_ENRICHERS).
                        withoutPostFetchCommandEnrichers(EXCLUDABLE_ENRICHERS).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of());
    }

    @Test
    public void remove_only_excludable_enricher_flow_flow_config() {
        TestEnricher excludableEnricher = new TestEnricher();
        TestEnricher nonExcludableEnricher = new TestEnricher();
        ChangeFlowConfig<TestEntity> flow =
                ChangeFlowConfig.builder(TestEntity.INSTANCE).
                        withLabeledPostFetchCommandEnricher(excludableEnricher, EXCLUDABLE_ENRICHERS).
                        withPostFetchCommandEnricher(nonExcludableEnricher).
                        withoutPostFetchCommandEnrichers(EXCLUDABLE_ENRICHERS).
                        build();
        Assert.assertEquals(flow.getPostFetchCommandEnrichers(), ImmutableList.of(nonExcludableEnricher));
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
}