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
 * Test that fields are properly fetched by the fetcher for a hierarchy of 2 levels with:
 * <ul>
 *     <li>child</li>
 *     <li>parent</li>
 *     <li>secondary table of parent</li>
 * </ul>
 * See diagrams below. Straight arrows are relations between <b>tables</b> and curvy arrows are between <b>entity types</b>:
 * <pre>
 *
 * ---------------------------
 * |        Parent           |~~~~~~~~
 * ---------------------------       )
 *     / \  / \   / \                (
 *      |    )     |                \ /
 *      |    )     |          ---------------
 *      |    (     -----------|  Secondary  |
 *      |    )                ---------------
 *      |    (
 *   -------------
 *   |   Child   |
 *   -------------
 *
 * </pre>
 */
public class SecondaryOfParentFetcherHierarchy2Test {

    private static final int CHILD_ID = 1;
    private static final int PARENT_ID = 2;
    private static final String SEC_FIELD_1_VALUE = "secField1";
    private static final String SEC_FIELD_2_VALUE = "secField2";

    private static final Set<DataTable> ALL_TABLES = ImmutableSet.of(
        ChildTable.INSTANCE,
        ParentTable.INSTANCE,
        SecondaryOfParentTable.INSTANCE);

    private EntitiesFetcher entitiesFetcher;

    private DSLContext dslContext;

    @Before
    public void setUp() {

        dslContext = TestJooqConfig.create();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(ParentTable.INSTANCE)
                  .set(ParentTable.INSTANCE.id, PARENT_ID)
                  .execute();

        dslContext.insertInto(SecondaryOfParentTable.INSTANCE)
                  .set(SecondaryOfParentTable.INSTANCE.parent_id, PARENT_ID)
                  .set(SecondaryOfParentTable.INSTANCE.sec_field_1, SEC_FIELD_1_VALUE)
                  .set(SecondaryOfParentTable.INSTANCE.sec_field_2, SEC_FIELD_2_VALUE)
                  .execute();

        dslContext.insertInto(ChildTable.INSTANCE)
                  .set(ChildTable.INSTANCE.id, CHILD_ID)
                  .set(ChildTable.INSTANCE.parent_id, PARENT_ID)
                  .execute();

        entitiesFetcher = new EntitiesFetcher(dslContext, new FeatureSet(FindSecondaryTablesOfParents));
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> dslContext.dropTableIfExists(table).execute());
    }

    @Test
    public void fetchFieldsOfSecondaryOnly() {

        final Set<EntityField<?, String>> fieldsToFetch = ImmutableSet.of(ParentEntityType.SEC_FIELD_1,
                                                                          ParentEntityType.SEC_FIELD_2);
        final ChildEntityType.Key keyToFetch = new ChildEntityType.Key(CHILD_ID);

        final Map<Identifier<ChildEntityType>, Entity> fetchedKeyToEntity =
            entitiesFetcher.fetchEntitiesByKeys(ChildEntityType.INSTANCE,
                                                ChildEntityType.Key.DEFINITION,
                                                singleton(keyToFetch),
                                                fieldsToFetch);

        assertFetchedEntity(fetchedKeyToEntity,
                            keyToFetch,
                            fieldsToFetch);
    }

    @Test
    public void fetchFieldsOfParentAndSecondary() {

        final Set<EntityField<?, String>> fieldsToFetch = ImmutableSet.of(ParentEntityType.FIELD_1,
                                                                          ParentEntityType.SEC_FIELD_1,
                                                                          ParentEntityType.SEC_FIELD_2);
        final ChildEntityType.Key keyToFetch = new ChildEntityType.Key(CHILD_ID);

        final Map<Identifier<ChildEntityType>, Entity> fetchedKeyToEntity =
            entitiesFetcher.fetchEntitiesByKeys(ChildEntityType.INSTANCE,
                                                ChildEntityType.Key.DEFINITION,
                                                singleton(keyToFetch),
                                                fieldsToFetch);

        assertFetchedEntity(fetchedKeyToEntity,
                            keyToFetch,
                            fieldsToFetch);
    }

    private static class ChildTable extends AbstractDataTable<ChildTable> {

        static final ChildTable INSTANCE = new ChildTable("child");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, Integer> parent_id = createFKField("parent_id", ParentTable.INSTANCE.id);

        ChildTable(String name) {
            super(name);
        }

        ChildTable(ChildTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public ChildTable as(String alias) {
            return new ChildTable(this, alias);
        }
    }

    private static class ParentTable extends AbstractDataTable<ParentTable> {

        static final ParentTable INSTANCE = new ParentTable("parent");

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, String> field_1 = createField("field_1", SQLDataType.VARCHAR(20));

        ParentTable(String name) {
            super(name);
        }

        ParentTable(ParentTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public ParentTable as(String alias) {
            return new ParentTable(this, alias);
        }
    }

    private static class SecondaryOfParentTable extends AbstractDataTable<SecondaryOfParentTable> {

        static final SecondaryOfParentTable INSTANCE = new SecondaryOfParentTable("secondary_of_parent");

        final TableField<Record, Integer> parent_id = createFKField("parent_id", ParentTable.INSTANCE.id);
        final TableField<Record, String> sec_field_1 = createField("sec_field_1", SQLDataType.VARCHAR(20));
        final TableField<Record, String> sec_field_2 = createField("sec_field_2", SQLDataType.VARCHAR(20));

        SecondaryOfParentTable(String name) {
            super(name);
        }

        SecondaryOfParentTable(SecondaryOfParentTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public SecondaryOfParentTable as(String alias) {
            return new SecondaryOfParentTable(this, alias);
        }
    }

    private static class ChildEntityType extends AbstractEntityType<ChildEntityType> {
        static final ChildEntityType INSTANCE = new ChildEntityType();

        @Id
        static final EntityField<ChildEntityType, Integer> ID = INSTANCE.field(ChildTable.INSTANCE.id);
        @Required(RELATION)
        static final EntityField<ChildEntityType, Integer> PARENT_ID = INSTANCE.field(ChildTable.INSTANCE.parent_id);

        ChildEntityType() {
            super("child");
        }

        static class Key extends SingleUniqueKeyValue<ChildEntityType, Integer> {
            static final SingleUniqueKey<ChildEntityType, Integer> DEFINITION = new SingleUniqueKey<ChildEntityType, Integer>(ChildEntityType.ID) {
                @Override
                protected ChildEntityType.Key createValue(Integer value) {
                    return new ChildEntityType.Key(value);
                }
            };

            Key(int id) {
                super(DEFINITION, id);
            }
        }

        @Override
        public DataTable getPrimaryTable() {
            return ChildTable.INSTANCE;
        }
    }

    private static class ParentEntityType extends AbstractEntityType<ParentEntityType> {
        static final ParentEntityType INSTANCE = new ParentEntityType();

        @Id
        static final EntityField<ParentEntityType, Integer> ID = INSTANCE.field(ParentTable.INSTANCE.id);
        static final EntityField<ParentEntityType, String> FIELD_1 = INSTANCE.field(ParentTable.INSTANCE.field_1);
        static final EntityField<ParentEntityType, String> SEC_FIELD_1 = INSTANCE.field(SecondaryOfParentTable.INSTANCE.sec_field_1);
        static final EntityField<ParentEntityType, String> SEC_FIELD_2 = INSTANCE.field(SecondaryOfParentTable.INSTANCE.sec_field_2);

        ParentEntityType() {
            super("parent");
        }

        static class Key extends SingleUniqueKeyValue<ParentEntityType, Integer> {
            static final SingleUniqueKey<ParentEntityType, Integer> DEFINITION = new SingleUniqueKey<ParentEntityType, Integer>(ParentEntityType.ID) {
                @Override
                protected ParentEntityType.Key createValue(Integer value) {
                    return new ParentEntityType.Key(value);
                }
            };

            Key(int id) {
                super(DEFINITION, id);
            }
        }

        @Override
        public DataTable getPrimaryTable() {
            return ParentTable.INSTANCE;
        }
    }
}
