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
import org.junit.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.kenshoo.pl.entity.Feature.FetchMany;
import static com.kenshoo.pl.one2many.ChildEntity.*;
import static org.junit.Assert.assertThat;

@Ignore
public class EntitiesFetcherByIdTest {

    private static final ParentTable parentTable = ParentTable.INSTANCE;
    private static final ChildTable childTable = ChildTable.INSTANCE;
    private static final GrandChildTable grandChildTable = GrandChildTable.INSTANCE;
    private static final OtherChildTable otherChildTable = OtherChildTable.INSTANCE;
    private static final Set<DataTable> ALL_TABLES = ImmutableSet.of(parentTable,
            childTable,
            grandChildTable,
            otherChildTable);

    private static DSLContext staticDSLContext;
    private static boolean tablesCreated;

    private DSLContext dslContext = TestJooqConfig.create();

    private EntitiesFetcher entitiesFetcher;

    @Before
    public void setup() {
        entitiesFetcher = new EntitiesFetcher(dslContext, new FeatureSet(FetchMany));
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

        dslContext.insertInto(grandChildTable)
                .columns(grandChildTable.child_id, grandChildTable.color)
                .values(1, "color1")
                .values(2, "color2")
                .values(3, "color3")
                .values(4, "color4")
                .execute();

        dslContext.insertInto(otherChildTable)
                .columns(otherChildTable.id, otherChildTable.parent_id, otherChildTable.name)
                .values(1, 1, "otherChild1")
                .values(2, 1, "otherChild2")
                .values(3, 2, "otherChild3")
                .values(4, 2, "otherChild4")
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

        Identifier<ParentEntity> parentId = new ParentEntity.Key(1);

        Map<Identifier<ParentEntity>, CurrentEntityState> idEntityMap = entitiesFetcher.fetchEntitiesByIds(ImmutableList.of(parentId),
                ID, FIELD_1);

        List<FieldsValueMap<ChildEntity>> manyValues = idEntityMap.get(parentId).getMany(INSTANCE);

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

        Identifier<GrandChildEntity> grandChildId = new GrandChildEntity.Color("color1");

        Map<Identifier<GrandChildEntity>, CurrentEntityState> idEntityMap = entitiesFetcher.fetchEntitiesByIds(ImmutableList.of(grandChildId),
                OtherChildEntity.ID, OtherChildEntity.NAME);

        List<FieldsValueMap<OtherChildEntity>> manyValues = idEntityMap.get(grandChildId).getMany(OtherChildEntity.INSTANCE);

        assertThat(valuesOfId(manyValues, OtherChildEntity.ID, 1).get(OtherChildEntity.NAME), Is.is("otherChild1"));
        assertThat(valuesOfId(manyValues, OtherChildEntity.ID, 2).get(OtherChildEntity.NAME), Is.is("otherChild2"));
    }

    private <E extends EntityType<E>> FieldsValueMap<E> valuesOfId(List<FieldsValueMap<E>> manyValues, EntityField<E,?> fieldId, Integer id) {
        return manyValues.stream().filter(fieldsValueMap -> id.equals(fieldsValueMap.get(fieldId))).findFirst().get();
    }
}