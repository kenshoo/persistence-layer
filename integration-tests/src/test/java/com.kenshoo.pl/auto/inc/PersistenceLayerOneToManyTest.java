package com.kenshoo.pl.auto.inc;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.Feature.AutoIncrementSupport;
import static org.hamcrest.core.Is.is;
import static org.jooq.lambda.Seq.seq;
import static org.junit.Assert.assertThat;

public class PersistenceLayerOneToManyTest {

    private final static ParentTable PARENT_TABLE = ParentTable.INSTANCE;
    private final static ChildTable CHILD_TABLE = ChildTable.INSTANCE;

    private DSLContext jooq = TestJooqConfig.create();

    private PLContext plContext;

    private PersistenceLayer<ParentEntity, ParentEntity.Key> persistenceLayer;

    private ChangeFlowConfig<ParentEntity> changeFlowConfig;

    private static DSLContext dslContext;

    @Before
    public void setupTables() {

        plContext = new PLContext.Builder(jooq).build();

        changeFlowConfig = ChangeFlowConfigBuilderFactory.newInstance(plContext, ParentEntity.INSTANCE).
                withChildFlowBuilder(ChangeFlowConfigBuilderFactory.newInstance(plContext, ChildEntity.INSTANCE))
                .with(new FeatureSet(AutoIncrementSupport))
                .build();

        persistenceLayer = new PersistenceLayer<>(jooq);

        dslContext = jooq;
        Stream.of(PARENT_TABLE, CHILD_TABLE)
                .forEach(table -> DataTableUtils.createTable(dslContext, table));
    }

    @After
    public void tearDown() {
        Stream.of(PARENT_TABLE, CHILD_TABLE)
                .forEach(table -> dslContext.dropTable(table).execute());
    }

    @Test
    public void create_parent_with_2_children() {

        final ParentEntityCreateCommand parent1 = newParent().with(ParentEntity.NAME, "parent1")
                .with(newChild()
                        .with(ChildEntity.ORDINAL, 1)
                        .with(ChildEntity.FIELD_1, "child1"))
                .with(newChild()
                        .with(ChildEntity.ORDINAL, 2)
                        .with(ChildEntity.FIELD_1, "child2"));

        final ParentEntityCreateCommand parent2 = newParent().with(ParentEntity.NAME, "parent2")
                .with(newChild()
                        .with(ChildEntity.ORDINAL, 1)
                        .with(ChildEntity.FIELD_1, "child3"));


        List<Integer> retrievedParentIds = createAndRetrieveIds(ImmutableList.of(parent1, parent2));

        Map<String, ChildPojo> childrenByNames = jooq.select().from(CHILD_TABLE).fetchMap(CHILD_TABLE.field1, toChildPojo());

        assertThat(childrenByNames.get("child1").parentId, is(retrievedParentIds.get(0)));
        assertThat(childrenByNames.get("child1").ordinal, is(1));

        assertThat(childrenByNames.get("child1").parentId, is(retrievedParentIds.get(0)));
        assertThat(childrenByNames.get("child2").ordinal, is(2));

        assertThat(childrenByNames.get("child3").parentId, is(retrievedParentIds.get(1)));
        assertThat(childrenByNames.get("child3").ordinal, is(1));
    }

    @Test
    public void upsert_for_non_existing_parent() {

        final UpsertByIdInTarget parent = new UpsertByIdInTarget(1000).with(ParentEntity.NAME, "parent")
                .with(newChild()
                        .with(ChildEntity.ORDINAL, 1)
                        .with(ChildEntity.FIELD_1, "child1"));

        List<Integer> retrievedParentIds = seq(persistenceLayer.upsert(ImmutableList.of(parent), changeFlowConfig))
                .map(res -> res.getCommand().get(ParentEntity.ID)).toList();

        Map<String, ChildPojo> childrenByNames = jooq.select().from(CHILD_TABLE).fetchMap(CHILD_TABLE.field1, toChildPojo());

        assertThat(childrenByNames.get("child1").parentId, is(retrievedParentIds.get(0)));
        assertThat(childrenByNames.get("child1").ordinal, is(1));
    }

    @Test
    public void upsert_for_existing_parent() {

        final ParentEntityCreateCommand parentCreation = newParent()
                .with(ParentEntity.ID_IN_TARGET, 1000)
                .with(ParentEntity.NAME, "parent");

        List<Integer> retrievedParentIds = createAndRetrieveIds(ImmutableList.of(parentCreation));

        final UpsertByIdInTarget parentUpsert = new UpsertByIdInTarget(1000)
                .with(newChild()
                        .with(ChildEntity.ORDINAL, 1)
                        .with(ChildEntity.FIELD_1, "child1"));

        persistenceLayer.upsert(ImmutableList.of(parentUpsert), changeFlowConfig);

        Map<String, ChildPojo> childrenByNames = jooq.select().from(CHILD_TABLE).fetchMap(CHILD_TABLE.field1, toChildPojo());

        assertThat(childrenByNames.get("child1").parentId, is(retrievedParentIds.get(0)));
        assertThat(childrenByNames.get("child1").ordinal, is(1));
    }

    private List<Integer> createAndRetrieveIds(ImmutableList<ParentEntityCreateCommand> of) {
        return seq(persistenceLayer.create(of, changeFlowConfig, ParentEntity.Key.DEFINITION))
                .map(res -> res.getIdentifier().getId()).toList();
    }

    private ParentEntityCreateCommand newParent() {
        return new ParentEntityCreateCommand();
    }

    private ChildEntityCreateCommand newChild() {
        return new ChildEntityCreateCommand();
    }

    class UpsertByIdInTarget extends InsertOnDuplicateUpdateCommand<ParentEntity, ParentEntity.UniqueKey> implements EntityCommandExt<ParentEntity, UpsertByIdInTarget> {
        public UpsertByIdInTarget(int idInTarget) {
            super(ParentEntity.INSTANCE, new ParentEntity.UniqueKey(idInTarget));
        }
    }

    class ParentEntityCreateCommand extends CreateEntityCommand<ParentEntity> implements EntityCommandExt<ParentEntity, ParentEntityCreateCommand> {

        ParentEntityCreateCommand() {
            super(ParentEntity.INSTANCE);
        }
    }

    class ChildEntityCreateCommand extends CreateEntityCommand<ChildEntity> implements EntityCommandExt<ChildEntity, ChildEntityCreateCommand> {

        ChildEntityCreateCommand() {
            super(ChildEntity.INSTANCE);
        }
    }

    private RecordMapper<Record, ChildPojo> toChildPojo() {
        return rec -> new ChildPojo() {{
            parentId = rec.get(CHILD_TABLE.parent_id);
            ordinal = rec.get(CHILD_TABLE.ordinal);
            field1 = rec.get(CHILD_TABLE.field1);
        }};
    }

}
