package com.kenshoo.pl.entity.internal;


import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class SingleFieldEnricherTest {

    static final String VALUE = "value";
    public static final String OVERRIDE_VALUE = "override value";
    public static final String DO_NOT_ENRICH_VALUE = "do not enrich";

    private SingleFieldEnricher<TestEntity, String> simpleFieldEnricher = new TestFieldEnricher();

    @Test
    public void enriched_fields() {
        assertThat(simpleFieldEnricher.fieldsToEnrich().collect(Collectors.toList()), containsInAnyOrder(TestEntity.FIELD_1));
    }

    @Test
    public void support_operation() {
        assertThat(simpleFieldEnricher.getSupportedChangeOperation(), is(SupportedChangeOperation.CREATE_UPDATE_AND_DELETE));
    }

    @Test
    public void enrich_command() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        simpleFieldEnricher.enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, prepareCtx(cmd, VALUE));
        assertThat(cmd.get(TestEntity.FIELD_1), is(VALUE));
    }

    @Test
    public void should_not_run_enrich_command() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd.set(TestEntity.FIELD_1, VALUE);
        simpleFieldEnricher.enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, prepareCtx(cmd, OVERRIDE_VALUE));
        assertThat(cmd.get(TestEntity.FIELD_1), is(VALUE));
    }

    @Test
    public void do_not_need_enrich_command() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        simpleFieldEnricher.enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, prepareCtx(cmd, DO_NOT_ENRICH_VALUE));
        assertThat(cmd.containsField(TestEntity.FIELD_1), is(false));
    }


    @Test
    public void enrich_should_run() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        assertThat(simpleFieldEnricher.shouldRun(ImmutableList.of(cmd)), is(true));
    }

    @Test
    public void enrich_should_run_for_mixed_commands() {
        CreateEntityCommand<TestEntity> shouldNotEnrichCmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        CreateEntityCommand<TestEntity> shouldEnrichCmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        shouldEnrichCmd.set(TestEntity.FIELD_1, VALUE);
        assertThat(simpleFieldEnricher.shouldRun(ImmutableList.of(shouldNotEnrichCmd, shouldEnrichCmd)), is(true));
    }

    @Test
    public void enrich_should_not_run() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd.set(TestEntity.FIELD_1, "value");
        assertThat(simpleFieldEnricher.shouldRun(ImmutableList.of(cmd)), is(false));
    }

    private ChangeContext prepareCtx(CreateEntityCommand<TestEntity> cmd, String value) {
        ChangeContextImpl ctx = new ChangeContextImpl(null, FeatureSet.EMPTY);
        EntityImpl entity = new EntityImpl();
        entity.set(TestEntity.FIELD_2, value);
        ctx.addEntity(cmd, entity);
        return ctx;
    }

    static class TestFieldEnricher extends SingleFieldEnricher<TestEntity, String> {
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
    }
}