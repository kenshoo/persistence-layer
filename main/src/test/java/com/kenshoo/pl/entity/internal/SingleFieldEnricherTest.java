package com.kenshoo.pl.entity.internal;


import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class SingleFieldEnricherTest {

    private static final String VALUE = "value";
    private static final String OVERRIDE_VALUE = "override value";
    private static final String DO_NOT_ENRICH_VALUE = "do not enrich";


    @Test
    public void enriched_fields() {
        TestFieldEnricher enricher = enricher().build();
        assertThat(enricher.fieldsToEnrich().collect(Collectors.toList()), containsInAnyOrder(TestEntity.FIELD_1));
    }

    private TestFieldEnricher.Builder enricher() {
        return new TestFieldEnricher.Builder();
    }

    @Test
    public void support_operation() {
        assertThat(enricher().build().getSupportedChangeOperation(), is(SupportedChangeOperation.CREATE_UPDATE_AND_DELETE));
    }

    @Test
    public void enrich_command() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        enricher().build().enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, prepareCtx(cmd, VALUE));
        assertThat(cmd.get(TestEntity.FIELD_1), is(VALUE));
    }

    @Test
    public void should_not_run_enrich_command() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd.set(TestEntity.FIELD_1, VALUE);
        enricher().build().enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, prepareCtx(cmd, OVERRIDE_VALUE));
        assertThat(cmd.get(TestEntity.FIELD_1), is(VALUE));
    }

    @Test
    public void should_not_enrich_command_when_field_contains_null() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd.set(TestEntity.FIELD_1, (String)null);
        enricher().build().enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, prepareCtx(cmd, VALUE));
        assertThat(cmd.get(TestEntity.FIELD_1), is((String)null));
    }

    @Test
    public void should_not_enrich_command_when_triggered_field_is_not_exist() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        enricher().withTriggeredField(TestEntity.FIELD_3).build().enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, prepareCtx(cmd, VALUE));
        assertThat(cmd.containsField(TestEntity.FIELD_1), is(false));
    }

    @Test
    public void enrich_command_when_field_contains_null() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd.set(TestEntity.FIELD_1, (String)null);
        enricher().considerNullAsMissing().build().enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, prepareCtx(cmd, VALUE));
        assertThat(cmd.get(TestEntity.FIELD_1), is(VALUE));
    }

    @Test
    public void enrich_command_when_triggered_field_exist() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd.set(TestEntity.FIELD_3, 10);
        enricher().withTriggeredField(TestEntity.FIELD_3).build().enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, prepareCtx(cmd, VALUE));
        assertThat(cmd.get(TestEntity.FIELD_1), is(VALUE));
    }

    @Test
    public void do_not_override_field_when_exist_in_command() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        enricher().build().enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, prepareCtx(cmd, DO_NOT_ENRICH_VALUE));
        assertThat(cmd.containsField(TestEntity.FIELD_1), is(false));
    }


    @Test
    public void enrich_should_run() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        assertThat(enricher().build().shouldRun(ImmutableList.of(cmd)), is(true));
    }

    @Test
    public void enrich_should_run_for_mixed_commands() {
        CreateEntityCommand<TestEntity> shouldNotEnrichCmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        CreateEntityCommand<TestEntity> shouldEnrichCmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        shouldEnrichCmd.set(TestEntity.FIELD_1, VALUE);
        assertThat(enricher().build().shouldRun(ImmutableList.of(shouldNotEnrichCmd, shouldEnrichCmd)), is(true));
    }

    @Test
    public void enrich_should_not_run() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd.set(TestEntity.FIELD_1, "value");
        assertThat(enricher().build().shouldRun(ImmutableList.of(cmd)), is(false));
    }

    @Test
    public void enrich_should_not_run_when_field_has_null() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd.set(TestEntity.FIELD_1, (String)null);
        assertThat(enricher().build().shouldRun(ImmutableList.of(cmd)), is(false));
    }

    @Test
    public void enrich_should_run_when_field_has_null_but_considered_as_field_is_not_exist() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd.set(TestEntity.FIELD_1, (String)null);
        assertThat(enricher().considerNullAsMissing().build().shouldRun(ImmutableList.of(cmd)), is(true));
    }

    @Test
    public void enrich_should_not_run_when_triggered_field_is_not_exist() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        assertThat(enricher().withTriggeredField(TestEntity.FIELD_3).build().shouldRun(ImmutableList.of(cmd)), is(false));
    }

    @Test
    public void enrich_should_run_when_triggered_field_is_exist() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd.set(TestEntity.FIELD_3, 100);
        assertThat(enricher().withTriggeredField(TestEntity.FIELD_3).build().shouldRun(ImmutableList.of(cmd)), is(true));
    }

    private ChangeContext prepareCtx(CreateEntityCommand<TestEntity> cmd, String value) {
        ChangeContextImpl ctx = new ChangeContextImpl(null, FeatureSet.EMPTY);
        EntityImpl entity = new EntityImpl();
        entity.set(TestEntity.FIELD_2, value);
        ctx.addEntity(cmd, entity);
        return ctx;
    }

    static class TestFieldEnricher extends SingleFieldEnricher<TestEntity, String> {

        private final boolean considerNullAsMissing;
        private final EntityField<TestEntity, ?> triggeredField;

        TestFieldEnricher(boolean considerNullAsMissing, EntityField<TestEntity, ?> triggeredField) {
            this.considerNullAsMissing = considerNullAsMissing;
            this.triggeredField = triggeredField;
        }

        @Override
        protected EntityField<TestEntity, String> enrichedField() {
            return TestEntity.FIELD_1;
        }

        @Override
        protected String enrichedValue(EntityChange<TestEntity> entityChange, Entity entity) {
            return entity.get(TestEntity.FIELD_2);
        }

        @Override
        protected boolean needEnrich(EntityChange<TestEntity> entityChange, Entity entity) {
            return !(entity.containsField(TestEntity.FIELD_2) && entity.get(TestEntity.FIELD_2).equals(DO_NOT_ENRICH_VALUE));
        }

        @Override
        protected boolean considerNullAsMissing() {
            return considerNullAsMissing;
        }

        @Override
        protected Stream<EntityField<TestEntity, ?>> triggeredByFields() {
            return triggeredField != null ? Stream.of(triggeredField) : super.triggeredByFields();
        }

        public static class Builder {

            private boolean considerNullAsMissing = false;
            private EntityField<TestEntity, ?> triggeredField;

            Builder considerNullAsMissing() {
                this.considerNullAsMissing = true;
                return this;
            }

            Builder withTriggeredField(EntityField<TestEntity, ?> triggeredField) {
                this.triggeredField = triggeredField;
                return this;
            }

            public TestFieldEnricher build(){
                return new TestFieldEnricher(considerNullAsMissing, triggeredField);
            }
        }
    }
}