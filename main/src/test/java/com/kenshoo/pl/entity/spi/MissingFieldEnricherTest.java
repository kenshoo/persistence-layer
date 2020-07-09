package com.kenshoo.pl.entity.spi;

import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MissingFieldEnricherTest {

    @Mock
    private ChangeContext changeContext;

    @Mock
    private CurrentEntityState currentState;

    private final CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);

    @Before
    public void setup() {
       when(changeContext.getEntity(cmd)).thenReturn(currentState);
    }

    @Test
    public void enrich_should_run() {
        assertThat(enricher().build().shouldRun(ImmutableList.of(cmd)), is(true));
    }

    @Test
    public void enrich_should_not_run() {
        cmd.set(TestEntity.FIELD_1, "value");
        assertThat(enricher().build().shouldRun(ImmutableList.of(cmd)), is(false));
    }

    @Test
    public void enrich_command_when_field_does_not_contain_value() {
        enricher().build().enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, changeContext);
        assertThat(cmd.get(TestEntity.FIELD_1), is("value"));
    }

    @Test
    public void do_not_enrich_command_when_field_contains_value() {
        cmd.set(TestEntity.FIELD_1, "do not override value");
        enricher().build().enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, changeContext);
        assertThat(cmd.get(TestEntity.FIELD_1), is("do not override value"));
    }

    @Test
    public void should_not_enrich_command_when_field_contains_null() {
        cmd.set(TestEntity.FIELD_1, (String)null);
        enricher().build().enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, changeContext);
        assertThat(cmd.get(TestEntity.FIELD_1), is((String)null));
    }

    @Test
    public void enrich_should_not_run_when_field_has_null() {
        cmd.set(TestEntity.FIELD_1, (String)null);
        assertThat(enricher().build().shouldRun(ImmutableList.of(cmd)), is(false));
    }

    @Test
    public void enrich_command_when_field_contains_null() {
        cmd.set(TestEntity.FIELD_1, (String)null);
        enricher().considerNullAsMissing().build().enrich(ImmutableList.of(cmd), ChangeOperation.CREATE, changeContext);
        assertThat(cmd.get(TestEntity.FIELD_1), is("value"));
    }

    @Test
    public void enrich_should_run_when_field_has_null_but_considered_as_field_is_not_exist() {
        CreateEntityCommand<TestEntity> cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd.set(TestEntity.FIELD_1, (String)null);
        assertThat(enricher().considerNullAsMissing().build().shouldRun(ImmutableList.of(cmd)), is(true));
    }

    private TestFieldEnricher.Builder enricher() {
        return new TestFieldEnricher.Builder();
    }

    static class TestFieldEnricher extends MissingFieldEnricher<TestEntity, String> {

        private final boolean considerNullAsMissing;

        public TestFieldEnricher(boolean considerNullAsMissing) {
            this.considerNullAsMissing = considerNullAsMissing;
        }

        @Override
        protected boolean considerNullAsMissing() {
            return considerNullAsMissing;
        }

        @Override
        protected EntityField<TestEntity, String> enrichedField() {
            return TestEntity.FIELD_1;
        }

        @Override
        protected String enrichedValue(EntityChange<TestEntity> entityChange, CurrentEntityState currentState) {
            return "value";
        }

        public static class Builder {

            private boolean considerNullAsMissing = false;

            TestFieldEnricher.Builder considerNullAsMissing() {
                this.considerNullAsMissing = true;
                return this;
            }

            public TestFieldEnricher build(){
                return new TestFieldEnricher(considerNullAsMissing);
            }
        }
    }
}