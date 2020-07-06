package com.kenshoo.pl.entity.spi;

import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.ExistingFieldModifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExistingFieldModifierTest {

    @Mock
    private ChangeContext changeContext;

    @Mock
    private Entity currentState;

    private final TestFieldEnricher testFieldEnricher = new TestFieldEnricher();

    private final CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);

    @Before
    public void setup() {
        when(changeContext.getEntity(cmd)) .thenReturn(currentState);
    }

    @Test
    public void enrich_should_run() {
        cmd.set(TestEntity.FIELD_1, "value");
        assertThat(testFieldEnricher.shouldRun(ImmutableList.of(cmd)), is(true));
    }

    @Test
    public void enrich_should_not_run() {
        assertThat(testFieldEnricher.shouldRun(ImmutableList.of(cmd)), is(false));
    }

    @Test
    public void enrich_command_when_field_contains_value() {
        cmd.set(TestEntity.FIELD_1, "value");
        testFieldEnricher.enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, changeContext);
        assertThat(cmd.get(TestEntity.FIELD_1), is("override value"));
    }
    @Test
    public void do_not_enrich_command_when_field_does_not_contain_value() {
        testFieldEnricher.enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, changeContext);
        assertThat(cmd.containsField(TestEntity.FIELD_1), is(false));
    }

    static class TestFieldEnricher extends ExistingFieldModifier<TestEntity, String> {

        @Override
        protected EntityField<TestEntity, String> enrichedField() {
            return TestEntity.FIELD_1;
        }

        @Override
        protected String enrichedValue(EntityChange<TestEntity> entityChange, Entity currentState) {
            return "override " + entityChange.get(TestEntity.FIELD_1);
        }
    }
}