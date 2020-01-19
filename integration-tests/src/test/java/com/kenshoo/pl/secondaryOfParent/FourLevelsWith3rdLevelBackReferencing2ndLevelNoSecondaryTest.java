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
 * Test that fields are properly fetched by the fetcher for the following hierarchy:
 * <ul>
 * <li>A 4-level hierarchy of tables where none are secondary tables.</li>
 * <li>Level 2 (indexed from 0) has a back reference to a table from level 1</li>
 * </ul>
 * See diagram below:
 * <pre>
 *         -------------
 *         |  Table3   |
 *         -------------
 *              / \ (1)
 *               |
 *               |  (n)
 *         -------------
 *         |  Table2   |
 *         -------------
 * (n)        |  / \      (1)
 *      -------   |
 *      |         ------
 * (1) \ /             |  (n)
 * -----------    -----------
 * | Table11 |    | Table12 |
 * -----------    -----------
 * (1) / \           / \  (1)
 *      |             |
 *      ------   ------
 *           |   |
 *        -----------
 *        | Table0  |
 * (n)    -----------     (n)
 *
 * </pre>
 *
 */
public class FourLevelsWith3rdLevelBackReferencing2ndLevelNoSecondaryTest {

    private static final int ENTITY_0_ID = 1;
    private static final int ENTITY_11_ID = 2;
    private static final int ENTITY_12_ID = 3;
    private static final int ENTITY_2_ID = 4;
    private static final int ENTITY_3_ID = 5;

    private static final String ENTITY_2_NAME = "entity2Name";
    private static final String ENTITY_3_NAME = "entity3Name";

    private static final Set<DataTable> ALL_TABLES = ImmutableSet.of(
        Table0.INSTANCE,
        Table11.INSTANCE,
        Table12.INSTANCE,
        Table2.INSTANCE,
        Table3.INSTANCE);

    private EntitiesFetcher entitiesFetcher;

    private DSLContext dslContext;

    @Before
    public void setUp() {

        dslContext= TestJooqConfig.create();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(Table3.INSTANCE)
                  .set(Table3.INSTANCE.id, ENTITY_3_ID)
                  .set(Table3.INSTANCE.name, ENTITY_3_NAME)
                  .execute();

        dslContext.insertInto(Table2.INSTANCE)
                  .set(Table2.INSTANCE.id, ENTITY_2_ID)
                  .set(Table2.INSTANCE.name, ENTITY_2_NAME)
                  .set(Table2.INSTANCE.entity3_id, ENTITY_3_ID)
                  .set(Table2.INSTANCE.entity11_id, ENTITY_11_ID)
                  .execute();

        dslContext.insertInto(Table11.INSTANCE)
                  .set(Table11.INSTANCE.id, ENTITY_11_ID)
                  .execute();

        dslContext.insertInto(Table12.INSTANCE)
                  .set(Table12.INSTANCE.id, ENTITY_12_ID)
                  .set(Table12.INSTANCE.entity2_id, ENTITY_2_ID)
                  .execute();

        dslContext.insertInto(Table0.INSTANCE)
                  .set(Table0.INSTANCE.id, ENTITY_0_ID)
                  .set(Table0.INSTANCE.entity12_id, ENTITY_12_ID)
                  .set(Table0.INSTANCE.entity11_id, ENTITY_11_ID)
                  .execute();

        entitiesFetcher = new EntitiesFetcher(dslContext, new FeatureSet(FindSecondaryTablesOfParents));
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> dslContext.dropTableIfExists(table).execute());
    }

    @Test
    public void fetchFieldsFromEntities2And3() {

        final Set<EntityField<?, String>> fieldsToFetch = ImmutableSet.of(EntityType2.NAME,
                                                                          EntityType3.NAME);
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
        final TableField<Record, Integer> entity12_id = createFKField("entity12_id", Table12.INSTANCE.id);
        final TableField<Record, Integer> entity11_id = createFKField("entity11_id", Table11.INSTANCE.id);

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

    private static class Table11 extends AbstractDataTable<Table11> {

        static final Table11 INSTANCE = new Table11("entity11");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);

        Table11(String name) {
            super(name);
        }

        Table11(Table11 aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public Table11 as(String alias) {
            return new Table11(this, alias);
        }
    }

    private static class Table12 extends AbstractDataTable<Table12> {

        static final Table12 INSTANCE = new Table12("entity12");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, Integer> entity2_id = createFKField("entity2_id", Table2.INSTANCE.id);

        Table12(String name) {
            super(name);
        }

        Table12(Table12 aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public Table12 as(String alias) {
            return new Table12(this, alias);
        }
    }

    private static class Table2 extends AbstractDataTable<Table2> {

        static final Table2 INSTANCE = new Table2("entity2");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, String> name = createPKField("name", SQLDataType.VARCHAR(20));
        final TableField<Record, Integer> entity3_id = createFKField("entity3_id", Table3.INSTANCE.id);
        final TableField<Record, Integer> entity11_id = createFKField("entity11_id", Table11.INSTANCE.id);

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

    private static class Table3 extends AbstractDataTable<Table3> {

        static final Table3 INSTANCE = new Table3("entity3");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(20));

        Table3(String name) {
            super(name);
        }

        Table3(Table3 aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public Table3 as(String alias) {
            return new Table3(this, alias);
        }
    }

    private static class EntityType0 extends AbstractEntityType<EntityType0> {
        static final EntityType0 INSTANCE = new EntityType0();

        @Id
        static final EntityField<EntityType0, Integer> ID = INSTANCE.field(Table0.INSTANCE.id);
        @Required(RELATION)
        static final EntityField<EntityType0, Integer> ENTITY12_ID = INSTANCE.field(Table0.INSTANCE.entity12_id);
        @Required(RELATION)
        static final EntityField<EntityType0, Integer> ENTITY11_ID = INSTANCE.field(Table0.INSTANCE.entity11_id);

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

    private static class EntityType2 extends AbstractEntityType<EntityType2> {
        static final EntityType2 INSTANCE = new EntityType2();

        @Id
        static final EntityField<EntityType2, Integer> ID = INSTANCE.field(Table2.INSTANCE.id);
        static final EntityField<EntityType2, String> NAME = INSTANCE.field(Table2.INSTANCE.name);
        @Required(RELATION)
        static final EntityField<EntityType2, Integer> ENTITY3_ID = INSTANCE.field(Table2.INSTANCE.entity3_id);
        @Required(RELATION)
        static final EntityField<EntityType2, Integer> ENTITY2_ID = INSTANCE.field(Table2.INSTANCE.entity11_id);

        EntityType2() {
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

    private static class EntityType3 extends AbstractEntityType<EntityType3> {
        static final EntityType3 INSTANCE = new EntityType3();

        @Id
        static final EntityField<EntityType3, Integer> ID = INSTANCE.field(Table3.INSTANCE.id);
        static final EntityField<EntityType3, String> NAME = INSTANCE.field(Table3.INSTANCE.name);

        public EntityType3() {
            super("entity3");
        }

        static class Key extends SingleUniqueKeyValue<EntityType3, Integer> {
            static final SingleUniqueKey<EntityType3, Integer> DEFINITION = new SingleUniqueKey<EntityType3, Integer>(EntityType3.ID) {
                @Override
                protected EntityType3.Key createValue(Integer value) {
                    return new EntityType3.Key(value);
                }
            };

            public Key(int id) {
                super(DEFINITION, id);
            }
        }

        @Override
        public DataTable getPrimaryTable() {
            return Table3.INSTANCE;
        }
    }

}
