package com.kenshoo.pl.one2many.relatedByNonPK;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.FluidPersistenceCmdBuilder;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;
import com.kenshoo.pl.entity.spi.FieldComplexValidator;
import com.kenshoo.pl.entity.spi.FieldValidator;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import com.kenshoo.pl.entity.spi.helpers.EntityChangeCompositeValidator;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.jooq.lambda.Seq;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kenshoo.pl.FluidPersistenceCmdBuilder.fluid;
import static com.kenshoo.pl.entity.SupportedChangeOperation.CREATE_AND_UPDATE;
import static com.kenshoo.pl.entity.spi.FieldValueSupplier.fromOldValue;
import static com.kenshoo.pl.one2many.relatedByNonPK.ChildEntity.FIELD_1;
import static com.kenshoo.pl.one2many.relatedByNonPK.ChildEntity.ORDINAL;
import static com.kenshoo.pl.one2many.relatedByNonPK.ParentEntity.NAME;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.jooq.lambda.Seq.seq;
import static org.junit.Assert.*;

public class PersistenceLayerOneToManyRelatedByNonPKTest {

    private static TablesSetup tablesSetup = new TablesSetup();

    private final static ParentTable PARENT = ParentTable.INSTANCE;
    private final static ChildTable CHILD = ChildTable.INSTANCE;

    private DSLContext jooq = TestJooqConfig.create();

    private PLContext plContext;

    private PersistenceLayer<ParentEntity> persistenceLayer;

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
        jooq.alterTable(PARENT).add(DSL.constraint("parent_unique_key").unique(PARENT.type, PARENT.idInTarget)).execute();
        jooq.alterTable(CHILD).add(DSL.constraint("child_unique_key").unique(CHILD.type, CHILD.idInTarget, CHILD.ordinal)).execute();
    }


    @After
    public void clearTables() {
        Stream.of(PARENT, CHILD)
                .map(jooq::deleteFrom)
                .forEach(Query::execute);
    }

    @AfterClass
    public static void dropTables() {
        tablesSetup.staticDSLContext.dropTableIfExists(PARENT).execute();
        tablesSetup.staticDSLContext.dropTableIfExists(CHILD).execute();
    }

    @Test
    public void update_parent_without_children_using_different_parent_key() {

        insert(newParent(Type.T1, 11)
                .with(ParentEntity.NAME, "old name")
                .with(ParentEntity.TYPE, Type.T1)
                .with(ParentEntity.ID_IN_TARGET, 11)
        );

        update(existingParentWithUniqueKey(Type.T1, 11)
                .with(ParentEntity.NAME, "new name")
        );

        assertThat(parentNamesInDB(), containsInAnyOrder("new name"));
    }

    @Test
    public void create_parent_with_2_children() {

        insert(newParent(Type.T1, 11)
                .with(upsertChild(0).with(FIELD_1, "child1"))
                .with(upsertChild(8).with(FIELD_1, "child2"))
        );

        List<ChildPojo> children = childrenInDb().stream().
                sorted(Comparator.comparingInt(c -> c.ordinal)).
                collect(Collectors.toList());

        assertThat(children, not(empty()));

        assertThat(children.get(0).type, is(Type.T1));
        assertThat(children.get(0).idInTarget, is(11));
        assertThat(children.get(0).ordinal, is(0));
        assertThat(children.get(0).field1, is("child1"));

        assertThat(children.get(1).type, is(Type.T1));
        assertThat(children.get(1).idInTarget, is(11));
        assertThat(children.get(1).ordinal, is(8));
        assertThat(children.get(1).field1, is("child2"));
    }

    @Test
    public void update_child() {

        var results = insert(newParent(Type.T1, 11)
                .with(upsertChild(1).with(FIELD_1, "child1"))
                .with(upsertChild(2).with(FIELD_1, "child2"))
                .with(upsertChild(3).with(FIELD_1, "child3"))
        );

        update(existingParentWithId(generatedId(results, Type.T1, 11))
                .with(updateChild(2).with(FIELD_1, "child2_UPDATED!"))
        );

        List<String> childsFromDB = seq(childrenInDb()).map(child -> child.field1).collect(toList());

        assertThat(childsFromDB, containsInAnyOrder("child1", "child2_UPDATED!", "child3"));
    }

    @Test
    public void delete_only_some_of_the_children() {

        var results = insert(newParent(Type.T1, 11)
                .with(insertChild().with(ORDINAL, 1).with(FIELD_1, "child1"))
                .with(insertChild().with(ORDINAL, 2).with(FIELD_1, "child2"))
                .with(insertChild().with(ORDINAL, 3).with(FIELD_1, "child3"))
                .with(insertChild().with(ORDINAL, 4).with(FIELD_1, "child4"))
        );

        update(existingParentWithId(generatedId(results, Type.T1, 11))
                .with(deleteChild(3))
                .with(deleteChild(4))
        );

        List<Integer> remainingChildren = seq(childrenInDb()).map(rec -> rec.ordinal).collect(toList());

        assertThat(remainingChildren, containsInAnyOrder(1, 2));
    }

    @Test
    public void delete_children_of_only_one_parent() {

        var results = insert(
                newParent(Type.T1, 11).with(insertChild().with(ORDINAL, 1).with(FIELD_1, "child1")),
                newParent(Type.T2, 12).with(insertChild().with(ORDINAL, 1).with(FIELD_1, "child2"))
        );

        update(existingParentWithId(generatedId(results, Type.T1, 11)).with(deleteChild(1)));

        List<String> remainingChildren = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());

        assertThat(remainingChildren, containsInAnyOrder("child2"));
    }

    @Test
    public void delete_and_update_and_add_children_in_one_command() {

        var results = insert(newParent(Type.T1, 11)
                .with(insertChild().with(ORDINAL, 1).with(FIELD_1, "child1"))
                .with(insertChild().with(ORDINAL, 2).with(FIELD_1, "child2"))
        );

        update(existingParentWithId(generatedId(results, Type.T1, 11))
                .with(deleteChild(1))
                .with(upsertChild(2).with(FIELD_1,  "updated_child2"))
                .with(upsertChild(3).with(FIELD_1,  "new_child3"))
        );

        List<ChildPojo> children = childrenInDb();

        assertThat(children, hasSize(2));

        assertThat(children.get(0).type, is(Type.T1));
        assertThat(children.get(0).idInTarget, is(11));
        assertThat(children.get(0).ordinal, is(2));
        assertThat(children.get(0).field1, is("updated_child2"));

        assertThat(children.get(1).type, is(Type.T1));
        assertThat(children.get(1).idInTarget, is(11));
        assertThat(children.get(1).ordinal, is(3));
        assertThat(children.get(1).field1, is("new_child3"));
    }

    @Test
    public void update_with_supplier_from_parent() {

        var results = insert(newParent(Type.T1, 11).with(NAME, "moshe")
                .with(insertChild().with(ORDINAL, 0).with(FIELD_1, "bla bla")));

        update(existingParentWithId(generatedId(results, Type.T1, 11))
                .with(updateChild(0).with(FIELD_1, fromOldValue(ParentEntity.NAME, parentName -> "I'm the child of " + parentName)))
        );

        ChildPojo child = childrenInDb().get(0);

        assertThat(child.field1, is("I'm the child of moshe"));
    }

    @Test
    public void create_child_with_supplier_from_parent() {

        var results = insert(
                newParent(Type.T1, 11).with(NAME, "moshe"),
                newParent(Type.T2, 12).with(NAME, "david")
        );

        var supplier = fromOldValue(ParentEntity.NAME, parentName -> "I'm the child of " + parentName);

        update(
                existingParentWithId(generatedId(results, Type.T1, 11)).with(insertChild().with(ORDINAL, 0).with(FIELD_1, supplier)),
                existingParentWithId(generatedId(results, Type.T2, 12)).with(insertChild().with(ORDINAL, 0).with(FIELD_1, supplier))
        );

        var children = childrenInDb().stream().
                sorted(Comparator.comparingInt(c -> c.idInTarget)).
                collect(Collectors.toList());

        assertThat(children.get(0).field1, is("I'm the child of moshe"));
        assertThat(children.get(1).field1, is("I'm the child of david"));
    }

    @Test
    public void update_with_supplier_from_itself() {

        var createResults = insert(
                newParent(Type.T1, 11).with(insertChild().with(ORDINAL, 0).with(FIELD_1, "one")),
                newParent(Type.T1, 12).with(insertChild().with(ORDINAL, 0).with(FIELD_1, "two")),
                newParent(Type.T1, 13).with(insertChild().with(ORDINAL, 0).with(FIELD_1, "three"))
        );

        update(existingParentWithId(generatedId(createResults, Type.T1, 12))
                .with(updateChild(0).with(FIELD_1, fromOldValue(FIELD_1, v -> v + " + something")))
        );

        List<String> results = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());

        assertThat(results, containsInAnyOrder("one", "two + something", "three"));
    }

    @Test
    public void update_with_enricher_from_itself() {

        var createResults = insert(
                newParent(Type.T1, 11).with(insertChild().with(ORDINAL, 0).with(FIELD_1, "one")),
                newParent(Type.T2, 11).with(insertChild().with(ORDINAL, 0).with(FIELD_1, "two")),
                newParent(Type.T3, 11).with(insertChild().with(ORDINAL, 0).with(FIELD_1, "three"))
        );

        update(parentFlow(childFlow(enrichWithValueFrom(FIELD_1, (cmd, previousValue) -> cmd.set(FIELD_1, previousValue + "_suffix")))),
                existingParentWithId(generatedId(createResults, Type.T1, 11)).with(updateChild(0).with(FIELD_1, fromOldValue(FIELD_1, v -> "prefix_" + v))),
                existingParentWithId(generatedId(createResults, Type.T2, 11)).with(updateChild(0).with(FIELD_1, fromOldValue(FIELD_1, v -> "prefix_" + v))),
                existingParentWithId(generatedId(createResults, Type.T3, 11)).with(updateChild(0).with(FIELD_1, fromOldValue(FIELD_1, v -> "prefix_" + v)))
        );

        List<String> results = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());

        assertThat(results, containsInAnyOrder("prefix_one_suffix", "prefix_two_suffix", "prefix_three_suffix"));
    }

    @Test
    public void dont_update_parent_if_child_is_invalid() {

        var createResults = insert(
                newParent(Type.T1, 11).with(ParentEntity.NAME, "parent1").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "one")),
                newParent(Type.T1, 12).with(ParentEntity.NAME, "parent2").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "two"))
        );

        UpdateResult<ParentEntity, ParentEntity.Key> results = update(parentFlow(childFlow(field1ShouldNotBe("INVALID"))),
                existingParentWithId(generatedId(createResults, Type.T1, 11)).with(ParentEntity.NAME, "parent1_new").with(updateChild(0).with(FIELD_1, "INVALID")),
                existingParentWithId(generatedId(createResults, Type.T1, 12)).with(ParentEntity.NAME, "parent2_new").with(updateChild(0).with(FIELD_1, "valid child"))
        );

        List<String> children = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());
        assertThat(children, containsInAnyOrder("one", "valid child"));

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

        var results = insert(
                newParent(Type.T1, 11).with(NAME, "parent1").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "one")),
                newParent(Type.T1, 12).with(NAME, "parent2").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "two"))
        );

        update(parentFlow(childFlow(parent1ShouldNotBe("parent1"))),
                existingParentWithId(generatedId(results, Type.T1, 11)).with(updateChild(0).with(FIELD_1, "INVALID")),
                existingParentWithId(generatedId(results, Type.T1, 12)).with(updateChild(0).with(FIELD_1, "valid child"))
        );

        List<String> children = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());
        assertThat(children, containsInAnyOrder("one", "valid child"));

        assertThat(parentNamesInDB(), containsInAnyOrder("parent1", "parent2"));
    }

    @Test
    public void dont_update_child_for_parent1_when_parent1_is_renamed() {

        var results = insert(
                newParent(Type.T1, 11).with(ParentEntity.NAME, "parent1").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "one")),
                newParent(Type.T1, 12).with(ParentEntity.NAME, "parent2").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "two"))
        );

        update(parentFlow(childFlow(parent1ShouldNotBe("parent1_new"))),
                existingParentWithId(generatedId(results, Type.T1, 11)).with(ParentEntity.NAME, "parent1_new").with(updateChild(0).with(FIELD_1, "INVALID")),
                existingParentWithId(generatedId(results, Type.T1, 12)).with(ParentEntity.NAME, "parent2_new").with(updateChild(0).with(FIELD_1, "valid child"))
        );

        List<String> children = childrenInDb().stream().map(rec -> rec.field1).collect(toList());
        assertThat(children, containsInAnyOrder("one", "valid child"));

        assertThat(parentNamesInDB(), containsInAnyOrder("parent1", "parent2_new"));
    }

    @Test
    public void create_new_hierarchy_with_enricher_from_parent() {

        final ChangeFlowConfig.Builder<ParentEntity> flow = parentFlow(childFlow(
                enrichWithValueFrom(ParentEntity.NAME, (child, parentName) -> child.set(FIELD_1, child.get(FIELD_1) + " (child of " + parentName + ")"))
        ));

        insert(flow, newParent(Type.T1, 11).with(ParentEntity.NAME, "avraham").with(
                insertChild().with(ORDINAL, 0).with(FIELD_1, "izak")
                )
        );

        List<String> actualChildren = seq(childrenInDb()).map(rec -> rec.field1).collect(toList());

        assertThat(actualChildren, contains("izak (child of avraham)"));
    }

    @Test
    public void delete_missing_children_create_new_and_update_existing_another_commands() {

        var results = insert(newParent(Type.T1, 11)
                .with(upsertChild(1).with(FIELD_1, "child1"))
                .with(upsertChild(2).with(FIELD_1, "child2"))
        );

        final ParentCmdBuilder parentBuilder = existingParentWithId(generatedId(results, Type.T1, 11))
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

        var  results = insert(newParent(Type.T1, 11)
                .with(upsertChild(1).with(FIELD_1, "child1"))
                .with(upsertChild(2).with(FIELD_1, "child2"))
        );

        final ParentCmdBuilder parentBuilder = existingParentWithId(generatedId(results, Type.T1, 11)).with(deletionOfOther);

        update(parentFlow(childFlow()), parentBuilder);

        Map<Integer, ChildPojo> childrenByOrdinal = seq(childrenInDb()).toMap(child -> child.ordinal);

        assertFalse(childrenByOrdinal.containsKey(1));
        assertFalse(childrenByOrdinal.containsKey(2));
    }

    @Test
    public void when_no_missing_children_in_parent_then_success_to_update_other_child() {

        var results = insert(newParent(Type.T1, 11));

        final ParentCmdBuilder parentBuilder = existingParentWithId(generatedId(results, Type.T1, 11))
                .with(upsertChild(1).with(FIELD_1, "child1"))
                .with(deletionOfOther);

        update(parentFlow(childFlow()), parentBuilder);

        Map<Integer, ChildPojo> childrenByOrdinal = seq(childrenInDb()).toMap(child -> child.ordinal);

        assertTrue(childrenByOrdinal.containsKey(1));
    }

    @Test
    public void when_parent_does_not_assign_to_child_with_its_identifier_then_success_to_update_other_child() {

        insert(newParent(Type.T1, 11)
                .with(upsertChild(1).with(FIELD_1, "child1"))
                .with(upsertChild(2).with(FIELD_1, "child2"))
        );

        final ParentCmdBuilder parentBuilder = existingParentWithUniqueKey(Type.T1, 11)
                .with(upsertChild(1).with(FIELD_1, "child1 updated"))
                .with(deletionOfOther);

        update(parentFlow(childFlow()), parentBuilder);

        Map<Integer, ChildPojo> childrenByOrdinal = seq(childrenInDb()).toMap(child -> child.ordinal);

        assertTrue(childrenByOrdinal.containsKey(1));
        assertFalse(childrenByOrdinal.containsKey(2));
    }



    @Test
    public void check_each_parent_not_delete_another_parents_child() {

        var results = insert(newParent(Type.T1, 11)
                .with(upsertChild(1).with(FIELD_1, "child1")),

            newParent(Type.T1, 12)
                .with(upsertChild(2).with(FIELD_1, "child2"))
        );

        final ParentCmdBuilder parentBuilder1 = existingParentWithId(generatedId(results, Type.T1, 11))
                .with(upsertChild(1).with(FIELD_1, "child1 updated").get())
                .with(deletionOfOther);

        final ParentCmdBuilder parentBuilder2 = existingParentWithId(generatedId(results, Type.T1, 12))
                .with(upsertChild(2).with(FIELD_1, "child2 updated").get())
                .with(deletionOfOther);

        update(parentFlow(childFlow()), parentBuilder1, parentBuilder2);

        Map<Integer, ChildPojo> childrenByOrdinal = seq(childrenInDb()).toMap(child -> child.ordinal);

        assertThat(childrenByOrdinal.get(1).field1, is("child1 updated"));
        assertThat(childrenByOrdinal.get(2).field1, is("child2 updated"));
    }


    @Test
    public void dont_crash_when_upserting_parents_with_deletion_of_other_children() {

        insert(newParent(Type.T1, 1).with(insertChild()));

        List<InsertOnDuplicateUpdateCommand<ParentEntity, ParentEntity.UniqueKey>> upserts = ImmutableList.of(
                new InsertOnDuplicateUpdateCommand<>(ParentEntity.INSTANCE, new ParentEntity.UniqueKey(Type.T1, 1)),
                new InsertOnDuplicateUpdateCommand<>(ParentEntity.INSTANCE, new ParentEntity.UniqueKey(Type.T1, 2))
        );

        upserts.forEach(cmd -> cmd.add(new DeletionOfOther<ChildEntity>(ChildEntity.INSTANCE)));

        persistenceLayer.upsert(upserts, parentFlow(childFlow()).build());

        assertThat(childrenInDb(), empty());
    }



    private PostFetchCommandEnricher<ChildEntity> enrichWithValueFrom(EntityField otherField, BiConsumer<ChangeEntityCommand<ChildEntity>, Object> enrichment) {
        return new PostFetchCommandEnricher<ChildEntity>() {

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
        return new FieldValidator<>() {
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
        return new FieldComplexValidator<>() {
            @Override
            public EntityField<ChildEntity, String> validatedField() {
                return FIELD_1;
            }

            @Override
            public ValidationError validate(String fieldValue, CurrentEntityState entity) {
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


    private RecordMapper<Record, ChildPojo> toChildPojo() {
        return rec -> new ChildPojo() {{
            type = Type.valueOf(rec.get(CHILD.type));
            idInTarget = rec.get(CHILD.idInTarget);
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


    private ParentCmdBuilder newParent(Type type, int idInTarget) {
        CreateEntityCommand<ParentEntity> cmd = new CreateEntityCommand<>(ParentEntity.INSTANCE);
        cmd.set(ParentEntity.TYPE, type);
        cmd.set(ParentEntity.ID_IN_TARGET, idInTarget);
        return new ParentCmdBuilder(cmd);
    }

    private ParentCmdBuilder existingParentWithId(Integer id) {
        return new ParentCmdBuilder(new UpdateEntityCommand<>(ParentEntity.INSTANCE, new ParentEntity.Key(id)));
    }

    private ParentCmdBuilder existingParentWithUniqueKey(Type type, Integer idInTarget) {
        return new ParentCmdBuilder(new UpdateEntityCommand<>(ParentEntity.INSTANCE, new ParentEntity.UniqueKey(type, idInTarget)));
    }

    private ChangeFlowConfig.Builder<ChildEntity> childFlow() {
        return createChildEntityFlowBuilder();
    }

    private  ChangeFlowConfig.Builder<ChildEntity> childFlow(FieldComplexValidator<ChildEntity, String> validator) {
        EntityChangeCompositeValidator<ChildEntity> compositeValidator = new EntityChangeCompositeValidator<>();
        compositeValidator.register(validator);
        return createChildEntityFlowBuilder()
                .withValidator(compositeValidator);
    }

    private  ChangeFlowConfig.Builder<ChildEntity> childFlow(FieldValidator<ChildEntity, String> validator) {
        EntityChangeCompositeValidator<ChildEntity> compositeValidator = new EntityChangeCompositeValidator<>();
        compositeValidator.register(validator);
        return createChildEntityFlowBuilder()
                .withValidator(compositeValidator);
    }

    private ChangeFlowConfig.Builder<ChildEntity> childFlow(PostFetchCommandEnricher<ChildEntity>... enrichers) {
        return createChildEntityFlowBuilder()
                .withPostFetchCommandEnrichers(Arrays.stream(enrichers).collect(toList()));
    }

    private ChangeFlowConfig.Builder<ParentEntity> parentFlow(ChangeFlowConfig.Builder<ChildEntity> childFlow) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, ParentEntity.INSTANCE).withChildFlowBuilder(childFlow);
    }


    private ChangeFlowConfig.Builder<ChildEntity> createChildEntityFlowBuilder() {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, ChildEntity.INSTANCE);
    }

    private static class TablesSetup {
        DSLContext staticDSLContext;
        boolean alreadyCreated = false;
    }


    private List<String> parentNamesInDB() {
        return jooq.select().from(PARENT).fetch(rec -> rec.get(PARENT.name));
    }

    private List<ChildPojo> childrenInDb() {
        return jooq.select().from(CHILD).fetch(toChildPojo());
    }

    private int generatedId(CreateResult<ParentEntity, ParentEntity.Key> results, Type type, int idInTarget) {
        ParentEntity.UniqueKey requestedUniqueKey = new ParentEntity.UniqueKey(type, idInTarget);
        return Seq.seq(results.iterator())
                .map(EntityChangeResult::getCommand)
                .filter(cmd -> new ParentEntity.UniqueKey(cmd.get(ParentEntity.TYPE), cmd.get(ParentEntity.ID_IN_TARGET)).equals(requestedUniqueKey))
                .map(CreateEntityCommand::getIdentifier)
                .map(identifier -> identifier.get(ParentEntity.ID))
                .findFirst()
                .orElse(-1);
    }
}
