package com.kenshoo.pl.one2many.events;

import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.EnrichmentEvent;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnrichmentListener;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.kenshoo.pl.one2many.events.ChildEntity.CHILD_NAME;
import static com.kenshoo.pl.one2many.events.ParentEntity.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceLayerEnrichmentEventTest {

    private static final String PARENT_NAME = "parent";
    private static final String PARENT_ID_IN_TARGET = "10";
    private static final String NAME_OF_CHILD = "child";
    private static final String NEW_NAME_OF_CHILD = "rename child";

    private static TablesSetup tablesSetup = new TablesSetup();

    private DSLContext jooq = TestJooqConfig.create();

    private PLContext plContext;

    private PersistenceLayer<ParentEntity> persistenceLayer;

    @Before
    public void setupTables() {
        persistenceLayer = new PersistenceLayer<>(jooq);
        plContext = new PLContext.Builder(jooq).build();

        if (tablesSetup.alreadyCreated) {
            return;
        }
        tablesSetup.alreadyCreated = true;
        tablesSetup.staticDSLContext = jooq;

        DataTableUtils.createTable(jooq, ParentTable.INSTANCE);
        DataTableUtils.createTable(jooq, ChildTable.INSTANCE);
    }

    @After
    public void clearTables() {
        Stream.of(ParentTable.INSTANCE, ChildTable.INSTANCE)
                .map(jooq::deleteFrom)
                .forEach(Query::execute);
    }

    @Test
    public void sent_event_from_child_and_update_parent_in_create() {
        var childCommand = new CreateEntityCommand<>(ChildEntity.INSTANCE);
        childCommand.set(CHILD_NAME, NAME_OF_CHILD);

        var parentCommand = new CreateEntityCommand<>(ParentEntity.INSTANCE);
        parentCommand.set(NAME, PARENT_NAME);
        parentCommand.set(ID_IN_TARGET, PARENT_ID_IN_TARGET);
        parentCommand.addChild(childCommand);

        persistenceLayer.create(List.of(parentCommand), flow(new EnrichParent<>(FIELD_TO_ENRICH)));

        var parentEntity = plContext.select(FIELD_TO_ENRICH).from(INSTANCE).
                where(PLCondition.trueCondition()).
                fetchByKeys(List.of(IdentifierType.uniqueKey(ID_IN_TARGET).createIdentifier(PARENT_ID_IN_TARGET)))
                .get(0);

        assertThat(parentEntity.get(FIELD_TO_ENRICH), is(NAME_OF_CHILD));

    }


    @Test
    public void sent_event_from_child_and_update_parent_in_update() {

        var childCommand = new CreateEntityCommand<>(ChildEntity.INSTANCE);
        childCommand.set(CHILD_NAME, NAME_OF_CHILD);

        var parentCommand = new CreateEntityCommand<>(ParentEntity.INSTANCE);
        parentCommand.set(NAME, PARENT_NAME);
        parentCommand.set(ID_IN_TARGET, PARENT_ID_IN_TARGET);
        parentCommand.addChild(childCommand);

        persistenceLayer.create(List.of(parentCommand), defaultFlow());

        var childUpdCommand = new UpdateEntityCommand<>(ChildEntity.INSTANCE, IdentifierType.uniqueKey(CHILD_NAME).createIdentifier(NAME_OF_CHILD));
        childUpdCommand.set(CHILD_NAME, NEW_NAME_OF_CHILD);

        var parentUpdCommand = new UpdateEntityCommand<>(ParentEntity.INSTANCE, IdentifierType.uniqueKey(ID_IN_TARGET).createIdentifier(PARENT_ID_IN_TARGET));
        parentUpdCommand.addChild(childUpdCommand);

        persistenceLayer.update(List.of(parentUpdCommand), flow(new EnrichParent<>(FIELD_TO_ENRICH)));

        var parentEntity = plContext.select(FIELD_TO_ENRICH).from(INSTANCE).
                where(PLCondition.trueCondition()).
                fetchByKeys(List.of(IdentifierType.uniqueKey(ID_IN_TARGET).createIdentifier(PARENT_ID_IN_TARGET)))
                .get(0);

        assertThat(parentEntity.get(FIELD_TO_ENRICH), is(NEW_NAME_OF_CHILD));

    }

    @Test
    public void sent_event_from_child_and_concatenate_parent_and_child_name() {

        var childCommand = new CreateEntityCommand<>(ChildEntity.INSTANCE);
        childCommand.set(CHILD_NAME, NAME_OF_CHILD);

        var parentCommand = new CreateEntityCommand<>(ParentEntity.INSTANCE);
        parentCommand.set(NAME, PARENT_NAME);
        parentCommand.set(ID_IN_TARGET, PARENT_ID_IN_TARGET);
        parentCommand.addChild(childCommand);

        persistenceLayer.create(List.of(parentCommand), defaultFlow());

        var childUpdCommand = new UpdateEntityCommand<>(ChildEntity.INSTANCE, IdentifierType.uniqueKey(CHILD_NAME).createIdentifier(NAME_OF_CHILD));
        childUpdCommand.set(CHILD_NAME, NEW_NAME_OF_CHILD);

        var parentUpdCommand = new UpdateEntityCommand<>(ParentEntity.INSTANCE, IdentifierType.uniqueKey(ID_IN_TARGET).createIdentifier(PARENT_ID_IN_TARGET));
        parentUpdCommand.addChild(childUpdCommand);

        persistenceLayer.update(List.of(parentUpdCommand), flow(new ConcatenateParentAndChildName<>(NAME, FIELD_TO_ENRICH)));

        var parentEntity = plContext.select(FIELD_TO_ENRICH).from(INSTANCE).
                where(PLCondition.trueCondition()).
                fetchByKeys(List.of(IdentifierType.uniqueKey(ID_IN_TARGET).createIdentifier(PARENT_ID_IN_TARGET)))
                .get(0);

        assertThat(parentEntity.get(FIELD_TO_ENRICH), is(PARENT_NAME + NEW_NAME_OF_CHILD ));

    }

    private ChangeFlowConfig<ParentEntity> defaultFlow() {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, ParentEntity.INSTANCE).
                withChildFlowBuilder(ChangeFlowConfigBuilderFactory.newInstance(plContext, ChildEntity.INSTANCE))
                .build();
    }

    private ChangeFlowConfig<ParentEntity> flow(PostFetchCommandEnricher<ParentEntity> enrichmentListener) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, ParentEntity.INSTANCE).
                withPostFetchCommandEnricher(enrichmentListener).
                withChildFlowBuilder(ChangeFlowConfigBuilderFactory.newInstance(plContext, ChildEntity.INSTANCE).
                        withPostFetchCommandEnricher(buildEnricherPublisher(CHILD_NAME)))
                .build();
    }

    private <E extends EntityType<E>> ChildNamePublisher<E> buildEnricherPublisher(EntityField<E, String> field) {
        return new ChildNamePublisher<>(field);
    }


    private class EnrichParent<E extends EntityType<E>> implements PostFetchCommandEnricher<E>, PostFetchCommandEnrichmentListener<E, ChildNameEvent> {

        private final EntityField<E, String> field_to_enrich;

        private EnrichParent(EntityField<E, String> field_to_enrich) {
            this.field_to_enrich = field_to_enrich;
        }

        @Override
        public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {

        }

        @Override
        public Class<ChildNameEvent> getEventType() {
            return ChildNameEvent.class;
        }

        @Override
        public void enrich(ChangeEntityCommand<E> commandToEnrich, ChildNameEvent enrichmentEvent, ChangeContext changeContext) {
            commandToEnrich.set(field_to_enrich, enrichmentEvent.getChildName());
        }
    }

    private class ConcatenateParentAndChildName<E extends EntityType<E>> implements PostFetchCommandEnricher<E>, PostFetchCommandEnrichmentListener<E, ChildNameEvent> {

        private final EntityField<E, String> name;
        private final EntityField<E, String> field_to_enrich;

        private ConcatenateParentAndChildName(EntityField<E, String> name, EntityField<E, String> field_to_enrich) {
            this.name = name;
            this.field_to_enrich = field_to_enrich;
        }

        @Override
        public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
        }

        @Override
        public Class<ChildNameEvent> getEventType() {
            return ChildNameEvent.class;
        }

        @Override
        public void enrich(ChangeEntityCommand<E> commandToEnrich, ChildNameEvent enrichmentEvent, ChangeContext changeContext) {
            commandToEnrich.set(field_to_enrich, changeContext.getEntity(commandToEnrich).get(name) + enrichmentEvent.getChildName());
        }

        @Override
        public Stream<EntityField<E, ?>> fieldsToEnrich() {
            return Stream.of(name);
        }
    }

    private class ChildNamePublisher<E extends EntityType<E>> implements PostFetchCommandEnricher<E> {

        private final EntityField<E, String> field;

        private ChildNamePublisher(EntityField<E, String> field) {
            this.field = field;
        }

        @Override
        public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
            changeEntityCommands.forEach(cmd -> changeContext.publish(new ChildNameEvent(cmd.get(field), cmd), changeContext));
        }
    }

    private class ChildNameEvent extends EnrichmentEvent {

        private final String childName;

        public ChildNameEvent(String callerName, ChangeEntityCommand<? extends EntityType<?>> source) {
            super(source);
            this.childName = callerName;
        }

        public String getChildName() {
            return childName;
        }
    }

    private static class TablesSetup {
        DSLContext staticDSLContext;
        boolean alreadyCreated = false;
    }

}