package com.kenshoo.pl.auto.inc;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.kenshoo.pl.auto.inc.TestEntity.NAME;
import static com.kenshoo.pl.auto.inc.TestEntity.SECOND_NAME;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.jooq.lambda.Seq.seq;
import static org.junit.Assert.assertThat;

public class PersistenceLayerOneToOneTest {

    private static final TestEntityTable PRIMARY_TABLE = TestEntityTable.INSTANCE;
    private static final TestSecondaryEntityTable SECONDARY_TABLE = TestSecondaryEntityTable.INSTANCE;

    private DSLContext dslContext;
    private PersistenceLayer<TestEntity> persistenceLayer;
    private ChangeFlowConfig<TestEntity> flowConfig;

    @Before
    public void setUp() {

        dslContext = TestJooqConfig.create();
        persistenceLayer = new PersistenceLayer<>(dslContext);
        final PLContext plContext = new PLContext.Builder(dslContext).build();
        flowConfig = ChangeFlowConfigBuilderFactory
            .newInstance(plContext, TestEntity.INSTANCE)
            .build();

        Stream.of(PRIMARY_TABLE, SECONDARY_TABLE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));
    }

    @After
    public void tearDown() {
        Stream.of(SECONDARY_TABLE, PRIMARY_TABLE)
              .forEach(table -> dslContext.deleteFrom(table).execute());
    }

    @Test
    public void auto_generated_ids_are_returned_in_order() {
        final List<TestEntityCreateCommand> createCommands = Stream.of("name1", "name2", "name3")
                                                                   .sorted()
                                                                   .map(name -> new TestEntityCreateCommand().with(NAME, name))
                                                                   .collect(toList());

        final CreateResult<TestEntity, TestEntity.Key> creationResult = persistenceLayer.create(createCommands,
                                                                                                flowConfig,
                                                                                                TestEntity.Key.DEFINITION);

        final List<Integer> expectedReturnedIds = dslContext.select(PRIMARY_TABLE.id)
                                                            .from(PRIMARY_TABLE)
                                                            .orderBy(PRIMARY_TABLE.name)
                                                            .fetch(PRIMARY_TABLE.id);

        final List<Integer> actualReturnedIds = seq(creationResult)
                .map(EntityChangeResult::getIdentifier)
                .map(TestEntity.Key::getId)
                .filter(Objects::nonNull)
                .toList();


        assertThat("Incorrect returned ids: ",
                   actualReturnedIds, equalTo(expectedReturnedIds));
    }

    @Test
    public void auto_generated_id_of_primary_table_is_populated_in_secondary_table() {
        final String name = "name";
        final String secondName = "secondName";

        final TestEntityCreateCommand createCommand = new TestEntityCreateCommand()
                                                                             .with(NAME, name)
                                                                             .with(SECOND_NAME, secondName);

        persistenceLayer.create(ImmutableList.of(createCommand),
                                flowConfig,
                                TestEntity.Key.DEFINITION);

        final int primaryId = dslContext.select(PRIMARY_TABLE.id)
                                        .from(PRIMARY_TABLE)
                                        .where(PRIMARY_TABLE.name.eq(name))
                                        .fetchOne(PRIMARY_TABLE.id);

        final int primaryIdInSecondary = dslContext.select(SECONDARY_TABLE.parentId)
                                                         .from(SECONDARY_TABLE)
                                                         .where(SECONDARY_TABLE.secondName.eq(secondName))
                                                         .fetchOne(SECONDARY_TABLE.parentId);

        assertThat("Incorrect primary id in secondary table: ",
                   primaryIdInSecondary, equalTo(primaryId));
    }

    @Test
    public void test_upserts() {

        EntityChangeResult<TestEntity, Identifier<TestEntity>, CreateEntityCommand<TestEntity>> existingItem = persistenceLayer.create(singletonList(
                new TestEntityCreateCommand().with(NAME, "existing item")
        ), flowConfig, TestEntity.Key.DEFINITION).iterator().next();

        final List<InsertOnDuplicateUpdateCommand<TestEntity, Name>> upserts = Stream
                .of("existing item", "new item")
                .map(name -> new InsertOnDuplicateUpdateCommand<>(TestEntity.INSTANCE, new Name(name)))
                .collect(toList());

        InsertOnDuplicateUpdateResult<TestEntity, Name> upsertResults = persistenceLayer.upsert(upserts, flowConfig);

        final Map<String, Integer> fromDB = dslContext.select(PRIMARY_TABLE.id, PRIMARY_TABLE.name)
                .from(PRIMARY_TABLE)
                .orderBy(PRIMARY_TABLE.name)
                .fetchMap(r -> r.get(PRIMARY_TABLE.name), r -> r.get(PRIMARY_TABLE.id));

        assertThat(existingItem.getCommand().get(TestEntity.ID), is(fromDB.get("existing item")));
        assertThat(second(upsertResults).getCommand().get(TestEntity.ID), is(fromDB.get("new item")));
    }

    private EntityChangeResult<TestEntity, Name, InsertOnDuplicateUpdateCommand<TestEntity, Name>> second(InsertOnDuplicateUpdateResult<TestEntity, Name> upsertResults) {
        return seq(upsertResults.iterator()).get(1).get();
    }

}
