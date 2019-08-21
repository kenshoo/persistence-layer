package com.kenshoo.pl.auto.inc;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import org.jooq.DSLContext;
import org.jooq.lambda.Seq;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.jooq.lambda.Seq.seq;
import static org.junit.Assert.assertThat;

public class PersistenceLayerOneToOneTest {

    private static final TestEntityTable PRIMARY_TABLE = TestEntityTable.INSTANCE;
    private static final TestSecondaryEntityTable SECONDARY_TABLE = TestSecondaryEntityTable.INSTANCE;

    private DSLContext dslContext;
    private PersistenceLayer<TestEntity, TestEntity.Key> persistenceLayer;
    private ChangeFlowConfig<TestEntity> changeFlowConfig;

    @Before
    public void setUp() {
        dslContext = TestJooqConfig.create();
        persistenceLayer = new PersistenceLayer<>(dslContext);
        final PLContext plContext = new PLContext.Builder(dslContext).build();
        changeFlowConfig = ChangeFlowConfigBuilderFactory.newInstance(plContext, TestEntity.INSTANCE).build();

        Stream.of(PRIMARY_TABLE, SECONDARY_TABLE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));
    }

    @After
    public void tearDown() {
        Stream.of(SECONDARY_TABLE, PRIMARY_TABLE)
              .forEach(table -> dslContext.deleteFrom(table).execute());
    }

    @Ignore
    @Test
    public void auto_generated_ids_are_returned_in_order() {
        final List<TestEntityCreateCommand> createCommands = Stream.of("name1", "name2", "name3")
                                                                   .sorted()
                                                                   .map(name -> TestEntityCreateCommand.builder().withName(name).build())
                                                                   .collect(toList());

        final CreateResult<TestEntity, TestEntity.Key> creationResult = persistenceLayer.create(createCommands,
                                                                                                changeFlowConfig,
                                                                                                TestEntity.Key.DEFINITION);

        final List<Integer> expectedReturnedIds = dslContext.select(PRIMARY_TABLE.id)
                                                            .from(PRIMARY_TABLE)
                                                            .orderBy(PRIMARY_TABLE.name)
                                                            .fetch(PRIMARY_TABLE.id);

        final List<Integer> actualReturnedIds = seq(creationResult)
                                                     .filter(entityResult -> nonNull(entityResult.getIdentifier()))
                                                     .map(entityResult -> entityResult.getIdentifier().getId())
                                                     .filter(Objects::nonNull)
                                                     .toList();

        assertThat("Incorrect returned ids: ",
                   actualReturnedIds, equalTo(expectedReturnedIds));
    }

    @Ignore
    @Test
    public void auto_generated_id_of_primary_table_is_populated_in_secondary_table() {
        final String name = "name";
        final String secondName = "secondName";

        final TestEntityCreateCommand createCommand = TestEntityCreateCommand.builder()
                                                                             .withName(name)
                                                                             .withSecondName(secondName)
                                                                             .build();

        persistenceLayer.create(ImmutableList.of(createCommand),
                                changeFlowConfig,
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

}
