package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.AbstractDataTable;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.annotation.Required;
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
import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PLContextSelectTest {

    private static final TestTable table = TestTable.INSTANCE;
    private static final TestParentTable parent_table = TestParentTable.INSTANCE;
    private static final TestParentSecondaryTable parent_sec_table = TestParentSecondaryTable.INSTANCE;
    private static final Set<DataTable> ALL_TABLES = ImmutableSet.of(table,
                                                                     parent_table,
                                                                     parent_sec_table);

    private static DSLContext staticDSLContext;
    private static boolean tablesCreated;

    private DSLContext dslContext = TestJooqConfig.create();

    private PLContext plContext;

    @Before
    public void setup() {
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(always -> true)
            .build();

        staticDSLContext = dslContext;
        if (!tablesCreated) {
            ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));
            tablesCreated = true;
        }
        dslContext.insertInto(parent_table)
                  .columns(parent_table.id, parent_table.field1)
                  .values(1, "ParentAlpha")
                  .values(2, "ParentBravo")
                  .execute();

        dslContext.insertInto(parent_sec_table)
                  .columns(parent_sec_table.id, parent_sec_table.field1, parent_sec_table.parent_id)
                  .values(1, "ParentSecondaryAlpha", 1)
                  .values(2, "ParentSecondaryBravo", 2)
                  .execute();

        dslContext.insertInto(table)
                  .columns(table.id, table.field1, table.parent_id)
                  .values(1, "Alpha", 1)
                  .values(2, "Bravo", 1)
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
    public void selectFromSingleEntity() {
        final List<CurrentEntityState> entities = plContext.select(TestEntityType.ID, TestEntityType.FIELD1)
                                               .from(TestEntityType.INSTANCE)
                                               .where(TestEntityType.FIELD1.eq("Alpha"))
                                               .fetch();
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.ID, 1),
                                  fieldValue(TestEntityType.FIELD1, "Alpha")));
    }

    @Test
    public void selectIsNull() {
        final List<CurrentEntityState> entities = plContext.select(TestParentEntityType.FIELD1)
                .from(TestParentEntityType.INSTANCE)
                .where(TestParentEntityType.FIELD2.isNull())
                .fetch();
        assertThat("Incorrect number of entities fetched: ",
                entities.size(), is(2));

    }

    @Test
    public void selectFromChildAndParent() {
        final List<CurrentEntityState> entities = plContext.select(TestEntityType.ID, TestEntityType.FIELD1, TestParentEntityType.FIELD1)
                                               .from(TestEntityType.INSTANCE)
                                               .where(TestEntityType.FIELD1.eq("Alpha"))
                                               .fetch();
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.ID, 1),
                                  fieldValue(TestEntityType.FIELD1, "Alpha"),
                                  fieldValue(TestParentEntityType.FIELD1, "ParentAlpha")));
    }

    @Test
    public void selectFromChildAndParentAndParentSecondary() {
        final List<CurrentEntityState> entities = plContext.select(TestEntityType.ID,
                                                       TestEntityType.FIELD1,
                                                       TestParentEntityType.FIELD1,
                                                       TestParentEntityType.SECONDARY_FIELD1)
                                               .from(TestEntityType.INSTANCE)
                                               .where(TestEntityType.FIELD1.eq("Alpha"))
                                               .fetch();
        assertThat("Incorrect number of entities fetched: ",
                   entities.size(), is(1));

        assertThat(entities.get(0),
                   hasFieldValues(fieldValue(TestEntityType.ID, 1),
                                  fieldValue(TestEntityType.FIELD1, "Alpha"),
                                  fieldValue(TestParentEntityType.FIELD1, "ParentAlpha"),
                                  fieldValue(TestParentEntityType.SECONDARY_FIELD1, "ParentSecondaryAlpha")));
    }

    @Test
    public void selectFromSingleEntityByKeys() {
        final SingleUniqueKey<TestEntityType, Integer> uniqueKey = new SingleUniqueKey<>(TestEntityType.ID);

        final List<CurrentEntityState> entities = plContext.select(TestEntityType.ID, TestEntityType.FIELD1)
                .from(TestEntityType.INSTANCE)
                .where(PLCondition.trueCondition())
                .fetchByKeys(ImmutableList.of(uniqueKey.createValue(1)));
        assertThat("Incorrect number of entities fetched: ",
                entities.size(), is(1));

        assertThat(entities.get(0),
                hasFieldValues(fieldValue(TestEntityType.ID, 1),
                        fieldValue(TestEntityType.FIELD1, "Alpha")));
    }

    @Test
    public void selectFromSingleEntityByParentKeys() {
        final SingleUniqueKey<TestParentEntityType, Integer> uniqueKey = new SingleUniqueKey<>(TestParentEntityType.ID);

        final List<CurrentEntityState> entities = plContext.select(TestEntityType.ID, TestEntityType.FIELD1)
                .from(TestEntityType.INSTANCE)
                .where(PLCondition.trueCondition())
                .fetchByKeys(ImmutableList.of(uniqueKey.createValue(1)));
        assertThat("Incorrect number of entities fetched: ",
                entities.size(), is(2));

        assertThat(entities.get(0),
                hasFieldValues(fieldValue(TestEntityType.ID, 1),
                        fieldValue(TestEntityType.FIELD1, "Alpha")));
        assertThat(entities.get(1),
                hasFieldValues(fieldValue(TestEntityType.ID, 2),
                        fieldValue(TestEntityType.FIELD1, "Bravo")));
    }

    @Test
    public void selectFromSingleEntityInClause() {
        final List<CurrentEntityState> entities = plContext.select(TestEntityType.ID, TestEntityType.FIELD1)
                .from(TestEntityType.INSTANCE)
                .where(TestEntityType.FIELD1.in("Alpha", "Bravo"))
                .fetch();
        assertThat("Incorrect number of entities fetched: ",
                entities.size(), is(2));

        assertThat(entities.get(0),
                hasFieldValues(fieldValue(TestEntityType.ID, 1),
                        fieldValue(TestEntityType.FIELD1, "Alpha")));
        assertThat(entities.get(1),
                hasFieldValues(fieldValue(TestEntityType.ID, 2),
                        fieldValue(TestEntityType.FIELD1, "Bravo")));
    }

    @Test
    public void selectFromSingleEntityInAndEqClause() {
        final List<CurrentEntityState> entities = plContext.select(TestEntityType.ID, TestEntityType.FIELD1)
                .from(TestEntityType.INSTANCE)
                .where(TestEntityType.FIELD1.in("Alpha", "Bravo").and(TestEntityType.ID.eq(2)))
                .fetch();
        assertThat("Incorrect number of entities fetched: ",
                entities.size(), is(1));

        assertThat(entities.get(0),
                hasFieldValues(fieldValue(TestEntityType.ID, 2),
                        fieldValue(TestEntityType.FIELD1, "Bravo")));
    }

    @Test
    public void selectFromChildAndParentInClause() {
        final List<CurrentEntityState> entities = plContext.select(TestEntityType.ID, TestEntityType.FIELD1, TestParentEntityType.FIELD1)
                .from(TestEntityType.INSTANCE)
                .where(TestEntityType.FIELD1.in("Alpha", "Bravo"))
                .fetch();
        assertThat("Incorrect number of entities fetched: ",
                entities.size(), is(2));

        assertThat(entities.get(0),
                hasFieldValues(fieldValue(TestEntityType.ID, 1),
                        fieldValue(TestEntityType.FIELD1, "Alpha"),
                        fieldValue(TestParentEntityType.FIELD1, "ParentAlpha")));

        assertThat(entities.get(1),
                hasFieldValues(fieldValue(TestEntityType.ID, 2),
                        fieldValue(TestEntityType.FIELD1, "Bravo"),
                        fieldValue(TestParentEntityType.FIELD1, "ParentAlpha")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void selectWithoutFieldsShouldThrowException() {
        plContext.select()
                 .from(TestEntityType.INSTANCE)
                 .where(TestEntityType.FIELD1.eq("Alpha"))
                 .fetch();
    }

    private static class TestTable extends AbstractDataTable<TestTable> {
        private static final TestTable INSTANCE = new TestTable("test");

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

        private TestParentEntityType() {
            super("testParent");
        }

        @Override
        public DataTable getPrimaryTable() {
            return parent_table;
        }
    }
}