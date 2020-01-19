package com.kenshoo.pl.secondaryOfParent;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.AbstractDataTable;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static com.kenshoo.pl.entity.Feature.FindSecondaryTablesOfParents;
import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;
import static com.kenshoo.pl.testutils.EntityTestUtils.assertFetchedEntity;
import static java.util.Collections.singleton;

/**
 * Test that fields are properly fetched by the fetcher for a hierarchy of 3 levels with 1 secondary for each of the upper levels.<br>
 * See diagram below:
 * <pre>
 *
 * ---------- (1)    (1) -------------
 * | Table2 |------------| Table2Sec |
 * ----------            -------------
 * (1) / \
 *      |
 *      |
 * (n)  |
 * ---------- (1)    (1) -------------
 * | Table1 |------------| Table1Sec |
 * ----------            -------------
 * (1) / \
 *      |
 *      |
 * (n)  |
 * ----------
 * | Table0 |
 * ----------
 *
 * </pre>
 */
public class ThreeLevelsWithSecondaryForEachOfTop2Test {

    private static final int ENTITY_0_ID = 10;
    private static final int ENTITY_1_ID = 11;
    private static final String ENTITY_1_SEC_FIELD_1 = "entity1SecField1";
    private static final int ENTITY_2_ID = 12;
    private static final String ENTITY_2_SEC_FIELD_1 = "entity2SecField1";

    private static final Set<DataTable> ALL_TABLES = ImmutableSet.of(
        Table0.INSTANCE,
        Table1.INSTANCE,
        Table1Sec.INSTANCE,
        Table2.INSTANCE,
        Table2Sec.INSTANCE);

    private EntitiesFetcher entitiesFetcher;

    private DSLContext dslContext;

    @Before
    public void setUp() {

        dslContext= TestJooqConfig.create();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(Table2.INSTANCE)
                  .set(Table2.INSTANCE.id, ENTITY_2_ID)
                  .execute();

        dslContext.insertInto(Table2Sec.INSTANCE)
                  .set(Table2Sec.INSTANCE.entity2_id, ENTITY_2_ID)
                  .set(Table2Sec.INSTANCE.field_1, ENTITY_2_SEC_FIELD_1)
                  .execute();

        dslContext.insertInto(Table1.INSTANCE)
                  .set(Table1.INSTANCE.id, ENTITY_1_ID)
                  .set(Table1.INSTANCE.entity2_id, ENTITY_2_ID)
                  .execute();

        dslContext.insertInto(Table1Sec.INSTANCE)
                  .set(Table1Sec.INSTANCE.entity1_id, ENTITY_1_ID)
                  .set(Table1Sec.INSTANCE.field_1, ENTITY_1_SEC_FIELD_1)
                  .execute();

        dslContext.insertInto(Table0.INSTANCE)
                  .set(Table0.INSTANCE.id, ENTITY_0_ID)
                  .set(Table0.INSTANCE.entity1_id, ENTITY_1_ID)
                  .execute();

        entitiesFetcher = new EntitiesFetcher(dslContext, new FeatureSet(FindSecondaryTablesOfParents));
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> dslContext.dropTableIfExists(table).execute());
    }

    @Test
    public void fetchFieldsFromBothSecondaryTables() {

        final Set<EntityField<?, String>> fieldsToFetch = ImmutableSet.of(EntityType1.ENTITY_1_SEC_FIELD_1,
                                                                          EntityType2.ENTITY_2_SEC_FIELD_1);
        final EntityType0.Key keyToFetch = new EntityType0.Key(ENTITY_0_ID);

        final Map<Identifier<EntityType0>, Entity> fetchedKeyToEntity =
            entitiesFetcher.fetchEntitiesByKeys(EntityType0.INSTANCE,
                                                EntityType0.Key.DEFINITION,
                                                singleton(keyToFetch),
                                                fieldsToFetch);

        assertFetchedEntity(fetchedKeyToEntity,
                            keyToFetch,
                            fieldsToFetch);
    }

    @Test
    public void fetchFieldsFromTable2AndBothSecondaryTables() {

        final Set<EntityField<?, String>> fieldsToFetch = ImmutableSet.of(EntityType1.ENTITY_1_SEC_FIELD_1,
                                                                          EntityType2.FIELD_1,
                                                                          EntityType2.ENTITY_2_SEC_FIELD_1);
        final EntityType0.Key keyToFetch = new EntityType0.Key(ENTITY_0_ID);

        final Map<Identifier<EntityType0>, Entity> fetchedKeyToEntity =
            entitiesFetcher.fetchEntitiesByKeys(EntityType0.INSTANCE,
                                                EntityType0.Key.DEFINITION,
                                                singleton(keyToFetch),
                                                fieldsToFetch);

        assertFetchedEntity(fetchedKeyToEntity,
                            keyToFetch,
                            fieldsToFetch);
    }

    private static class Table0 extends AbstractDataTable<Table0> {

        static final Table0 INSTANCE = new Table0("entity0");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, Integer> entity1_id = createFKField("entity1_id", Table1.INSTANCE.id);

        Table0(String name) {
            super(name);
        }

        Table0(Table0 aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public Table0 as(String alias) {
            return new Table0(this, alias);
        }
    }

    private static class Table1 extends AbstractDataTable<Table1> {

        static final Table1 INSTANCE = new Table1("entity1");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, Integer> entity2_id = createFKField("entity2_id", Table2.INSTANCE.id);

        Table1(String name) {
            super(name);
        }

        Table1(Table1 aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public Table1 as(String alias) {
            return new Table1(this, alias);
        }
    }

    private static class Table1Sec extends AbstractDataTable<Table1Sec> {

        static final Table1Sec INSTANCE = new Table1Sec("entity1sec");

        final TableField<Record, Integer> entity1_id = createFKField("entity1_id", Table1.INSTANCE.id);
        final TableField<Record, String> field_1 = createField("field_1", SQLDataType.VARCHAR(20));

        Table1Sec(String name) {
            super(name);
        }

        Table1Sec(Table1Sec aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public Table1Sec as(String alias) {
            return new Table1Sec(this, alias);
        }
    }

    private static class Table2 extends AbstractDataTable<Table2> {

        static final Table2 INSTANCE = new Table2("entity2");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, String> field_1 = createField("field_1", SQLDataType.VARCHAR(20));

        Table2(String name) {
            super(name);
        }

        Table2(Table2 aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public Table2 as(String alias) {
            return new Table2(this, alias);
        }
    }

    private static class Table2Sec extends AbstractDataTable<Table2Sec> {

        static final Table2Sec INSTANCE = new Table2Sec("entity2sec");

        final TableField<Record, Integer> entity2_id = createFKField("entity2_id", Table2.INSTANCE.id);
        final TableField<Record, String> field_1 = createField("field_1", SQLDataType.VARCHAR(20));

        Table2Sec(String name) {
            super(name);
        }

        Table2Sec(Table2Sec aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public Table2Sec as(String alias) {
            return new Table2Sec(this, alias);
        }
    }

    private static class EntityType0 extends AbstractEntityType<EntityType0> {
        static final EntityType0 INSTANCE = new EntityType0();

        @Id
        static final EntityField<EntityType0, Integer> ID = INSTANCE.field(Table0.INSTANCE.id);
        @Required(RELATION)
        static final EntityField<EntityType0, Integer> ENTITY1_ID = INSTANCE.field(Table0.INSTANCE.entity1_id);

        EntityType0() {
            super("entity0");
        }

        static class Key extends SingleUniqueKeyValue<EntityType0, Integer> {
            static final SingleUniqueKey<EntityType0, Integer> DEFINITION = new SingleUniqueKey<EntityType0, Integer>(EntityType0.ID) {
                @Override
                protected EntityType0.Key createValue(Integer value) {
                    return new EntityType0.Key(value);
                }
            };

            public Key(int id) {
                super(DEFINITION, id);
            }
        }

        @Override
        public DataTable getPrimaryTable() {
            return Table0.INSTANCE;
        }
    }

    private static class EntityType1 extends AbstractEntityType<EntityType1> {
        static final EntityType1 INSTANCE = new EntityType1();

        @Id
        static final EntityField<EntityType1, Integer> ID = INSTANCE.field(Table1.INSTANCE.id);
        @Required(RELATION)
        static final EntityField<EntityType1, Integer> ENTITY2_ID = INSTANCE.field(Table2.INSTANCE.id);
        static final EntityField<EntityType1, String> ENTITY_1_SEC_FIELD_1 = INSTANCE.field(Table1Sec.INSTANCE.field_1);

        EntityType1() {
            super("entity1");
        }

        static class Key extends SingleUniqueKeyValue<EntityType1, Integer> {
            static final SingleUniqueKey<EntityType1, Integer> DEFINITION = new SingleUniqueKey<EntityType1, Integer>(EntityType1.ID) {
                @Override
                protected EntityType1.Key createValue(Integer value) {
                    return new EntityType1.Key(value);
                }
            };

            public Key(int id) {
                super(DEFINITION, id);
            }
        }

        @Override
        public DataTable getPrimaryTable() {
            return Table1.INSTANCE;
        }
    }

    private static class EntityType2 extends AbstractEntityType<EntityType2> {
        static final EntityType2 INSTANCE = new EntityType2();

        @Id
        static final EntityField<EntityType2, Integer> ID = INSTANCE.field(Table2.INSTANCE.id);
        static final EntityField<EntityType2, String> FIELD_1 = INSTANCE.field(Table2.INSTANCE.field_1);
        static final EntityField<EntityType2, String> ENTITY_2_SEC_FIELD_1 = INSTANCE.field(Table2Sec.INSTANCE.field_1);

        public EntityType2() {
            super("entity2");
        }

        static class Key extends SingleUniqueKeyValue<EntityType2, Integer> {
            static final SingleUniqueKey<EntityType2, Integer> DEFINITION = new SingleUniqueKey<EntityType2, Integer>(EntityType2.ID) {
                @Override
                protected EntityType2.Key createValue(Integer value) {
                    return new EntityType2.Key(value);
                }
            };

            public Key(int id) {
                super(DEFINITION, id);
            }
        }

        @Override
        public DataTable getPrimaryTable() {
            return Table2.INSTANCE;
        }
    }

}
