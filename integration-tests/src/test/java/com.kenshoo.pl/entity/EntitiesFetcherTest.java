package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.*;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EntitiesFetcherTest {

    private static final Object[][] DATA = {
            {1, 1, "Alpha"},
            {1, 2, "Bravo"},
            {2, 1, "Charlie"},
            {3, 3, "Delta"},
    };

    private static DSLContext staticDSLContext;
    private static TestTable table;

    private DSLContext dslContext = TestJooqConfig.create();

    private EntitiesFetcher entitiesFetcher;

    @Before
    public void setup() {
        entitiesFetcher = new EntitiesFetcher(dslContext);
        staticDSLContext = dslContext;
        if (table == null) {
            String tableName = RandomStringUtils.randomAlphanumeric(15);
            table = new TestTable(tableName);
            DataTableUtils.createTable(dslContext, table);
        }
        DataTableUtils.populateTable(dslContext, table, DATA);
    }

    @After
    public void tearDown() {
        dslContext.deleteFrom(table).execute();
    }

    @AfterClass
    public static void dropTables() {
        staticDSLContext.dropTableIfExists(table).execute();
    }

    @Test
    public void fetchByKeys() {
        TestEntityType.Key key = new TestEntityType.Key(1);
        Map<TestEntityType.Key, TestEntity> entities = entitiesFetcher.fetchPartialEntities(TestEntityType.INSTANCE, ImmutableList.of(key), TestEntity.class);
        assertThat(entities.size(), is(1));
        assertThat(entities, hasKey(key));
        TestEntity entity = entities.get(key);
        assertThat(entity.getField1(), is("Alpha"));
    }

    @Test
    public void fetchByCondition() {
        List<TestEntity> entities = entitiesFetcher.fetchByCondition(TestEntityType.INSTANCE, table.id.eq(1), TestEntity.class);
        assertThat(entities.size(), is(1));
        TestEntity entity = entities.get(0);
        assertThat(entity.getField1(), is("Alpha"));
    }

    @Test
    public void fetchVirtualField() {
        TestEntityType.Key key = new TestEntityType.Key(1);
        Map<TestEntityType.Key, VirtualEntity> entities = entitiesFetcher.fetchPartialEntities(TestEntityType.INSTANCE, ImmutableList.of(key), VirtualEntity.class);
        assertThat(entities.size(), is(1));
        assertThat(entities, hasKey(key));
        VirtualEntity entity = entities.get(key);
        assertThat(entity.getVirtualField(), is("Alpha-1"));
    }

    private static class TestTable extends AbstractDataTable<TestTable> {

        private final TableField<Record, Integer> type = createPKField("type", SQLDataType.INTEGER);
        private final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
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
            return ImmutableList.<FieldAndValue<?>>of(new FieldAndValue<>(type, 1));
        }
    }

    public static class TestEntityType extends AbstractEntityType<TestEntityType> {

        public static final TestEntityType INSTANCE = new TestEntityType();

        public static final EntityField<TestEntityType, Integer> ID = INSTANCE.field(table.id);
        public static final EntityField<TestEntityType, String> FIELD1 = INSTANCE.field(table.field1);
        public static final EntityField<TestEntityType, Integer> TYPE = INSTANCE.field(table.type);
        public static final EntityField<TestEntityType, String> VIRTUAL_FIELD = INSTANCE.virtualField(FIELD1, TYPE, (field1, field2) -> field1 + "-" + field2, new CommonTypesStringConverter<>(String.class), Objects::equals);

        private TestEntityType() {
            super("test");
        }

        public static class Key extends SingleUniqueKeyValue<TestEntityType, Integer> {
            public static final SingleUniqueKey<TestEntityType, Integer> DEFINITION = new SingleUniqueKey<TestEntityType, Integer>(ID) {
                @Override
                protected SingleUniqueKeyValue<TestEntityType, Integer> createValue(Integer value) {
                    return new Key(value);
                }
            };

            public Key(int val) {
                super(DEFINITION, val);
            }
        }

        @Override
        public DataTable getPrimaryTable() {
            return table;
        }
    }

    public interface TestEntity extends PartialEntity {
        int getId();

        String getField1();
    }

    public interface VirtualEntity extends PartialEntity {
        String getVirtualField();
    }

}