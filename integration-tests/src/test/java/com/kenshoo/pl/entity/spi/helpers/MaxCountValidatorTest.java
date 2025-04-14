package com.kenshoo.pl.entity.spi.helpers;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import com.kenshoo.pl.one2many.relatedByPK.ChildEntity;
import com.kenshoo.pl.one2many.relatedByPK.ChildTable;
import com.kenshoo.pl.one2many.relatedByPK.ParentEntity;
import com.kenshoo.pl.one2many.relatedByPK.ParentTable;
import org.jooq.DSLContext;
import org.jooq.lambda.Seq;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;
import static com.kenshoo.pl.entity.TestEnum.Alpha;
import static com.kenshoo.pl.entity.TestEnum.Bravo;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MaxCountValidatorTest {

    private static final ParentTable parentTable = ParentTable.INSTANCE;
    private static final ChildTable childTable = ChildTable.INSTANCE;
    private static final Set<DataTable> ALL_TABLES = ImmutableSet.of(parentTable, childTable);

    private static boolean tablesCreated;

    private static DSLContext staticDSLContext;
    private DSLContext dslContext = TestJooqConfig.create();
    private PLContext plContext = new PLContext.Builder(dslContext).build();
    private EntitiesFetcher entitiesFetcher = new EntitiesFetcher(dslContext);
    private PersistenceLayer<ParentEntity> parentPersistence = new PersistenceLayer<>(plContext);
    private PersistenceLayer<ChildEntity> childPersistence = new PersistenceLayer<>(plContext);

    @Before
    public void setup() {
        if (!tablesCreated) {
            staticDSLContext = dslContext;
            ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));
            tablesCreated = true;
        }
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> dslContext.deleteFrom(table).execute());
    }

    @AfterClass
    public static void dropTables() {
        ALL_TABLES.forEach(table -> staticDSLContext.dropTableIfExists(table).execute());
    }

    @Test
    public void testFailTheLastChangesInBulkExceedingMax() {

        final int MAX_ALLOWED = 4;
        final var GROUP_BY_NAME = uniqueKey(ParentEntity.ENUM_FIELD);

        create(
                new CreateParent().with(ParentEntity.ID, 1).with(ParentEntity.ENUM_FIELD, Alpha),
                new CreateParent().with(ParentEntity.ID, 2).with(ParentEntity.ENUM_FIELD, Alpha),
                new CreateParent().with(ParentEntity.ID, 3).with(ParentEntity.ENUM_FIELD, Bravo),
                new CreateParent().with(ParentEntity.ID, 4).with(ParentEntity.ENUM_FIELD, Bravo),
                new CreateParent().with(ParentEntity.ID, 5).with(ParentEntity.ENUM_FIELD, Bravo)
        );

        var validator = new MaxCountValidator.Builder<>(entitiesFetcher, ParentEntity.INSTANCE, GROUP_BY_NAME)
                .setMaxAllowed(MAX_ALLOWED)
                .build();

        var commands = asList(
                new CreateParent().with(ParentEntity.ID, 100).with(ParentEntity.ENUM_FIELD, Alpha),
                new CreateParent().with(ParentEntity.ID, 200).with(ParentEntity.ENUM_FIELD, Alpha),
                new CreateParent().with(ParentEntity.ID, 300).with(ParentEntity.ENUM_FIELD, Alpha),
                new CreateParent().with(ParentEntity.ID, 400).with(ParentEntity.ENUM_FIELD, Alpha)
        );

        var results = parentPersistence.create(commands, parentFlow(validator).build());

        assertFalse(results.hasErrors(commands.get(0)));
        assertFalse(results.hasErrors(commands.get(1)));
        assertTrue(results.hasErrors(commands.get(2)));
        assertTrue(results.hasErrors(commands.get(3)));
    }

    @Test
    public void testDontFailGroupThatDidNotExceedMaxWhenOtherGroupDidExceedMax() {

        final int MAX_ALLOWED = 1;
        final var GROUP_BY_NAME = uniqueKey(ParentEntity.NAME);

        create(
                new CreateParent().with(ParentEntity.ID, 1).with(ParentEntity.NAME, "red")
        );

        var validator = new MaxCountValidator.Builder<>(entitiesFetcher, ParentEntity.INSTANCE, GROUP_BY_NAME)
                .setMaxAllowed(MAX_ALLOWED)
                .build();

        var commands = asList(
                new CreateParent().with(ParentEntity.ID, 100).with(ParentEntity.NAME, "red"),
                new CreateParent().with(ParentEntity.ID, 200).with(ParentEntity.NAME, "blue")
        );

        var results = parentPersistence.create(commands, parentFlow(validator).build());

        assertTrue(results.hasErrors(commands.get(0)));
        assertFalse(results.hasErrors(commands.get(1)));
    }

    @Test
    public void testWhenCommandsNotMatchingConditionThenTheyAreNotCounted() {

        final int MAX_ALLOWED = 1;
        final var GROUP_BY_NAME = uniqueKey(ParentEntity.NAME);

        var validator = new MaxCountValidator.Builder<>(entitiesFetcher, ParentEntity.INSTANCE, GROUP_BY_NAME)
                .setMaxAllowed(MAX_ALLOWED)
                .setFetchCondition(ParentEntity.ID.in(100, 200))
                .setPostFetchCondition(ParentEntity.ID.postFetchIn(100, 200))
                .build();

        var commands = asList(
                new CreateParent().with(ParentEntity.ID, 100).with(ParentEntity.NAME, "red"),
                new CreateParent().with(ParentEntity.ID, 200).with(ParentEntity.NAME, "red"),
                new CreateParent().with(ParentEntity.ID, 300).with(ParentEntity.NAME, "red")
        );

        var results = parentPersistence.create(commands, parentFlow(validator).build());

        assertFalse(results.hasErrors(commands.get(0)));
        assertTrue(results.hasErrors(commands.get(1)));
        assertFalse(results.hasErrors(commands.get(2)));
    }

    @Test
    public void testConditionOnParentsAndGroupingOnChildren() {

        final int MAX_ALLOWED = 4;
        final var GROUP_BY_FIELD_1 = uniqueKey(ChildEntity.FIELD_1);

        create(
                new CreateParent().with(ParentEntity.ID, 1).with(ParentEntity.NAME, "parent matching condition")
                    .with(new CreateChild().with(ChildEntity.ID, 1).with(ChildEntity.FIELD_1, "new york"))
                    .with(new CreateChild().with(ChildEntity.ID, 2).with(ChildEntity.FIELD_1, "new york")),
                new CreateParent().with(ParentEntity.ID, 2).with(ParentEntity.NAME, "parent not included in condition")
                        .with(new CreateChild().with(ChildEntity.ID, 3).with(ChildEntity.FIELD_1, "new york"))
                        .with(new CreateChild().with(ChildEntity.ID, 4).with(ChildEntity.FIELD_1, "new york"))
                        .with(new CreateChild().with(ChildEntity.ID, 5).with(ChildEntity.FIELD_1, "new york"))
                        .with(new CreateChild().with(ChildEntity.ID, 6).with(ChildEntity.FIELD_1, "new york"))
                        .with(new CreateChild().with(ChildEntity.ID, 7).with(ChildEntity.FIELD_1, "new york"))
        );

        var validator = new MaxCountValidator.Builder<>(entitiesFetcher, ChildEntity.INSTANCE, GROUP_BY_FIELD_1)
                .setMaxAllowed(MAX_ALLOWED)
                .setFetchCondition(ParentEntity.NAME.eq("parent matching condition"))
                .setPostFetchCondition(ParentEntity.NAME.postFetchEq("parent matching condition"))
                .build();

        var commands = asList(
                new CreateChild().with(ChildEntity.PARENT_ID, 1).with(ChildEntity.ID, 100).with(ChildEntity.FIELD_1, "new york"),
                new CreateChild().with(ChildEntity.PARENT_ID, 1).with(ChildEntity.ID, 200).with(ChildEntity.FIELD_1, "new york"),
                new CreateChild().with(ChildEntity.PARENT_ID, 1).with(ChildEntity.ID, 300).with(ChildEntity.FIELD_1, "new york"),
                new CreateChild().with(ChildEntity.PARENT_ID, 2).with(ChildEntity.ID, 400).with(ChildEntity.FIELD_1, "new york")
        );

        var results = childPersistence.create(commands, childFlow(validator).build());

        assertFalse(results.hasErrors(commands.get(0)));
        assertFalse(results.hasErrors(commands.get(1)));
        assertTrue(results.hasErrors(commands.get(2)));
        assertFalse(results.hasErrors(commands.get(3)));
    }

    private ChangeFlowConfig.Builder<ParentEntity> parentFlow(ChangesValidator<ParentEntity>... validators) {
        return ChangeFlowConfigBuilderFactory
                .newInstance(plContext, ParentEntity.INSTANCE)
                .withValidators(Seq.of(validators).toList())
                .withChildFlowBuilder(childFlow())
                ;
    }

    private ChangeFlowConfig.Builder<ChildEntity> childFlow(ChangesValidator<ChildEntity>... validators) {
        return ChangeFlowConfigBuilderFactory
                .newInstance(plContext, ChildEntity.INSTANCE)
                .withValidators(Seq.of(validators).toList());
    }

    private static class CreateParent extends CreateEntityCommand<ParentEntity> implements EntityCommandExt<ParentEntity, CreateParent> {
        public CreateParent() {
            super(ParentEntity.INSTANCE);
        }
    }

    private void create(CreateParent... parents) {
        parentPersistence.create(asList(parents), parentFlow().build());
    }

    private static class CreateChild extends CreateEntityCommand<ChildEntity> implements EntityCommandExt<ChildEntity, CreateChild> {
        public CreateChild() {
            super(ChildEntity.INSTANCE);
        }
    }

}
