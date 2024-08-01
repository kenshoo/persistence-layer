package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.*;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.kenshoo.matcher.EntityHasFieldValuesMatcher.fieldValue;
import static com.kenshoo.matcher.EntityHasFieldValuesMatcher.hasFieldValues;
import static com.kenshoo.pl.entity.EntitiesFetcherByPLConditionWithVirtualPartitionTest.TestEntityType.*;
import static com.kenshoo.pl.entity.PLCondition.not;
import static java.util.Collections.singleton;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EntitiesFetcherByPLConditionWithVirtualPartitionTest {

    private static final TestTable table = TestTable.INSTANCE;
    private static final Set<DataTable> ALL_TABLES = singleton(table);
    public static final TestEntityType ENTITY_TYPE = TestEntityType.INSTANCE;

    private static DSLContext staticDSLContext;
    private static boolean tablesCreated;

    private DSLContext dslContext = TestJooqConfig.create();

    private EntitiesFetcher entitiesFetcher;

    @Before
    public void setup() {
        entitiesFetcher = new EntitiesFetcher(dslContext);
        staticDSLContext = dslContext;
        if (!tablesCreated) {
            ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));
            tablesCreated = true;
        }

        // The first two rows below are in the virtual partition (see table class below)
        dslContext.insertInto(table)
                  .columns(table.id1, table.id2, table.id3, table.field1)
                  .values(1, 1, 1, "1-1-1")
                  .values(1, 1, 2, "1-1-2")
                  .values(1, 2, 1, "1-2-1")
                  .values(1, 2, 2, "1-2-2")
                  .values(2, 1, 1, "2-1-1")
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
    public void fetchByEqualsConditionWhereInsidePartitionShouldReturnAll() {
        final List<CurrentEntityState> entities = entitiesFetcher.fetch(ENTITY_TYPE,
                                                            FIELD1.eq("1-1-1"),
                                                            FIELD1);

        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));
        assertThat(entities.get(0), hasFieldValues(fieldValue(FIELD1, "1-1-1")));
    }

    @Test
    public void fetchByEqualsConditionWhereOutsidePartitionShouldReturnEmpty() {
        final List<CurrentEntityState> entities = entitiesFetcher.fetch(ENTITY_TYPE,
                                                            ID2.eq(2),
                                                            FIELD1);

        assertThat(entities, is(empty()));
    }

    @Test
    public void fetchByEqualsConditionWherePartiallyIntersectsPartitionShouldReturnIntersection() {
        final List<CurrentEntityState> entities = entitiesFetcher.fetch(ENTITY_TYPE,
                                                            ID1.eq(1),
                                                            FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(2));

        final List<CurrentEntityState> sortedEntities = sortByField1(entities);

        assertThat(sortedEntities.get(0), hasFieldValues(fieldValue(FIELD1, "1-1-1")));
        assertThat(sortedEntities.get(1), hasFieldValues(fieldValue(FIELD1, "1-1-2")));
    }

    @Test
    public void fetchByAndConditionWhereInsidePartitionShouldReturnAll() {
        final List<CurrentEntityState> entities = entitiesFetcher.fetch(ENTITY_TYPE,
                                                            ID1.eq(1).and(ID2.eq(1)),
                                                            FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(2));

        final List<CurrentEntityState> sortedEntities = sortByField1(entities);

        assertThat(sortedEntities.get(0), hasFieldValues(fieldValue(FIELD1, "1-1-1")));
        assertThat(sortedEntities.get(1), hasFieldValues(fieldValue(FIELD1, "1-1-2")));
    }

    @Test
    public void fetchByAndConditionWhereOutsidePartitionShouldReturnEmpty() {
        final List<CurrentEntityState> entities = entitiesFetcher.fetch(ENTITY_TYPE,
                                                            ID1.eq(2).and(ID2.eq(1)),
                                                            FIELD1);
        assertThat(entities, is(empty()));
    }

    @Test
    public void fetchByAndConditionWherePartiallyIntersectsPartitionShouldReturnIntersection() {
        final List<CurrentEntityState> entities = entitiesFetcher.fetch(ENTITY_TYPE,
                                                            ID2.eq(1).and(ID3.eq(1)),
                                                            FIELD1);
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));
        assertThat(entities.get(0), hasFieldValues(fieldValue(FIELD1, "1-1-1")));
    }

    @Test
    public void fetchByOrConditionWherePartiallyIntersectsPartitionShouldReturnIntersection() {
        final List<CurrentEntityState> entities = entitiesFetcher.fetch(ENTITY_TYPE,
                                                            ID1.eq(1).or(ID3.eq(1)),
                                                            FIELD1);

        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(2));

        final List<CurrentEntityState> sortedEntities = sortByField1(entities);

        assertThat(sortedEntities.get(0), hasFieldValues(fieldValue(FIELD1, "1-1-1")));
        assertThat(sortedEntities.get(1), hasFieldValues(fieldValue(FIELD1, "1-1-2")));
    }

    @Test
    public void fetchByOrConditionWhereOutsidePartitionShouldReturnEmpty() {
        final List<CurrentEntityState> entities = entitiesFetcher.fetch(ENTITY_TYPE,
                                                            ID1.eq(2).or(FIELD1.eq("1-2-1")),
                                                            FIELD1);

        assertThat(entities, is(empty()));
    }

    @Test
    public void fetchByNotEqualsConditionWherePartiallyIntersectsPartitionShouldReturnIntersection() {
        final List<CurrentEntityState> entities = entitiesFetcher.fetch(ENTITY_TYPE,
                                                            not(FIELD1.eq("1-1-1")),
                                                            FIELD1);

        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));
        assertThat(entities.get(0), hasFieldValues(fieldValue(FIELD1, "1-1-2")));
    }

    private List<CurrentEntityState> sortByField1(List<CurrentEntityState> entities) {
        return entities.stream()
                       .sorted(comparing(entity -> entity.get(FIELD1)))
                       .collect(toList());
    }

    private static class TestTable extends AbstractDataTable<TestTable> {
        private static final TestTable INSTANCE = new TestTable("test");

        private final TableField<Record, Integer> id1 = createPKField("id1", SQLDataType.INTEGER.identity(true));
        private final TableField<Record, Integer> id2 = createPKField("id2", SQLDataType.INTEGER.identity(true));
        private final TableField<Record, Integer> id3 = createPKField("id3", SQLDataType.INTEGER.identity(true));
        private final TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR.length(50));

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

        @Override
        public Collection<FieldAndValue<?>> getVirtualPartition() {
            return ImmutableList.of(new FieldAndValue<>(id1, 1),
                                    new FieldAndValue<>(id2, 1));
        }
    }

    public static class TestEntityType extends AbstractEntityType<TestEntityType> {

        public static final TestEntityType INSTANCE = new TestEntityType();

        public static final EntityField<TestEntityType, Integer> ID1 = INSTANCE.field(table.id1);
        public static final EntityField<TestEntityType, Integer> ID2 = INSTANCE.field(table.id2);
        public static final EntityField<TestEntityType, Integer> ID3 = INSTANCE.field(table.id3);
        public static final EntityField<TestEntityType, String> FIELD1 = INSTANCE.field(table.field1);

        private TestEntityType() {
            super("test");
        }

        @Override
        public DataTable getPrimaryTable() {
            return table;
        }
    }
}