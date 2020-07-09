package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.one2many.*;
import org.hamcrest.core.Is;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.kenshoo.pl.one2many.ChildEntity.FIELD_1;
import static com.kenshoo.pl.one2many.ChildEntity.ID;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

public class EntityFetcherByIdTest {

    private static final ParentTable parentTable = ParentTable.INSTANCE;
    private static final ChildTable childTable = ChildTable.INSTANCE;
    private static final OtherChildTable otherChildTable = OtherChildTable.INSTANCE;
    private static final GrandChildTable grandChildTable = GrandChildTable.INSTANCE;
    private static final OtherGrandChildTable otherGrandChildTable = OtherGrandChildTable.INSTANCE;
    private static final Set<DataTable> ALL_TABLES = ImmutableSet.of(parentTable,
            childTable,
            otherChildTable,
            grandChildTable,
            otherGrandChildTable);

    private static DSLContext staticDSLContext;
    private static boolean tablesCreated;

    private DSLContext dslContext = TestJooqConfig.create();

    private EntitiesFetcher entitiesFetcher;

    @Before
    public void setup() {
        entitiesFetcher = new EntitiesFetcher(dslContext, new FeatureSet(Feature.FetchMany));
        staticDSLContext = dslContext;
        if (!tablesCreated) {
            ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));
            tablesCreated = true;
        }
        dslContext.insertInto(parentTable)
                .columns(parentTable.id, parentTable.idInTarget, parentTable.name)
                .values(1, 111, "parent1")
                .values(2, 222, "parent2")
                .execute();

        dslContext.insertInto(childTable)
                .columns(childTable.id, childTable.parent_id, childTable.ordinal, childTable.field1)
                .values(1, 1, 1, "child1")
                .values(2, 1, 2, "child2")
                .values(3, 2, 3, "child3")
                .values(4, 2, 4, "child4")
                .execute();

        dslContext.insertInto(otherChildTable)
                .columns(otherChildTable.id, otherChildTable.parent_id, otherChildTable.name)
                .values(1, 1, "otherChild1")
                .values(2, 1, "otherChild2")
                .values(3, 2, "otherChild3")
                .values(4, 2, "otherChild4")
                .execute();

        dslContext.insertInto(grandChildTable)
                .columns(grandChildTable.child_id, grandChildTable.color)
                .values(1, "color1")
                .values(2, "color2")
                .values(3, "color3")
                .values(4, "color4")
                .execute();

        dslContext.insertInto(otherGrandChildTable)
                .columns(otherGrandChildTable.parent_id, otherGrandChildTable.child_id, otherGrandChildTable.name)
                .values(1, 1, "otherGrandChild1")
                .values(1, 1, "otherGrandChild2")
                .values(1, 2, "otherGrandChild1")
                .values(1, 2, "otherGrandChild2")
                .values(2, 3, "otherGrandChild1")
                .values(2, 4, "otherGrandChild1")
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


    /*
     * requested many child for parent
     *         -------------
     *         |  parent   |
     *         -------------
     *              /|\ (1)
     *              |
     *              |  (n)
     *         -------------
     *         |  child   |
     *         -------------
     */
    @Test
    public void when_fetching_field_of_manyToOne_relation_to_current_id_then_return_many_entities_as_expected() {

        final Identifier<ParentEntity> parentId = new ParentEntity.Key(1);

        final Map<Identifier<ParentEntity>, CurrentEntityState> idEntityMap = entitiesFetcher.fetchEntitiesByIds(ImmutableList.of(parentId),
                ID, FIELD_1);

        final List<FieldsValueMap<ChildEntity>> manyValues = idEntityMap.get(parentId).getMany(ChildEntity.INSTANCE);

        assertThat(valuesOfId(manyValues, ID, 1).get(FIELD_1), Is.is("child1"));
        assertThat(valuesOfId(manyValues, ID, 2).get(FIELD_1), Is.is("child2"));
    }

    /*
     * requested many otherChild for grandChild
     *                   -----------------------
     *                  |        parent        |
     *                  -----------------------
     *                   /|\ (1)           /|\ (1)
     *                   |                 |
     *                  |  (n)            |  (n)
     *      ------------------         -------------------
     *     |      child      |        |     otherChild   |
     *     ------------------         --------------------
     *          /|\ (1)
     *          |
     *         |  (n)
     *   ---------------
     *  | grandChild   |
     *  ---------------
     */
    @Test
    public void when_fetching_field_of_manyToOne_relation_to_parentId_then_return_many_entities_as_expected() {

        final Identifier<GrandChildEntity> grandChildId = new GrandChildEntity.Color("color1");

        final Map<Identifier<GrandChildEntity>, CurrentEntityState> idEntityMap = entitiesFetcher.fetchEntitiesByIds(ImmutableList.of(grandChildId),
                OtherChildEntity.ID, OtherChildEntity.NAME);

        final List<FieldsValueMap<OtherChildEntity>> manyValues = idEntityMap.get(grandChildId).getMany(OtherChildEntity.INSTANCE);

        assertThat(valuesOfId(manyValues, OtherChildEntity.ID, 1).get(OtherChildEntity.NAME), Is.is("otherChild1"));
        assertThat(valuesOfId(manyValues, OtherChildEntity.ID, 2).get(OtherChildEntity.NAME), Is.is("otherChild2"));
    }

    /*
     * requested single child for otherGrandChild (and not fetch a many for parent)
     *                   -----------------------------
     *                  |           parent           |
     *                  -----------------------------
     *                     /|\ (1)          /|\ (1)
     *                     |                |
     *                    |  (n)           |
     *         ------------------         |
     *        |      child      |        |
     *        ------------------        |
     *                   /|\ (1)       |
     *                   |            |
     *                  |  (n)       |  (n)
     *            -------------------------------
     *           |        otherGrandChild       |
     *           -------------------------------
     */
    @Test
    public void dont_fetch_as_a_secondary_table_of_your_parent_if_you_can_fetch_it_directly_from_another_parent() {

        final Identifier<OtherGrandChildEntity> grandChildId = new OtherGrandChildEntity.ChildIdAndName(1, "otherGrandChild1");
        final Map<Identifier<OtherGrandChildEntity>, CurrentEntityState> idEntityMap = entitiesFetcher.fetchEntitiesByIds(ImmutableList.of(grandChildId), ChildEntity.ID);

        final CurrentEntityState entity = idEntityMap.get(grandChildId);

        assertThat(entity.get(ChildEntity.ID), Is.is(1));
        assertThat(entity.getMany(ChildEntity.INSTANCE), empty());
    }

    private <E extends EntityType<E>> FieldsValueMap<E> valuesOfId(List<FieldsValueMap<E>> manyValues, EntityField<E,?> fieldId, Integer id) {
        return manyValues.stream().filter(fieldsValueMap -> id.equals(fieldsValueMap.get(fieldId))).findFirst().get();
    }
}