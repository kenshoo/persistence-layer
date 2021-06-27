package com.kenshoo.pl.entity.spi.helpers;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.entity.spi.ChangesValidator;
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
import static com.kenshoo.pl.one2many.relatedByPK.ParentEntity.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MaxCountValidatorTest {

    private static final ParentTable parentTable = ParentTable.INSTANCE;
    private static final Set<DataTable> ALL_TABLES = ImmutableSet.of(parentTable);

    private static boolean tablesCreated;

    private static DSLContext staticDSLContext;
    private DSLContext dslContext = TestJooqConfig.create();
    private PLContext plContext = new PLContext.Builder(dslContext).build();
    private EntitiesFetcher entitiesFetcher = new EntitiesFetcher(dslContext);
    private PersistenceLayer<ParentEntity> parentPersistence = new PersistenceLayer<>(plContext);

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
        final var GROUP_BY_NAME = uniqueKey(NAME);

        create(
                new CreateParent().with(ID, 1).with(NAME, "red"),
                new CreateParent().with(ID, 2).with(NAME, "red"),
                new CreateParent().with(ID, 3).with(NAME, "blue"),
                new CreateParent().with(ID, 4).with(NAME, "blue"),
                new CreateParent().with(ID, 5).with(NAME, "blue")
        );

        var validator = new MaxCountValidator.Builder<>(entitiesFetcher, GROUP_BY_NAME)
                .setMaxAllowed(MAX_ALLOWED)
                .build();

        var commands = asList(
                new CreateParent().with(ID, 100).with(NAME, "red"),
                new CreateParent().with(ID, 200).with(NAME, "red"),
                new CreateParent().with(ID, 300).with(NAME, "red"),
                new CreateParent().with(ID, 400).with(NAME, "red")
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
        final var GROUP_BY_NAME = uniqueKey(NAME);

        create(
                new CreateParent().with(ID, 1).with(NAME, "red")
        );

        var validator = new MaxCountValidator.Builder<>(entitiesFetcher, GROUP_BY_NAME)
                .setMaxAllowed(MAX_ALLOWED)
                .build();

        var commands = asList(
                new CreateParent().with(ID, 100).with(NAME, "red"),
                new CreateParent().with(ID, 200).with(NAME, "blue")
        );

        var results = parentPersistence.create(commands, parentFlow(validator).build());

        assertTrue(results.hasErrors(commands.get(0)));
        assertFalse(results.hasErrors(commands.get(1)));
    }

    @Test
    public void testCommandsNotMatchingConditionAreNotCounted() {

        final int MAX_ALLOWED = 1;
        final var GROUP_BY_NAME = uniqueKey(NAME);

        var validator = new MaxCountValidator.Builder<>(entitiesFetcher, GROUP_BY_NAME)
                .setMaxAllowed(MAX_ALLOWED)
                .setCondition(ID.in(100, 200))
                .build();

        var commands = asList(
                new CreateParent().with(ID, 100).with(NAME, "red"),
                new CreateParent().with(ID, 200).with(NAME, "red"),
                new CreateParent().with(ID, 300).with(NAME, "red")
        );

        var results = parentPersistence.create(commands, parentFlow(validator).build());

        assertFalse(results.hasErrors(commands.get(0)));
        assertTrue(results.hasErrors(commands.get(1)));
        assertFalse(results.hasErrors(commands.get(2)));
    }

    private ChangeFlowConfig.Builder<ParentEntity> parentFlow(ChangesValidator<ParentEntity>... validators) {
        return ChangeFlowConfigBuilderFactory
                .newInstance(plContext, ParentEntity.INSTANCE)
                .withValidators(Seq.of(validators).toList())
                ;
    }

    private static class CreateParent extends CreateEntityCommand<ParentEntity> implements EntityCommandExt<ParentEntity, CreateParent> {
        public CreateParent() {
            super(ParentEntity.INSTANCE);
        }
    }

    private void create(CreateParent... parents) {
        parentPersistence.create(asList(parents), parentFlow().build());
    }
}
