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

import static com.kenshoo.matcher.EntityHasFieldValuesMatcher.fieldValue;
import static com.kenshoo.matcher.EntityHasFieldValuesMatcher.hasFieldValues;
import static com.kenshoo.pl.entity.Feature.FindSecondaryTablesOfParents;
import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test that fields are properly fetched by the fetcher for a hierarchy of 2 levels with:
 * <ul>
 *     <li>child</li>
 *     <li>parent</li>
 *     <li>2 secondary tables of parent</li>
 * </ul>
 * See diagram below:
 * <pre>
 *
 * ---------- (1)   (1) --------------
 * |        |-----------| ParentSec1 |
 * |        |           --------------
 * | Parent |
 * |        | (1)   (1) --------------
 * |        |-----------| ParentSec2 |
 * ----------           --------------
 * (1) / \
 *      |
 *      |
 * (n)  |
 * ---------
 * | Child |
 * ---------
 *
 * </pre>
 */
public class ParentWithTwoSecondariesTest {

    private static final int CHILD_ID = 1;
    private static final int PARENT_ID = 2;

    private static final Set<DataTable> ALL_TABLES = ImmutableSet.of(
        ChildTable.INSTANCE,
        ParentTable.INSTANCE,
        ParentSec1Table.INSTANCE,
        ParentSec2Table.INSTANCE);

    private static final String PARENT_SEC_1_FIELD_1_VALUE = "sec1Field1";
    private static final String PARENT_SEC_1_FIELD_2_VALUE = "sec1Field2";
    private static final String PARENT_SEC_2_FIELD_1_VALUE = "sec2Field1";
    private static final String PARENT_SEC_2_FIELD_2_VALUE = "sec2Field2";

    private EntitiesFetcher entitiesFetcher;

    private DSLContext dslContext;

    @Before
    public void setUp() {

        dslContext = TestJooqConfig.create();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(ParentTable.INSTANCE)
                  .set(ParentTable.INSTANCE.id, PARENT_ID)
                  .execute();

        dslContext.insertInto(ParentSec1Table.INSTANCE)
                  .set(ParentSec1Table.INSTANCE.parent_id, PARENT_ID)
                  .set(ParentSec1Table.INSTANCE.field_1, PARENT_SEC_1_FIELD_1_VALUE)
                  .set(ParentSec1Table.INSTANCE.field_2, PARENT_SEC_1_FIELD_2_VALUE)
                  .execute();

        dslContext.insertInto(ParentSec2Table.INSTANCE)
                  .set(ParentSec2Table.INSTANCE.parent_id, PARENT_ID)
                  .set(ParentSec2Table.INSTANCE.field_1, PARENT_SEC_2_FIELD_1_VALUE)
                  .set(ParentSec2Table.INSTANCE.field_2, PARENT_SEC_2_FIELD_2_VALUE)
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
    public void fetchFieldsOfBothSecondaries() {

        final Set<EntityField<?, String>> fieldsToFetch = ImmutableSet.of(ParentEntityType.SEC_1_FIELD_1,
                                                                          ParentEntityType.SEC_1_FIELD_2,
                                                                          ParentEntityType.SEC_2_FIELD_1,
                                                                          ParentEntityType.SEC_2_FIELD_2);
        final ChildEntityType.Key keyToFetch = new ChildEntityType.Key(CHILD_ID);

        final Map<Identifier<ChildEntityType>, Entity> fetchedKeyToEntity =
            entitiesFetcher.fetchEntitiesByKeys(ChildEntityType.INSTANCE,
                                                ChildEntityType.Key.DEFINITION,
                                                singleton(keyToFetch),
                                                fieldsToFetch);

        assertThat(fetchedKeyToEntity.get(keyToFetch),
                   hasFieldValues(fieldValue(ParentEntityType.SEC_1_FIELD_1, PARENT_SEC_1_FIELD_1_VALUE),
                                  fieldValue(ParentEntityType.SEC_1_FIELD_2, PARENT_SEC_1_FIELD_2_VALUE),
                                  fieldValue(ParentEntityType.SEC_2_FIELD_1, PARENT_SEC_2_FIELD_1_VALUE),
                                  fieldValue(ParentEntityType.SEC_2_FIELD_2, PARENT_SEC_2_FIELD_2_VALUE)));
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

    private static class ParentSec1Table extends AbstractDataTable<ParentSec1Table> {

        static final ParentSec1Table INSTANCE = new ParentSec1Table("parent_sec_1");

        final TableField<Record, Integer> parent_id = createFKField("parent_id", ParentTable.INSTANCE.id);
        final TableField<Record, String> field_1 = createField("field_1", SQLDataType.VARCHAR(20));
        final TableField<Record, String> field_2 = createField("field_2", SQLDataType.VARCHAR(20));

        ParentSec1Table(String name) {
            super(name);
        }

        ParentSec1Table(ParentSec1Table aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public ParentSec1Table as(String alias) {
            return new ParentSec1Table(this, alias);
        }
    }

    private static class ParentSec2Table extends AbstractDataTable<ParentSec2Table> {

        static final ParentSec2Table INSTANCE = new ParentSec2Table("parent_sec_2");

        final TableField<Record, Integer> parent_id = createFKField("parent_id", ParentTable.INSTANCE.id);
        final TableField<Record, String> field_1 = createField("field_1", SQLDataType.VARCHAR(20));
        final TableField<Record, String> field_2 = createField("field_2", SQLDataType.VARCHAR(20));

        ParentSec2Table(String name) {
            super(name);
        }

        ParentSec2Table(ParentSec2Table aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public ParentSec2Table as(String alias) {
            return new ParentSec2Table(this, alias);
        }
    }

    public static class ChildEntityType extends AbstractEntityType<ChildEntityType> {
        static final ChildEntityType INSTANCE = new ChildEntityType();

        @Id
        public static final EntityField<ChildEntityType, Integer> ID = INSTANCE.field(ChildTable.INSTANCE.id);
        @Required(RELATION)
        public static final EntityField<ChildEntityType, Integer> PARENT_ID = INSTANCE.field(ChildTable.INSTANCE.parent_id);

        ChildEntityType() {
            super("child");
        }

        static class Key extends SingleUniqueKeyValue<ParentWithTwoSecondariesTest.ChildEntityType, Integer> {
            static final SingleUniqueKey<ParentWithTwoSecondariesTest.ChildEntityType, Integer> DEFINITION = new SingleUniqueKey<ParentWithTwoSecondariesTest.ChildEntityType, Integer>(ParentWithTwoSecondariesTest.ChildEntityType.ID) {
                @Override
                protected ParentWithTwoSecondariesTest.ChildEntityType.Key createValue(Integer value) {
                    return new ParentWithTwoSecondariesTest.ChildEntityType.Key(value);
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

    public static class ParentEntityType extends AbstractEntityType<ParentEntityType> {
        static final ParentEntityType INSTANCE = new ParentEntityType();

        @Id
        public static final EntityField<ParentEntityType, Integer> ID = INSTANCE.field(ParentTable.INSTANCE.id);
        public static final EntityField<ParentEntityType, String> FIELD_1 = INSTANCE.field(ParentTable.INSTANCE.field_1);
        public static final EntityField<ParentEntityType, String> SEC_1_FIELD_1 = INSTANCE.field(ParentSec1Table.INSTANCE.field_1);
        public static final EntityField<ParentEntityType, String> SEC_1_FIELD_2 = INSTANCE.field(ParentSec1Table.INSTANCE.field_2);
        public static final EntityField<ParentEntityType, String> SEC_2_FIELD_1 = INSTANCE.field(ParentSec2Table.INSTANCE.field_1);
        public static final EntityField<ParentEntityType, String> SEC_2_FIELD_2 = INSTANCE.field(ParentSec2Table.INSTANCE.field_2);

        ParentEntityType() {
            super("parent");
        }

        static class Key extends SingleUniqueKeyValue<ParentWithTwoSecondariesTest.ParentEntityType, Integer> {
            static final SingleUniqueKey<ParentWithTwoSecondariesTest.ParentEntityType, Integer> DEFINITION = new SingleUniqueKey<ParentWithTwoSecondariesTest.ParentEntityType, Integer>(ParentWithTwoSecondariesTest.ParentEntityType.ID) {
                @Override
                protected ParentWithTwoSecondariesTest.ParentEntityType.Key createValue(Integer value) {
                    return new ParentWithTwoSecondariesTest.ParentEntityType.Key(value);
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
