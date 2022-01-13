package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.EnrichmentEvent;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnrichmentListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;

import static com.kenshoo.pl.entity.TestChildEntity.CHILD_FIELD_1;
import static com.kenshoo.pl.entity.TestChildEntity.CHILD_FIELD_3;
import static com.kenshoo.pl.entity.TestEntity.FIELD_1;
import static com.kenshoo.pl.entity.TestEntity.FIELD_3;
import static com.kenshoo.pl.entity.TestGrandChildEntity.GRAND_CHILD_FIELD_1;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PostFetchCommandEnrichmentPublisherTest {

    private static final String EVENT_TEST_STRING = "test";

    @Test
    public void sent_event_from_child_to_parent() {
        var enricherPublisher = buildEnricherPublisher(CHILD_FIELD_1);
        var flow =
                new ChangeFlowConfig.Builder<>(TestEntity.INSTANCE).
                        withPostFetchCommandEnricher(buildListenerEnricher(FIELD_1, FIELD_3)).
                        withChildFlowBuilder(new ChangeFlowConfig.Builder<>(TestChildEntity.INSTANCE).
                        withPostFetchCommandEnricher(enricherPublisher))
                        .build();

        var parentCommand = new CreateEntityCommand<>(TestEntity.INSTANCE);
        var childCommand = new CreateEntityCommand<>(TestChildEntity.INSTANCE);
        childCommand.set(CHILD_FIELD_1, EVENT_TEST_STRING);
        parentCommand.addChild(childCommand);

        enricherPublisher.enrich(List.of(childCommand), ChangeOperation.CREATE, createFlowContext(flow));
        assertThat(parentCommand.get(FIELD_1), is(EVENT_TEST_STRING));

    }

    @Test
    public void sent_event_from_grand_child_to_grand_parent() {
        var enricherPublisher = buildEnricherPublisher(GRAND_CHILD_FIELD_1);
        var flow =
                new ChangeFlowConfig.Builder<>(TestEntity.INSTANCE).
                        withPostFetchCommandEnricher(buildListenerEnricher(FIELD_1, FIELD_3)).
                        withChildFlowBuilder(new ChangeFlowConfig.Builder<>(TestChildEntity.INSTANCE).
                        withChildFlowBuilder(new ChangeFlowConfig.Builder<>(TestGrandChildEntity.INSTANCE).
                        withPostFetchCommandEnricher(enricherPublisher))).
                        build();

        ChangeEntityCommand<TestEntity> parentCommand = new CreateEntityCommand<>(TestEntity.INSTANCE);
        ChangeEntityCommand<TestChildEntity> childCommand = new CreateEntityCommand<>(TestChildEntity.INSTANCE);
        ChangeEntityCommand<TestGrandChildEntity> ninCommand = new CreateEntityCommand<>(TestGrandChildEntity.INSTANCE);
        ninCommand.set(GRAND_CHILD_FIELD_1, EVENT_TEST_STRING);
        childCommand.addChild(ninCommand);
        parentCommand.addChild(childCommand);

        enricherPublisher.enrich(List.of(ninCommand), ChangeOperation.CREATE, createFlowContext(flow));
        assertThat(parentCommand.get(FIELD_1), is(EVENT_TEST_STRING));

    }

    @Test
    public void sent_event_from_grand_child_to_child_and_parent() {
        var enricherPublisher = buildEnricherPublisher(GRAND_CHILD_FIELD_1);
        ChangeFlowConfig<TestEntity> flow =
                new ChangeFlowConfig.Builder<>(TestEntity.INSTANCE).
                        withPostFetchCommandEnricher(buildListenerEnricher(FIELD_1, FIELD_3)).
                        withChildFlowBuilder(new ChangeFlowConfig.Builder<>(TestChildEntity.INSTANCE).
                                withPostFetchCommandEnricher(buildListenerEnricher(CHILD_FIELD_1, CHILD_FIELD_3)).
                                withChildFlowBuilder(new ChangeFlowConfig.Builder<>(TestGrandChildEntity.INSTANCE).
                                        withPostFetchCommandEnricher(enricherPublisher))).
                        build();

        var parentCommand = new CreateEntityCommand<>(TestEntity.INSTANCE);
        var childCommand = new CreateEntityCommand<>(TestChildEntity.INSTANCE);
        var ninCommand = new CreateEntityCommand<>(TestGrandChildEntity.INSTANCE);
        ninCommand.set(GRAND_CHILD_FIELD_1, "nin");
        childCommand.addChild(ninCommand);
        parentCommand.addChild(childCommand);

        enricherPublisher.enrich(List.of(ninCommand), ChangeOperation.CREATE, createFlowContext(flow));
        assertThat(childCommand.get(CHILD_FIELD_1), is("nin"));
        assertThat(parentCommand.get(FIELD_1), is("nin"));

    }

    @Test
    public void sent_event_from_grand_child_to_ancestors_in_correct_order() {
        var enricherPublisher = new TestEntityEnricherPublisher<>(GRAND_CHILD_FIELD_1);
        var flow =
                new ChangeFlowConfig.Builder<>(TestEntity.INSTANCE).
                        withPostFetchCommandEnricher(buildListenerEnricher(FIELD_1, FIELD_3)).
                        withChildFlowBuilder(new ChangeFlowConfig.Builder<>(TestChildEntity.INSTANCE).
                                withPostFetchCommandEnricher(buildListenerEnricher(CHILD_FIELD_1, CHILD_FIELD_3)).
                                withChildFlowBuilder(new ChangeFlowConfig.Builder<>(TestGrandChildEntity.INSTANCE).
                                        withPostFetchCommandEnricher(enricherPublisher))).
                        build();

        var parentCommand = new CreateEntityCommand<>(TestEntity.INSTANCE);
        var childCommand = new CreateEntityCommand<>(TestChildEntity.INSTANCE);
        var ninCommand = new CreateEntityCommand<>(TestGrandChildEntity.INSTANCE);
        ninCommand.set(GRAND_CHILD_FIELD_1, "nin");
        childCommand.addChild(ninCommand);
        parentCommand.addChild(childCommand);

        enricherPublisher.enrich(List.of(ninCommand), ChangeOperation.CREATE, createFlowContext(flow));
        assertThat(childCommand.get(CHILD_FIELD_3), is(1));
        assertThat(parentCommand.get(FIELD_3), is(2));

    }

    private <E extends EntityType<E>> TestEntityEnricherPublisher<E> buildEnricherPublisher(EntityField<E, String> field) {
        return new TestEntityEnricherPublisher<>(field);
    }

    private <E extends EntityType<E>> PostFetchCommandEnricher<E> buildListenerEnricher(EntityField<E, String> field, EntityField<E, Integer> order) {
        return new TestEntityEnricherListener<>(field, order);
    }

    private ChangeContextImpl createFlowContext(ChangeFlowConfig<TestEntity> flow) {
        return new ChangeContextImpl(null, null, PostFetchCommandEnrichmentListenersManager.build(flow));
    }

    private class TestEntityEnricherListener<E extends EntityType<E>> implements PostFetchCommandEnricher<E>, PostFetchCommandEnrichmentListener<E, TestEnrichmentEvent> {

        private final EntityField<E, String> field;
        private final EntityField<E, Integer> order;

        private TestEntityEnricherListener(EntityField<E, String> field, EntityField<E, Integer> order) {
            this.field = field;
            this.order = order;
        }

        @Override
        public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {

        }

        @Override
        public Class<TestEnrichmentEvent> getEventType() {
            return TestEnrichmentEvent.class;
        }

        @Override
        public void enrich(ChangeEntityCommand<E> commandToEnrich, TestEnrichmentEvent enrichmentEvent, ChangeContext changeContext) {
            commandToEnrich.set(field, enrichmentEvent.getCallerName());
            commandToEnrich.set(order,  enrichmentEvent.getCallOrder());
        }
    }

    private class TestEntityEnricherPublisher<E extends EntityType<E>> implements PostFetchCommandEnricher<E> {

        private final EntityField<E, String> field;

        private TestEntityEnricherPublisher(EntityField<E, String> field) {
            this.field = field;
        }

        @Override
        public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
            changeEntityCommands.forEach(cmd -> changeContext.publish(new TestEnrichmentEvent(cmd.get(field), cmd), changeContext));
        }
    }

    private class TestEnrichmentEvent extends EnrichmentEvent {

        private final String callerName;
        private int callOrder = 0;

        public TestEnrichmentEvent(String callerName, ChangeEntityCommand<? extends EntityType<?>> source) {
            super(source);
            this.callerName = callerName;
        }

        public String getCallerName() {
            return callerName;
        }

        public int getCallOrder() {
            return ++callOrder;
        }
    }
}