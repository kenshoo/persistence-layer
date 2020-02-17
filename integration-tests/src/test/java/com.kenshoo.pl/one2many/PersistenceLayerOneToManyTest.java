package com.kenshoo.pl.one2many;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.FluidPersistenceCmdBuilder;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;
import com.kenshoo.pl.entity.spi.ChangeValidator;
import com.kenshoo.pl.entity.spi.FieldComplexValidator;
import com.kenshoo.pl.entity.spi.FieldValidator;
import com.kenshoo.pl.entity.spi.FieldValueSupplier;
import com.kenshoo.pl.entity.spi.NotSuppliedException;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import com.kenshoo.pl.entity.spi.ValidationException;
import com.kenshoo.pl.entity.spi.helpers.EntityChangeCompositeValidator;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.jooq.lambda.Seq;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.kenshoo.pl.FluidPersistenceCmdBuilder.fluid;
import static com.kenshoo.pl.entity.SupportedChangeOperation.CREATE_AND_UPDATE;
import static com.kenshoo.pl.one2many.ChildEntity.FIELD_1;
import static com.kenshoo.pl.one2many.ChildEntity.ORDINAL;
import static com.kenshoo.pl.one2many.ParentEntity.ID_IN_TARGET;
import static com.kenshoo.pl.one2many.ParentEntity.NAME;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.jooq.lambda.Seq.seq;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PersistenceLayerOneToManyTest {

    private static TablesSetup tablesSetup = new TablesSetup();

    private final static ParentTable PARENT = ParentTable.INSTANCE;
    private final static ChildTable CHILD = ChildTable.INSTANCE;
    private final static GrandChildTable GRAND_CHILD = GrandChildTable.INSTANCE;

    private final IdGenerator idGenerator = new IdGenerator();

    private DSLContext jooq = TestJooqConfig.create();

    private PLContext plContext;

    private PersistenceLayer<ParentEntity, ParentEntity.Key> persistenceLayer;

    private final DeletionOfOther<ChildEntity> deletionOfOther = new DeletionOfOther<>(ChildEntity.INSTANCE);

    @Before
    public void setupTables() {
        persistenceLayer = new PersistenceLayer<>(jooq);
        plContext = new PLContext.Builder(jooq).build();

        if (tablesSetup.alreadyCreated) {
            return;
        }
        tablesSetup.alreadyCreated = true;
        tablesSetup.staticDSLContext = jooq;
        DataTableUtils.createTable(jooq, PARENT);
        DataTableUtils.createTable(jooq, CHILD);
        DataTableUtils.createTable(jooq, GRAND_CHILD);
        jooq.alterTable(PARENT).add(DSL.constraint("unique_id").unique(PARENT.id)).execute();
        jooq.alterTable(PARENT).add(DSL.constraint("unique_key").unique(PARENT.idInTarget)).execute();
        jooq.alterTable(CHILD).add(DSL.constraint("unique_parent_and_ordinal").unique(CHILD.ordinal, CHILD.parent_id)).execute();
    }


    @After
    public void clearTables() {
        jooq.deleteFrom(PARENT).execute();
        jooq.deleteFrom(CHILD).execute();
        jooq.deleteFrom(GRAND_CHILD).execute();
    }

    @AfterClass
    public static void dropTables() {
        tablesSetup.staticDSLContext.dropTableIfExists(PARENT).execute();
        tablesSetup.staticDSLContext.dropTableIfExists(CHILD).execute();
    }

    @Test
    public void update_parent_without_children_using_different_parent_key() {

        insert(newParent()
                .with(ParentEntity.NAME, "old name")
                .with(ParentEntity.ID_IN_TARGET, 11)
        );

        update(existingParentWithUniqueKey(11)
                .with(ParentEntity.NAME, "new name")
        );

        assertThat(parentNamesInDB(), containsInAnyOrder("new name"));
    }

    @Test
    public void create_parent_with_2_children() {

        insert(newParent()
                .with(upsertChild(0).with(FIELD_1, "child1"))
                .with(upsertChild(8).with(FIELD_1, "child2"))
        );

        List<ChildPojo> children = childrenInDb();

        assertThat(children, not(empty()));

        assertThat(children.get(0).parentId, is(generatedId(0)));
        assertThat(children.get(0).ordinal, is(0));
        assertThat(children.get(0).field1, is("child1"));

        assertThat(children.get(1).parentId, is(generatedId(0)));
        assertThat(children.get(1).ordinal, is(8));
        assertThat(children.get(1).field1, is("child2"));
    }

    @Test
    public void update_child() {

        insert(newParent()
                .with(upsertChild(1).with(FIELD_1, "child1"))
                .with(upsertChild(2).with(FIELD_1, "child2"))
                .with(upsertChild(3).with(FIELD_1, "child3"))
        );

        update(existingParentWithId(generatedId(0))
                .with(updateChild(2).with(FIELD_1, "child2_UPDATED!"))
        );

        List<String> childsFromDB = seq(childrenInDb()).map(child -> child.field1).collect(toList());

        assertThat(childsFromDB, containsInAnyOrder("child1", "child2_UPDATED!", "child3"));
    }

    @Test
    public void delete_only_some_of_the_children() {

        insert(newParent()
                .with(insertChild().with(ORDINAL, 1).with(FIELD_1, "child1"))
                .with(insertChild().with(ORDINAL, 2).with(FIELD_1, "child2"))
                .with(insertChild().with(ORDINAL, 3).with(FIELD_1, "child3"))
                .with(insertChild().with(ORDINAL, 4).with(FIELD_1, "child4"))
        );

        update(existingParentWithId(generatedId(0))
                .with(deleteChild(3))
                .with(deleteChild(4))
        );

        List<Integer> remainingChildren = seq(childrenInDb()).map(rec -> rec.ordinal).collect(toList());;

        assertThat(remainingChildren, containsInAnyOrder(1, 2));
    }

    @Test
    public void delete_children_of_only_one_parent() {

        insert(
                newParent().with(insertChild().with(ORDINAL, 1).with(FIELD_1, "child1")),
                newParent().with(insertChild().with(ORDINAL, 1).with(FIELD_1, "child2"))
        );

        update(existingParentWithId(generatedId(0)).with(deleteChild(1)));

        List<String> remainingChildren = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());;

        assertThat(remainingChildren, containsInAnyOrder("child2"));
    }

    @Test
    public void delete_and_update_and_add_children_in_one_command() {

        insert(newParent()
                .with(insertChild().with(ORDINAL, 1).with(FIELD_1, "child1"))
                .with(insertChild().with(ORDINAL, 2).with(FIELD_1, "child2"))
        );

        update(existingParentWithId(generatedId(0))
                .with(deleteChild(1))
                .with(upsertChild(2).with(FIELD_1,  "updated_child2"))
                .with(upsertChild(3).with(FIELD_1,  "new_child3"))
        );

        List<ChildPojo> children = childrenInDb();

        assertThat(children, hasSize(2));

        assertThat(children.get(0).parentId, is(generatedId(0)));
        assertThat(children.get(0).ordinal, is(2));
        assertThat(children.get(0).field1, is("updated_child2"));

        assertThat(children.get(1).parentId, is(generatedId(0)));
        assertThat(children.get(1).ordinal, is(3));
        assertThat(children.get(1).field1, is("new_child3"));
    }

    @Test
    public void parent_is_using_unique_key_children_dont_have() {

        insert(newParent()
                .with(ParentEntity.ID_IN_TARGET, 11)
                .with(insertChild().with(ORDINAL, 1).with(FIELD_1, "child1"))
                .with(insertChild().with(ORDINAL, 2).with(FIELD_1, "child2"))
        );

        update(existingParentWithUniqueKey(11)
                .with(deleteChild(1))
                .with(upsertChild(2).with(FIELD_1,  "updated_child2"))
                .with(upsertChild(3).with(FIELD_1,  "new_child3"))
        );

        List<ChildPojo> children = childrenInDb();

        assertThat(children, hasSize(2));

        assertThat(children.get(0).parentId, is(generatedId(0)));
        assertThat(children.get(0).ordinal, is(2));
        assertThat(children.get(0).field1, is("updated_child2"));

        assertThat(children.get(1).parentId, is(generatedId(0)));
        assertThat(children.get(1).ordinal, is(3));
        assertThat(children.get(1).field1, is("new_child3"));
    }

    @Test
    public void update_with_supplier_from_parent() {

        insert(newParent().with(NAME, "moshe")
                .with(insertChild().with(ORDINAL, 0).with(FIELD_1, "bla bla")));

        update(existingParentWithId(generatedId(0))
                .with(updateChild(0).with(FIELD_1, supplyFromField(ParentEntity.NAME, parentName -> "I'm the child of " + parentName)))
        );

        ChildPojo child = childrenInDb().get(0);

        assertThat(child.field1, is("I'm the child of moshe"));
    }

    @Test
    public void create_child_with_supplier_from_parent() {

        insert(newParent().with(NAME, "moshe"));

        update(existingParentWithId(generatedId(0))
                .with(insertChild()
                        .with(ORDINAL, 0)
                        .with(FIELD_1, supplyFromField(ParentEntity.NAME, parentName -> "I'm the child of " + parentName))
                )
        );

        ChildPojo child = childrenInDb().get(0);

        assertThat(child.field1, is("I'm the child of moshe"));
    }

    @Test
    public void update_with_supplier_from_itself() {

        insert(
                newParent().with(insertChild().with(ORDINAL, 0).with(FIELD_1, "one")),
                newParent().with(insertChild().with(ORDINAL, 0).with(FIELD_1, "two")),
                newParent().with(insertChild().with(ORDINAL, 0).with(FIELD_1, "three"))
        );

        update(existingParentWithId(generatedId(1))
                .with(updateChild(0).with(FIELD_1, supplyFromField(FIELD_1, v -> v + " + something")))
        );

        List<String> results = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());;

        assertThat(results, containsInAnyOrder("one", "two + something", "three"));
    }

    @Test
    public void update_with_enricher_from_itself() {

        insert(
                newParent().with(insertChild().with(ORDINAL, 0).with(FIELD_1, "one")),
                newParent().with(insertChild().with(ORDINAL, 0).with(FIELD_1, "two")),
                newParent().with(insertChild().with(ORDINAL, 0).with(FIELD_1, "three"))
        );

        update(parentFlow(childFlow(enrichWithValueFrom(FIELD_1, (cmd, previousValue) -> cmd.set(FIELD_1, previousValue + "_suffix")))),
                existingParentWithId(generatedId(0)).with(updateChild(0).with(FIELD_1, supplyFromField(FIELD_1, v -> "prefix_" + v))),
                existingParentWithId(generatedId(1)).with(updateChild(0).with(FIELD_1, supplyFromField(FIELD_1, v -> "prefix_" + v))),
                existingParentWithId(generatedId(2)).with(updateChild(0).with(FIELD_1, supplyFromField(FIELD_1, v -> "prefix_" + v)))
        );

        List<String> results = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());;

        assertThat(results, containsInAnyOrder("prefix_one_suffix", "prefix_two_suffix", "prefix_three_suffix"));
    }

    @Test
    public void dont_update_parent_if_child_is_invalid() {

        insert(
                newParent().with(ParentEntity.NAME, "parent1").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "one")),
                newParent().with(ParentEntity.NAME, "parent2").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "two"))
        );

        UpdateResult<ParentEntity, ParentEntity.Key> results = update(parentFlow(childFlow(field1ShouldNotBe("INVALID"))),
                existingParentWithId(generatedId(0)).with(ParentEntity.NAME, "parent1_new").with(updateChild(0).with(FIELD_1, "INVALID")),
                existingParentWithId(generatedId(1)).with(ParentEntity.NAME, "parent2_new").with(updateChild(0).with(FIELD_1, "valid child"))
        );

        List<String> children = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());;
        assertThat(children, contains("one", "valid child"));

        assertThat(parentNamesInDB(), containsInAnyOrder("parent1", "parent2_new"));

        assertFalse(first(results).isSuccess());
        assertThat(first(results).getErrors(), hasSize(1));
        assertTrue(second(results).isSuccess());
    }

    private EntityChangeResult<ParentEntity, ParentEntity.Key, ?> first(ChangeResult<ParentEntity, ParentEntity.Key, ?> results) {
        return results.iterator().next();
    }

    private EntityChangeResult<ParentEntity, ParentEntity.Key, ?> second(ChangeResult<ParentEntity, ParentEntity.Key, ?> results) {
        return Iterators.get(results.iterator(), 1);
    }

    @Test
    public void dont_update_child_for_parent1() {

        insert(
                newParent().with(ParentEntity.NAME, "parent1").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "one")),
                newParent().with(ParentEntity.NAME, "parent2").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "two"))
        );

        update(parentFlow(childFlow(parent1ShouldNotBe("parent1"))),
                existingParentWithId(generatedId(0)).with(updateChild(0).with(FIELD_1, "INVALID")),
                existingParentWithId(generatedId(1)).with(updateChild(0).with(FIELD_1, "valid child"))
        );

        List<String> children = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());;
        assertThat(children, contains("one", "valid child"));

        assertThat(parentNamesInDB(), contains("parent1", "parent2"));
    }

    @Test
    public void dont_update_child_for_parent1_when_parent1_is_renamed() {

        insert(
                newParent().with(ParentEntity.NAME, "parent1").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "one")),
                newParent().with(ParentEntity.NAME, "parent2").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "two"))
        );

        update(parentFlow(childFlow(parent1ShouldNotBe("parent1_new"))),
                existingParentWithId(generatedId(0)).with(ParentEntity.NAME, "parent1_new").with(updateChild(0).with(FIELD_1, "INVALID")),
                existingParentWithId(generatedId(1)).with(ParentEntity.NAME, "parent2_new").with(updateChild(0).with(FIELD_1, "valid child"))
        );

        List<String> children = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());;
        assertThat(children, contains("one", "valid child"));

        assertThat(parentNamesInDB(), contains("parent1", "parent2_new"));
    }

    @Test
    public void create_new_hierarchy_with_enricher_from_parent() {

        final ChangeFlowConfig.Builder<ParentEntity> flow = parentFlow(childFlow(
                enrichWithValueFrom(ParentEntity.NAME, (child, parentName) -> child.set(FIELD_1, child.get(FIELD_1) + " (child of " + parentName + ")"))
        ));

        insert(flow, newParent().with(ParentEntity.NAME, "avraham").with(
                    insertChild().with(ORDINAL, 0).with(FIELD_1, "izak")
            )
        );

        List<String> actualChildren = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());

        assertThat(actualChildren, contains("izak (child of avraham)"));
    }

    @Test
    public void delete_missing_children_create_new_and_update_existing_another_commands() {

        insert(newParent()
                .with(upsertChild(1).with(FIELD_1, "child1"))
                .with(upsertChild(2).with(FIELD_1, "child2"))
        );

        final ParentCmdBuilder parentBuilder = existingParentWithId(generatedId(0))
                .with(upsertChild(1).with(FIELD_1, "child1 updated").get())
                .with(upsertChild(3).with(FIELD_1, "child3").get())
                .with(deletionOfOther);

        update(parentFlow(childFlow()), parentBuilder);

        Map<Integer, ChildPojo> childrenByOrdinal = seq(childrenInDb()).toMap(child -> child.ordinal);

        assertThat(childrenByOrdinal.get(1).field1, is("child1 updated"));
        assertThat(childrenByOrdinal.get(3).field1, is("child3"));
        assertFalse(childrenByOrdinal.containsKey(2));
    }

    @Test
    public void when_no_children_in_parent_then_delete_all_children() {

        insert(newParent()
                .with(upsertChild(1).with(FIELD_1, "child1"))
                .with(upsertChild(2).with(FIELD_1, "child2"))
        );

        final ParentCmdBuilder parentBuilder = existingParentWithId(generatedId(0)).with(deletionOfOther);;

        update(parentFlow(childFlow()), parentBuilder);

        Map<Integer, ChildPojo> childrenByOrdinal = seq(childrenInDb()).toMap(child -> child.ordinal);

        assertFalse(childrenByOrdinal.containsKey(1));
        assertFalse(childrenByOrdinal.containsKey(2));
    }

    @Test
    public void when_no_missing_children_in_parent_then_success_to_update_other_child() {

        insert(newParent());

        final ParentCmdBuilder parentBuilder = existingParentWithId(generatedId(0))
                .with(upsertChild(1).with(FIELD_1, "child1"))
                .with(deletionOfOther);

        update(parentFlow(childFlow()), parentBuilder);

        Map<Integer, ChildPojo> childrenByOrdinal = seq(childrenInDb()).toMap(child -> child.ordinal);

        assertTrue(childrenByOrdinal.containsKey(1));
    }

    @Test
    public void when_parent_does_not_assign_to_child_with_its_identifier_then_success_to_update_other_child() {

        insert(newParentWithUniqueKey(11)
                .with(upsertChild(1).with(FIELD_1, "child1"))
                .with(upsertChild(2).with(FIELD_1, "child2"))
        );

        final ParentCmdBuilder parentBuilder = existingParentWithUniqueKey(11)
                .with(upsertChild(1).with(FIELD_1, "child1 updated"))
                .with(deletionOfOther);

        update(parentFlow(childFlow()), parentBuilder);

        Map<Integer, ChildPojo> childrenByOrdinal = seq(childrenInDb()).toMap(child -> child.ordinal);

        assertTrue(childrenByOrdinal.containsKey(1));
        assertFalse(childrenByOrdinal.containsKey(2));
    }

    @Test
    public void delete_missing_grandchildren_of_two_different_children_of_same_parent() {

        insert(newParent()
                .with(upsertChild(1)
                        .withChild(upsertGrandChild("red"))
                        .withChild(upsertGrandChild("blue"))
                )
                .with(upsertChild(2)
                        .withChild(upsertGrandChild("green"))
                        .withChild(upsertGrandChild("yellow"))
                )
        );

        ParentCmdBuilder parentBuilder = existingParentWithId(generatedId(0))
                .with(upsertChild(1)
                        .withChild(upsertGrandChild("red"))
                        .with(new DeletionOfOther<>(GrandChildEntity.INSTANCE))
                        .get())
                .with(upsertChild(2)
                        .withChild(upsertGrandChild("yellow"))
                        .with(new DeletionOfOther<>(GrandChildEntity.INSTANCE))
                        .get());

        update(parentFlow(childFlow()), parentBuilder);

        assertThat(grandChildrenColorsInDB(), containsInAnyOrder("red", "yellow"));
    }

    @Test
    public void delete_missing_grandchildren_of_two_different_parents_where_children_have_same_identifier() {

        insert(newParent()
                        .with(upsertChild(1)
                                .withChild(upsertGrandChild("red"))
                                .withChild(upsertGrandChild("blue"))
                        ),
                newParent()
                        .with(upsertChild(1)
                                .withChild(upsertGrandChild("green"))
                                .withChild(upsertGrandChild("yellow"))
                        )
        );

        update(parentFlow(childFlow()),
                existingParentWithId(generatedId(0))
                        .with(upsertChild(1)
                                .withChild(upsertGrandChild("red"))
                                .with(new DeletionOfOther<>(GrandChildEntity.INSTANCE))
                                .get()),
                existingParentWithId(generatedId(1))
                        .with(upsertChild(1)
                                .withChild(upsertGrandChild("yellow"))
                                .with(new DeletionOfOther<>(GrandChildEntity.INSTANCE))
                                .get())
        );

        assertThat(grandChildrenColorsInDB(), containsInAnyOrder("red", "yellow"));
    }

    @Test
    public void check_each_parent_not_delete_another_parents_child() {

        insert(newParent()
                .with(upsertChild(1).with(FIELD_1, "child1"))
        );

        insert(newParent()
                .with(upsertChild(2).with(FIELD_1, "child2"))
        );

        final ParentCmdBuilder parentBuilder1 = existingParentWithId(generatedId(0))
                .with(upsertChild(1).with(FIELD_1, "child1 updated").get())
                .with(deletionOfOther);

        final ParentCmdBuilder parentBuilder2 = existingParentWithId(generatedId(1))
                .with(upsertChild(2).with(FIELD_1, "child2 updated").get())
                .with(deletionOfOther);

        update(parentFlow(childFlow()), parentBuilder1, parentBuilder2);

        Map<Integer, ChildPojo> childrenByOrdinal = seq(childrenInDb()).toMap(child -> child.ordinal);

        assertThat(childrenByOrdinal.get(1).field1, is("child1 updated"));
        assertThat(childrenByOrdinal.get(2).field1, is("child2 updated"));
    }

    @Test
    public void when_deleting_parent_with_cascade_then_delete_children_and_grand() {

        insert(newParent()
                        .with(upsertChild(1)
                                .withChild(upsertGrandChild("red"))
                                .withChild(upsertGrandChild("blue"))
                        )
        );

        delete(parentFlow(childFlow()), new ParentCmdBuilder(deleteParentWithId(generatedId(0)).setCascade()));

        assertThat(childrenInDb(), empty());
        assertThat(grandChildrenColorsInDB(), empty());
    }

    @Test
    public void dont_crash_when_upserting_children_with_deletion_of_other_grandchildren() {

        insert(newParent().with(upsertChild(1).withChild(upsertGrandChild("red"))));

        update(parentFlow(childFlow()), existingParentWithId(generatedId(0))
                        .with(upsertChild(1).with(new DeletionOfOther<>(GrandChildEntity.INSTANCE)))
                        .with(upsertChild(2).with(new DeletionOfOther<>(GrandChildEntity.INSTANCE)))
        );

        assertThat(grandChildrenColorsInDB(), empty());
        assertThat(childrenInDb(), hasSize(2));
    }

    @Test
    public void dont_crash_when_upserting_parents_with_deletion_of_other_children() {

        insert(newParent().with(ID_IN_TARGET, 1).with(insertChild()));

        List<InsertOnDuplicateUpdateCommand<ParentEntity, ParentEntity.UniqueKey>> upserts = ImmutableList.of(
                new InsertOnDuplicateUpdateCommand<>(ParentEntity.INSTANCE, new ParentEntity.UniqueKey(1)),
                new InsertOnDuplicateUpdateCommand<>(ParentEntity.INSTANCE, new ParentEntity.UniqueKey(2))
        );

        upserts.forEach(cmd -> cmd.add(new DeletionOfOther<ChildEntity>(ChildEntity.INSTANCE)));

        persistenceLayer.upsert(upserts, parentFlow(childFlow()).build());

        assertThat(childrenInDb(), empty());
    }

    @Test
    public void when_deleting_child_with_cascade_then_delete_all_grand_children() {

        insert(newParent()
                .with(upsertChild(1)
                        .withChild(upsertGrandChild("red"))
                        .withChild(upsertGrandChild("blue"))
                )
                .with(upsertChild(2)
                        .withChild(upsertGrandChild("white"))
                )
        );

        final ParentCmdBuilder parentBuilder = existingParentWithId(generatedId(0))
                .with(deleteChild(1).setCascade());

        update(parentFlow(childFlow()), parentBuilder);

        assertThat(parentIdsInDB(), containsInAnyOrder(generatedId(0)));
        assertThat(seq(childrenInDb()).map(rec -> rec.ordinal).collect(toList()), containsInAnyOrder(2));
        assertThat(grandChildrenColorsInDB(), containsInAnyOrder("white"));
    }

    @Test
    public void when_deleting_other_children_then_grand_children_are_deleted() {

        insert(newParent()
                .with(upsertChild(1)
                        .withChild(upsertGrandChild("red"))
                        .withChild(upsertGrandChild("blue"))
                )
                .with(upsertChild(2)
                        .withChild(upsertGrandChild("green"))
                        .withChild(upsertGrandChild("yellow"))
                )
        );

        ParentCmdBuilder parentBuilder = existingParentWithId(generatedId(0))
                .with(new DeletionOfOther<>(ChildEntity.INSTANCE))
                .with(upsertChild(1)
                        .get());

        update(parentFlow(childFlow()), parentBuilder);

        assertThat(grandChildrenColorsInDB(), containsInAnyOrder("red", "blue"));
    }


    private PostFetchCommandEnricher<ChildEntity> enrichWithValueFrom(EntityField otherField, BiConsumer<ChangeEntityCommand<ChildEntity>, Object> enrichment) {
        return new PostFetchCommandEnricher<ChildEntity>() {
            @Override
            public Stream<? extends EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<ChildEntity>> changeEntityCommands, ChangeOperation changeOperation) {
                return (Stream)Stream.of(otherField);
            }

            @Override
            public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<ChildEntity, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
                return (Stream)Stream.of(otherField);
            }

            @Override
            public SupportedChangeOperation getSupportedChangeOperation() {
                return CREATE_AND_UPDATE;
            }

            @Override
            public void enrich(Collection<? extends ChangeEntityCommand<ChildEntity>> commands, ChangeOperation op, ChangeContext ctx) {
                commands.forEach(cmd -> enrichment.accept(cmd, cmd.containsField(otherField) ? cmd.get(otherField) : ctx.getEntity(cmd).get(otherField)));
            }

            @Override
            public Stream<EntityField<ChildEntity, ?>> fieldsToEnrich() {
                return Stream.of(otherField);
            }

        };
    }

    private FieldValidator<ChildEntity, String> field1ShouldNotBe(String invalidValue) {
        return new FieldValidator<ChildEntity, String>() {
            @Override
            public EntityField<ChildEntity, String> validatedField() {
                return FIELD_1;
            }

            @Override
            public ValidationError validate(String fieldValue) {
                return fieldValue == invalidValue
                        ? new ValidationError("this is invalid")
                        : null;
            }
        };
    }

    private FieldComplexValidator<ChildEntity, String> parent1ShouldNotBe(final String parentName) {
        return new FieldComplexValidator<ChildEntity, String>() {
            @Override
            public EntityField<ChildEntity, String> validatedField() {
                return FIELD_1;
            }

            @Override
            public ValidationError validate(String fieldValue, Entity entity) {
                return entity.get(ParentEntity.NAME).equals(parentName)
                        ? new ValidationError("this is invalid")
                        : null;
            }

            @Override
            public Stream<EntityField<?, ?>> fetchFields() {
                return Stream.of(ParentEntity.NAME);
            }
        };
    }

    private int generatedId(int i) {
        return idGenerator.generatedIds.get(i);
    }

    private RecordMapper<Record, ChildPojo> toChildPojo() {
        return rec -> new ChildPojo() {{
            parentId = rec.get(CHILD.parent_id);
            ordinal = rec.get(CHILD.ordinal);
            field1 = rec.get(CHILD.field1);
        }};
    }

    private CreateResult<ParentEntity, ParentEntity.Key> insert(ChangeFlowConfig.Builder<ParentEntity> parentFlow, ParentCmdBuilder... cmds) {
        final List<CreateEntityCommand<ParentEntity>> changes = Seq.of(cmds).map(c -> (CreateEntityCommand<ParentEntity>)c.cmd).toList();
        return persistenceLayer.create(
                changes,
                parentFlow.build(),
                ParentEntity.Key.DEFINITION);
    }

    private CreateResult<ParentEntity, ParentEntity.Key> insert(ParentCmdBuilder... cmds) {
        return insert(parentFlow(childFlow()), cmds);
    }

    private UpdateResult<ParentEntity, ParentEntity.Key> update(ParentCmdBuilder... cmds) {
        return update(parentFlow(childFlow()), cmds);
    }

    private UpdateResult<ParentEntity, ParentEntity.Key> update(ChangeFlowConfig.Builder<ParentEntity> parentFlow, ParentCmdBuilder... cmds) {
        final List<UpdateEntityCommand<ParentEntity, ParentEntity.Key>> changes = Seq.of(cmds).map(c -> (UpdateEntityCommand<ParentEntity, ParentEntity.Key>)c.cmd).toList();
        return persistenceLayer.update(
                changes,
                parentFlow.build());
    }

    private DeleteResult<ParentEntity, ParentEntity.Key> delete(ChangeFlowConfig.Builder<ParentEntity> parentFlow, ParentCmdBuilder... cmds) {
        final List<DeleteEntityCommand<ParentEntity, ParentEntity.Key>> changes = Seq.of(cmds).map(c -> (DeleteEntityCommand<ParentEntity, ParentEntity.Key>)c.cmd).toList();
        return persistenceLayer.delete(
                changes,
                parentFlow.build());
    }

    class ParentCmdBuilder {
        final ChangeEntityCommand<ParentEntity> cmd;

        ParentCmdBuilder(ChangeEntityCommand<ParentEntity> cmd) {
            this.cmd = cmd;
        }

        <T> ParentCmdBuilder with(EntityField<ParentEntity, T> field, T value) {
            cmd.set(field, value);
            return this;
        }

        ParentCmdBuilder with(ChangeEntityCommand<ChildEntity> child) {
            cmd.addChild(child);
            return this;
        }

        ParentCmdBuilder with(FluidPersistenceCmdBuilder<ChildEntity> child) {
            cmd.addChild(child.get());
            return this;
        }

        ParentCmdBuilder with(MissingChildrenSupplier<ChildEntity> supplier) {
            cmd.add(supplier);
            return this;
        }

    }

    private FluidPersistenceCmdBuilder<GrandChildEntity> upsertGrandChild(String color) {
        return fluid(new InsertOnDuplicateUpdateCommand<>(GrandChildEntity.INSTANCE, new GrandChildEntity.Color(color)));
    }

    private FluidPersistenceCmdBuilder<ChildEntity> upsertChild(int ordinal) {
        return fluid(new InsertOnDuplicateUpdateCommand<>(ChildEntity.INSTANCE, new ChildEntity.Ordinal(ordinal)));
    }

    private FluidPersistenceCmdBuilder<ChildEntity> updateChild(int ordinal) {
        return fluid(new UpdateEntityCommand<>(ChildEntity.INSTANCE, new ChildEntity.Ordinal(ordinal)));
    }

    private FluidPersistenceCmdBuilder<ChildEntity> insertChild() {
        return fluid(new CreateEntityCommand<>(ChildEntity.INSTANCE));
    }

    private DeleteEntityCommand<ChildEntity, ChildEntity.Ordinal> deleteChild(int ordinal) {
        return new DeleteEntityCommand<>(ChildEntity.INSTANCE, new ChildEntity.Ordinal(ordinal));
    }

    private ParentCmdBuilder newParent() {
        return new ParentCmdBuilder(new CreateEntityCommand<>(ParentEntity.INSTANCE));
    }

    private ParentCmdBuilder newParentWithUniqueKey(Integer idInTarget) {
        CreateEntityCommand<ParentEntity> cmd = new CreateEntityCommand<>(ParentEntity.INSTANCE);
        cmd.set(ParentEntity.ID_IN_TARGET, idInTarget);
        return new ParentCmdBuilder(cmd);
    }

    private ParentCmdBuilder existingParentWithId(Integer id) {
        return new ParentCmdBuilder(new UpdateEntityCommand<>(ParentEntity.INSTANCE, new ParentEntity.Key(id)));
    }

    private DeleteEntityCommand deleteParentWithId(Integer id) {
        return new DeleteEntityCommand<>(ParentEntity.INSTANCE, new ParentEntity.Key(id));
    }

    private ParentCmdBuilder existingParentWithUniqueKey(Integer idInTarget) {
        return new ParentCmdBuilder(new UpdateEntityCommand<>(ParentEntity.INSTANCE, new ParentEntity.UniqueKey(idInTarget)));
    }

    private ChangeFlowConfig.Builder<ChildEntity> childFlow() {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, ChildEntity.INSTANCE)
                .withPostFetchCommandEnricher(childrenIdGenerator)
                .withChildFlowBuilder(grandChildFlow());
    }

    private ChangeFlowConfig.Builder<GrandChildEntity> grandChildFlow() {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, GrandChildEntity.INSTANCE);
    }

    private ChangeFlowConfig.Builder<ChildEntity> childFlow(ChangeValidator... validators) {
        EntityChangeCompositeValidator<ChildEntity> compositeValidator = new EntityChangeCompositeValidator<>();
        Seq.of(validators).forEach(v -> compositeValidator.register(ChildEntity.INSTANCE, v));
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, ChildEntity.INSTANCE)
                .withPostFetchCommandEnricher(childrenIdGenerator)
                .withValidator(compositeValidator);
    }

    private ChangeFlowConfig.Builder<ChildEntity> childFlow(PostFetchCommandEnricher<ChildEntity>... enrichers) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, ChildEntity.INSTANCE)
                .withPostFetchCommandEnricher(childrenIdGenerator)
                .withPostFetchCommandEnrichers(Arrays.stream(enrichers).collect(toList()));
    }

    private ChangeFlowConfig.Builder<ParentEntity> parentFlow(ChangeFlowConfig.Builder<ChildEntity> childFlow) {
        final IntegerIdGeneratorEnricher<ParentEntity> idEnricher = new IntegerIdGeneratorEnricher(idGenerator, ParentEntity.ID);
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, ParentEntity.INSTANCE)
                .withPostFetchCommandEnricher(idEnricher)
                .withChildFlowBuilder(childFlow);
    }


    private static class TablesSetup {
        DSLContext staticDSLContext;
        boolean alreadyCreated = false;
    }

    private <E extends EntityType<E>> FieldValueSupplier<String> supplyFromField(EntityField<E, String> field, Function<String, String> mapping) {
        return new FieldValueSupplier<String>() {
            @Override
            public String supply(Entity entity) throws ValidationException, NotSuppliedException {
                return mapping.apply(entity.get(field));
            }

            @Override
            public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
                return Stream.of(field);
            }
        };
    }

    final IntegerIdGeneratorEnricher<ChildEntity> childrenIdGenerator = new IntegerIdGeneratorEnricher<>(new IdGenerator(), ChildEntity.ID);

    private List<Integer> parentIdsInDB() {
        return jooq.select().from(PARENT).fetch(rec -> rec.get(PARENT.id));
    }

    private List<String> parentNamesInDB() {
        return jooq.select().from(PARENT).fetch(rec -> rec.get(PARENT.name));
    }

    private List<String> grandChildrenColorsInDB() {
        return jooq.select().from(GRAND_CHILD).fetch(rec -> rec.get(GRAND_CHILD.color));
    }

    private List<ChildPojo> childrenInDb() {
        return jooq.select().from(CHILD).fetch(toChildPojo());
    }
}