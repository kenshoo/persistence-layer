package com.kenshoo.pl.one2many;

import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.FluidPersistenceCmdBuilder;
import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeFlowConfig;
import com.kenshoo.pl.entity.ChangeFlowConfigBuilderFactory;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.CreateResult;
import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.PLContext;
import com.kenshoo.pl.entity.PersistenceLayer;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.UpdateResult;
import com.kenshoo.pl.entity.ValidationError;
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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.kenshoo.pl.FluidPersistenceCmdBuilder.fluid;
import static com.kenshoo.pl.entity.SupportedChangeOperation.CREATE_AND_UPDATE;
import static com.kenshoo.pl.one2many.ChildEntity.FIELD_1;
import static com.kenshoo.pl.one2many.ChildEntity.ORDINAL;
import static com.kenshoo.pl.one2many.ParentEntity.NAME;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class PersistenceLayerOneToManyTest {

    private static TablesSetup tablesSetup = new TablesSetup();

    private final static ParentTable PARENT = ParentTable.INSTANCE;
    private final static ChildTable CHILD = ChildTable.INSTANCE;

    private final IdGenerator idGenerator = new IdGenerator();

    private DSLContext jooq = TestJooqConfig.create();

    private PLContext plContext;

    private PersistenceLayer<ParentEntity, ParentEntity.Key> persistenceLayer;

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
        jooq.alterTable(PARENT).add(DSL.constraint("unique_id").unique(PARENT.id)).execute();
        jooq.alterTable(PARENT).add(DSL.constraint("unique_key").unique(PARENT.idInTarget)).execute();
        jooq.alterTable(CHILD).add(DSL.constraint("unique_parent_and_ordinal").unique(CHILD.ordinal, CHILD.parent_id)).execute();
    }


    @After
    public void clearTables() {
        jooq.deleteFrom(PARENT).execute();
        jooq.deleteFrom(CHILD).execute();
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

        String name = jooq.select(PARENT.name).from(PARENT).fetch(PARENT.name).get(0);

        assertThat(name, is("new name"));
    }

    @Test
    public void create_parent_with_2_children() {

        insert(newParent()
                .with(upsertChild(0).with(FIELD_1, "child1"))
                .with(upsertChild(8).with(FIELD_1, "child2"))
        );

        List<ChildPojo> children = jooq.select().from(CHILD).orderBy(CHILD.ordinal).fetch(toChildPojo());

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

        ChildPojo child2 = jooq.select().from(CHILD).where(CHILD.ordinal.eq(2)).fetchOne(toChildPojo());

        assertThat(child2.field1, is("child2_UPDATED!"));
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

        List<Integer> remainingChildren = jooq.select().from(CHILD).fetch(CHILD.ordinal);

        assertThat(remainingChildren, containsInAnyOrder(1, 2));
    }

    @Test
    public void delete_children_of_only_one_parent() {

        insert(
                newParent().with(insertChild().with(ORDINAL, 1).with(FIELD_1, "child1")),
                newParent().with(insertChild().with(ORDINAL, 1).with(FIELD_1, "child2"))
        );

        update(existingParentWithId(generatedId(0)).with(deleteChild(1)));

        List<String> remainingChildren = jooq.select().from(CHILD).fetch(CHILD.field1);

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

        List<ChildPojo> children = jooq.select().from(CHILD).orderBy(CHILD.ordinal).fetch(toChildPojo());

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

        List<ChildPojo> children = jooq.select().from(CHILD).orderBy(CHILD.ordinal).fetch(toChildPojo());

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

        ChildPojo child = jooq.select().from(CHILD).fetchOne(toChildPojo());

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

        ChildPojo child = jooq.select().from(CHILD).fetchOne(toChildPojo());

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

        List<String> results = jooq.select().from(CHILD).fetch(row -> row.get(ChildTable.INSTANCE.field1));

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

        List<String> results = jooq.select().from(CHILD).fetch(row -> row.get(ChildTable.INSTANCE.field1));

        assertThat(results, containsInAnyOrder("prefix_one_suffix", "prefix_two_suffix", "prefix_three_suffix"));
    }

    @Test
    public void dont_update_parent_if_child_is_invalid() {

        insert(
                newParent().with(ParentEntity.NAME, "parent1").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "one")),
                newParent().with(ParentEntity.NAME, "parent2").with(insertChild().with(ORDINAL, 0).with(FIELD_1, "two"))
        );

        update(parentFlow(childFlow(field1ShouldNotBe("INVALID"))),
                existingParentWithId(generatedId(0)).with(ParentEntity.NAME, "parent1_new").with(updateChild(0).with(FIELD_1, "INVALID")),
                existingParentWithId(generatedId(1)).with(ParentEntity.NAME, "parent2_new").with(updateChild(0).with(FIELD_1, "valid child"))
        );

        List<String> children = jooq.select().from(CHILD).orderBy(CHILD.parent_id).fetch(r -> r.get(CHILD.field1));
        assertThat(children, contains("one", "valid child"));

        List<String> parentNames = jooq.select().from(PARENT).orderBy(PARENT.id).fetch(r -> r.get(PARENT.name));
        assertThat(parentNames, contains("parent1", "parent2_new"));
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

        List<String> children = jooq.select().from(CHILD).orderBy(CHILD.parent_id).fetch(r -> r.get(CHILD.field1));
        assertThat(children, contains("one", "valid child"));

        List<String> parentNames = jooq.select().from(PARENT).orderBy(PARENT.id).fetch(r -> r.get(PARENT.name));
        assertThat(parentNames, contains("parent1", "parent2"));
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

        List<String> children = jooq.select().from(CHILD).orderBy(CHILD.parent_id).fetch(r -> r.get(CHILD.field1));
        assertThat(children, contains("one", "valid child"));

        List<String> parentNames = jooq.select().from(PARENT).orderBy(PARENT.id).fetch(r -> r.get(PARENT.name));
        assertThat(parentNames, contains("parent1", "parent2_new"));
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

        List<String> actualChildren = jooq.select().from(CHILD).fetch(row -> row.get(ChildTable.INSTANCE.field1));

        assertThat(actualChildren, contains("izak (child of avraham)"));
    }


    private PostFetchCommandEnricher<ChildEntity> enrichWithValueFrom(EntityField otherField, BiConsumer<ChangeEntityCommand<ChildEntity>, Object> enrichment) {
        return new PostFetchCommandEnricher<ChildEntity>() {
            @Override
            public Stream<? extends EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<ChildEntity>> changeEntityCommands, ChangeOperation changeOperation) {
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

    private ParentCmdBuilder existingParentWithId(Integer id) {
        return new ParentCmdBuilder(new UpdateEntityCommand<>(ParentEntity.INSTANCE, new ParentEntity.Key(id)));
    }

    private ParentCmdBuilder existingParentWithUniqueKey(Integer idInTarget) {
        return new ParentCmdBuilder(new UpdateEntityCommand<>(ParentEntity.INSTANCE, new ParentEntity.UniqueKey(idInTarget)));
    }

    private ChangeFlowConfig.Builder<ChildEntity> childFlow() {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, ChildEntity.INSTANCE);
    }

    private ChangeFlowConfig.Builder<ChildEntity> childFlow(ChangeValidator... validators) {
        EntityChangeCompositeValidator<ChildEntity> compositeValidator = new EntityChangeCompositeValidator<>();
        Seq.of(validators).forEach(v -> compositeValidator.register(ChildEntity.INSTANCE, v));
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, ChildEntity.INSTANCE)
                .withValidator(compositeValidator);
    }

    private ChangeFlowConfig.Builder<ChildEntity> childFlow(PostFetchCommandEnricher<ChildEntity>... enrichers) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, ChildEntity.INSTANCE)
                .withPostFetchCommandEnrichers(Arrays.stream(enrichers).collect(toList()));
    }

    private ChangeFlowConfig.Builder<ParentEntity> parentFlow(ChangeFlowConfig.Builder<ChildEntity> childFlow) {
        final IntegerIdGeneratorEnricher idEnricher = new IntegerIdGeneratorEnricher(idGenerator, ParentEntity.ID);
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

}