package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.AbstractDataTable;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.kenshoo.matcher.EntityHasFieldValuesMatcher.fieldValue;
import static com.kenshoo.matcher.EntityHasFieldValuesMatcher.hasFieldValues;
import static com.kenshoo.pl.entity.PLCondition.not;
import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class EntitiesFetcherByPLConditionTest {

    private static final TestTable table = TestTable.INSTANCE;
    private static final TestParentTable parent_table = TestParentTable.INSTANCE;
    private static final TestParentSecondaryTable parent_sec_table = TestParentSecondaryTable.INSTANCE;
    private static final Set<DataTable> ALL_TABLES = ImmutableSet.of(table,
                                                                     parent_table,
                                                                     parent_sec_table);

    private static DSLContext staticDSLContext;
    private static boolean tablesCreated;

    private DSLContext dslContext = TestJooqConfig.create();

    private EntitiesFetcher entitiesFetcher;

    @Before
    public void setup() {
        entitiesFetcher = new EntitiesFetcher(dslContext, new FeatureSet());
        staticDSLContext = dslContext;
        if (!tablesCreated) {
            ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));
            tablesCreated = true;
        }
        dslContext.insertInto(parent_table)
                  .columns(parent_table.id, parent_table.field1, parent_table.field2)
                  .values(1, "ParentAlpha", "ParentAlpha2")
                  .values(2, "ParentBravo", "ParentBravo2")
                  .values(3, "ParentCharlie", "ParentCharlie2")
                  .execute();

        dslContext.insertInto(parent_sec_table)
                  .columns(parent_sec_table.id, parent_sec_table.field1, parent_sec_table.field2, parent_sec_table.parent_id)
                  .values(1, "ParentSecondaryAlpha", "ParentSecondaryAlpha2", 1)
                  .values(2, "ParentSecondaryBravo", "ParentSecondaryBravo2", 2)
                  .values(3, "ParentSecondaryCharlie", "ParentSecondaryCharlie2", 3)
                  .execute();

        dslContext.insertInto(table)
                  .columns(table.type, table.id, table.field1, table.parent_id)
                  .values(1, 1, "Alpha", 1)
                  .values(1, 2, "Bravo", 1)
                  .values(2, 1, "Charlie", 2)
                  .values(3, 3, "Delta", 3)
                  .execute();

    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> dslContext.deleteFrom(table).execute());
    }

    @AfterClass
    public static void dropTables() {
        ALL_TABLES.forEach(table -> staticDSLContext.dropTableIfExists(table).execute());
    }

    @Test
    public void fetchByEqualsConditionWhereOneMatchesAndFieldOfConditionRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestEntityType.FIELD1.eq("Alpha"),
                                                            TestEntityType.ID, TestEntityType.FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.ID, 1),
                                  fieldValue(TestEntityType.FIELD1, "Alpha")));
    }

    @Test
    public void fetchByEqualsConditionWhereOneMatchesAndFieldOfConditionNotRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestEntityType.FIELD1.eq("Alpha"),
                                                            TestEntityType.ID, TestEntityType.TYPE);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.ID, 1),
                                  fieldValue(TestEntityType.TYPE, 1)));
    }

    @Test
    public void fetchByEqualsConditionWhereTwoMatch() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestEntityType.TYPE.eq(1),
                                                            TestEntityType.ID, TestEntityType.FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(2));

        final List<Entity> sortedEntities = entities.stream()
                                                    .sorted(comparing(entity -> entity.get(TestEntityType.ID)))
                                                    .collect(toList());

        assertThat(sortedEntities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.ID, 1),
                                  fieldValue(TestEntityType.FIELD1, "Alpha")));
        assertThat(sortedEntities.get(1),
                   hasFieldValues(fieldValue(TestEntityType.ID, 2),
                                  fieldValue(TestEntityType.FIELD1, "Bravo")));
    }

    @Test
    public void fetchByEqualsConditionWhereNoneMatch() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestEntityType.TYPE.eq(999),
                                                            TestEntityType.ID, TestEntityType.FIELD1);
        assertThat("Should not find entities for given condition: ",
                   entities, is(empty()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fetchByEqualsConditionWhereNoFieldsRequestedShouldThrowException() {
        entitiesFetcher.fetch(TestEntityType.INSTANCE,
                              TestEntityType.FIELD1.eq("Alpha"));
    }

    @Test
    public void fetchByEqualsConditionWhereConditionOnChildAndParentRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestEntityType.FIELD1.eq("Alpha"),
                                                            TestParentEntityType.FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestParentEntityType.FIELD1, "ParentAlpha")));
    }

    @Test
    public void fetchByEqualsConditionWhereConditionOnChildAndParentSecondaryRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestEntityType.FIELD1.eq("Alpha"),
                                                            TestParentEntityType.SECONDARY_FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestParentEntityType.SECONDARY_FIELD1, "ParentSecondaryAlpha")));
    }

    @Test
    public void fetchByEqualsConditionWhereConditionOnChildAndBothParentAndSecondaryRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestEntityType.FIELD1.eq("Charlie"),
                                                            TestParentEntityType.FIELD1, TestParentEntityType.SECONDARY_FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestParentEntityType.FIELD1, "ParentBravo"),
                                  fieldValue(TestParentEntityType.SECONDARY_FIELD1, "ParentSecondaryBravo")));
    }

    @Test
    public void fetchByEqualsConditionWhereConditionOnParentAndChildRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestParentEntityType.FIELD2.eq("ParentBravo2"),
                                                            TestEntityType.FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.FIELD1, "Charlie")));
    }

    @Test
    public void fetchByEqualsConditionWhereConditionOnParentAndParentRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestParentEntityType.FIELD2.eq("ParentBravo2"),
                                                            TestParentEntityType.FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestParentEntityType.FIELD1, "ParentBravo")));
    }

    @Test
    public void fetchByEqualsConditionWhereConditionOnParentAndParentSecondaryRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestParentEntityType.FIELD2.eq("ParentBravo2"),
                                                            TestParentEntityType.SECONDARY_FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestParentEntityType.SECONDARY_FIELD1, "ParentSecondaryBravo")));
    }

    @Test
    public void fetchByEqualsConditionWhereConditionOnParentAndBothChildAndParentSecondaryRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestParentEntityType.FIELD2.eq("ParentBravo2"),
                                                            TestEntityType.FIELD1, TestParentEntityType.SECONDARY_FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.FIELD1, "Charlie"),
                                  fieldValue(TestParentEntityType.SECONDARY_FIELD1, "ParentSecondaryBravo")));
    }

    @Test
    public void fetchByEqualsConditionWhereConditionOnParentSecondaryAndChildRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestParentEntityType.SECONDARY_FIELD2.eq("ParentSecondaryBravo2"),
                                                            TestEntityType.FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.FIELD1, "Charlie")));
    }

    @Test
    public void fetchByEqualsConditionWhereConditionOnParentSecondaryAndParentRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestParentEntityType.SECONDARY_FIELD2.eq("ParentSecondaryBravo2"),
                                                            TestParentEntityType.FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestParentEntityType.FIELD1, "ParentBravo")));
    }

    @Test
    public void fetchByEqualsConditionWhereConditionOnParentSecondaryAndParentSecondaryRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestParentEntityType.SECONDARY_FIELD2.eq("ParentSecondaryBravo2"),
                                                            TestParentEntityType.SECONDARY_FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestParentEntityType.SECONDARY_FIELD1, "ParentSecondaryBravo")));
    }

    @Test
    public void fetchByEqualsConditionWhereConditionOnParentSecondaryAndBothChildAndParentRequested() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestParentEntityType.SECONDARY_FIELD2.eq("ParentSecondaryBravo2"),
                                                            TestEntityType.FIELD1, TestParentEntityType.FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.FIELD1, "Charlie"),
                                  fieldValue(TestParentEntityType.FIELD1, "ParentBravo")));
    }

    @Test
    public void fetchByAndConditionWhereOneMatches() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestEntityType.ID.eq(1).and(TestEntityType.TYPE.eq(1)),
                                                            TestEntityType.FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.FIELD1, "Alpha")));
    }

    @Test
    public void fetchByAndConditionWhereNoneMatch() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestEntityType.ID.eq(1).and(TestEntityType.TYPE.eq(99)),
                                                            TestEntityType.FIELD1);
        assertThat("Should not find entities for given condition: ",
                   entities, is(empty()));
    }

    @Test
    public void fetchByOrConditionWhereOneMatches() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestEntityType.ID.eq(2).or(TestEntityType.TYPE.eq(99)),
                                                            TestEntityType.FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.FIELD1, "Bravo")));
    }

    @Test
    public void fetchByOrConditionWhereTwoMatch() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestEntityType.ID.eq(2).or(TestEntityType.TYPE.eq(3)),
                                                            TestEntityType.TYPE, TestEntityType.FIELD1);

        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(2));

        final List<Entity> sortedEntities = entities.stream()
                                                    .sorted(comparing(entity -> entity.get(TestEntityType.TYPE)))
                                                    .collect(toList());

        assertThat(sortedEntities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.FIELD1, "Bravo")));
        assertThat(sortedEntities.get(1),
                   hasFieldValues(fieldValue(TestEntityType.FIELD1, "Delta")));
    }

    @Test
    public void fetchByOrConditionWhereNoneMatch() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            TestEntityType.ID.eq(999).or(TestEntityType.TYPE.eq(999)),
                                                            TestEntityType.TYPE, TestEntityType.FIELD1);

        assertThat("Should not find entities for given condition: ",
                   entities, is(empty()));
    }

    @Test
    public void fetchByNotEqualsConditionWhereTwoMatch() {
        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                                                            not(TestEntityType.ID.eq(1)),
                                                            TestEntityType.TYPE, TestEntityType.FIELD1);

        final List<Entity> sortedEntities = entities.stream()
                                                    .sorted(comparing(entity -> entity.get(TestEntityType.TYPE)))
                                                    .collect(toList());

        assertThat(sortedEntities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.FIELD1, "Bravo")));
        assertThat(sortedEntities.get(1),
                   hasFieldValues(fieldValue(TestEntityType.FIELD1, "Delta")));
    }


    @Test
    public void fetchByUniqueKeys() {
        final PairUniqueKey<TestEntityType, Integer, Integer> uniqueKey= new PairUniqueKey<>(TestEntityType.ID, TestEntityType.TYPE);

        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                ImmutableList.of(uniqueKey.createValue(1, 1), uniqueKey.createValue(2, 1)),
                PLCondition.TrueCondition,
                TestEntityType.TYPE, TestEntityType.FIELD1);

        final List<Entity> sortedEntities = entities.stream()
                .sorted(comparing(entity -> entity.get(TestEntityType.TYPE)))
                .collect(toList());

        assertThat(sortedEntities.get(0),
                hasFieldValues(fieldValue(TestEntityType.FIELD1, "Alpha")));
        assertThat(sortedEntities.get(1),
                hasFieldValues(fieldValue(TestEntityType.FIELD1, "Bravo")));
    }

    @Test
    public void fetchByUniqueKeysAndCondition() {
        final PairUniqueKey<TestEntityType, Integer, Integer> uniqueKey= new PairUniqueKey<>(TestEntityType.ID, TestEntityType.TYPE);

        final List<Entity> entities = entitiesFetcher.fetch(TestEntityType.INSTANCE,
                ImmutableList.of(uniqueKey.createValue(1, 1), uniqueKey.createValue(2, 1)),
                not(TestEntityType.ID.eq(1)),
                TestEntityType.ID, TestEntityType.TYPE, TestEntityType.FIELD1);

        final List<Entity> sortedEntities = entities.stream()
                .sorted(comparing(entity -> entity.get(TestEntityType.TYPE)))
                .collect(toList());

        assertThat(sortedEntities.size(), is(1));
        assertThat(sortedEntities.get(0),
                hasFieldValues(fieldValue(TestEntityType.ID, 2), fieldValue(TestEntityType.FIELD1, "Bravo")));
    }

    private static class TestTable extends AbstractDataTable<TestTable> {
        private static final TestTable INSTANCE = new TestTable("test");

        private final TableField<Record, Integer> type = createPKField("type", SQLDataType.INTEGER);
        private final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        private final TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR.length(50));
        private final TableField<Record, Integer> parent_id = createFKField("parent_id", TestParentTable.INSTANCE.id);

        public TestTable(String name) {
            super(name);
        }

        public TestTable(TestTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public TestTable as(String alias) {
            return new TestTable(this, alias);
        }
    }

    private static class TestParentTable extends AbstractDataTable<TestParentTable> {
        private static final TestParentTable INSTANCE = new TestParentTable("testParent");

        private final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        private final TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR.length(50));
        private final TableField<Record, String> field2 = createField("field2", SQLDataType.VARCHAR.length(50));

        public TestParentTable(String name) {
            super(name);
        }

        public TestParentTable(TestParentTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public TestParentTable as(String alias) {
            return new TestParentTable(this, alias);
        }
    }

    private static class TestParentSecondaryTable extends AbstractDataTable<TestParentSecondaryTable> {
        private static final TestParentSecondaryTable INSTANCE = new TestParentSecondaryTable("testParentSecondary");

        private final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        private final TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR.length(50));
        private final TableField<Record, String> field2 = createField("field2", SQLDataType.VARCHAR.length(50));
        private final TableField<Record, Integer> parent_id = createFKField("parent_id", TestParentTable.INSTANCE.id);

        public TestParentSecondaryTable(String name) {
            super(name);
        }

        public TestParentSecondaryTable(TestParentSecondaryTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public TestParentSecondaryTable as(String alias) {
            return new TestParentSecondaryTable(this, alias);
        }
    }

    public static class TestEntityType extends AbstractEntityType<TestEntityType> {

        public static final TestEntityType INSTANCE = new TestEntityType();

        public static final EntityField<TestEntityType, Integer> ID = INSTANCE.field(table.id);
        public static final EntityField<TestEntityType, String> FIELD1 = INSTANCE.field(table.field1);
        public static final EntityField<TestEntityType, Integer> TYPE = INSTANCE.field(table.type);
        @Required(RELATION)
        public static final EntityField<TestEntityType, Integer> PARENT_ID = INSTANCE.field(table.parent_id);

        private TestEntityType() {
            super("test");
        }

        @Override
        public DataTable getPrimaryTable() {
            return table;
        }
    }

    public static class TestParentEntityType extends AbstractEntityType<TestParentEntityType> {

        public static final TestParentEntityType INSTANCE = new TestParentEntityType();

        public static final EntityField<TestParentEntityType, Integer> ID = INSTANCE.field(parent_table.id);
        public static final EntityField<TestParentEntityType, String> FIELD1 = INSTANCE.field(parent_table.field1);
        public static final EntityField<TestParentEntityType, String> FIELD2 = INSTANCE.field(parent_table.field2);
        public static final EntityField<TestParentEntityType, String> SECONDARY_FIELD1 = INSTANCE.field(parent_sec_table.field1);
        public static final EntityField<TestParentEntityType, String> SECONDARY_FIELD2 = INSTANCE.field(parent_sec_table.field2);

        private TestParentEntityType() {
            super("testParent");
        }

        @Override
        public DataTable getPrimaryTable() {
            return parent_table;
        }
    }
}