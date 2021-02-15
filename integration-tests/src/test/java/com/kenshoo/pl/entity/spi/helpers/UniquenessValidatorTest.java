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

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.jooq.lambda.function.Functions.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UniquenessValidatorTest {

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
    public void testDontFailIfNotSameValueBulk() {
        UniqueKey<ParentEntity> uniqueness = new UniqueKey<>(asList(ParentEntity.NAME));
        UniquenessValidator<ParentEntity> validator = new UniquenessValidator.Builder<>(entitiesFetcher, uniqueness).build();

        List<CreateParent> commands = asList(
                new CreateParent().with(ParentEntity.ID, 1).with(ParentEntity.NAME, "moshe"),
                new CreateParent().with(ParentEntity.ID, 2).with(ParentEntity.NAME, "moshe2")
        );

        CreateResult<ParentEntity, Identifier<ParentEntity>> results = parentPersistence.create(commands, parentFlow(validator).build());

        assertFalse(results.hasErrors());
    }

    @Test
    public void testFail3rdCommandWhenDuplicationIsWithinTheBulk() {
        UniqueKey<ParentEntity> uniqueness = new UniqueKey<>(asList(ParentEntity.NAME));
        UniquenessValidator<ParentEntity> validator = new UniquenessValidator.Builder<>(entitiesFetcher, uniqueness).build();

        List<CreateParent> commands = asList(
                new CreateParent().with(ParentEntity.ID, 1).with(ParentEntity.NAME, "moshe"),
                new CreateParent().with(ParentEntity.ID, 2).with(ParentEntity.NAME, "david"),
                new CreateParent().with(ParentEntity.ID, 3).with(ParentEntity.NAME, "moshe")
        );

        CreateResult<ParentEntity, Identifier<ParentEntity>> results = parentPersistence.create(commands, parentFlow(validator).build());

        assertThat(failedCommands(results), contains(commands.get(2)));
    }

    @Test
    public void testFailCommandWhenKeyAlreadyExistsInDb() {
        UniqueKey<ParentEntity> uniqueness = new UniqueKey<>(asList(ParentEntity.NAME));
        UniquenessValidator<ParentEntity> validator = new UniquenessValidator.Builder<>(entitiesFetcher, uniqueness)
                .setErrorCode("I found a duplication")
                .build();

        create(new CreateParent().with(ParentEntity.ID, 99).with(ParentEntity.NAME, "moshe"));

        List<CreateParent> commands = asList(
                new CreateParent().with(ParentEntity.ID, 1).with(ParentEntity.NAME, "moshe"),
                new CreateParent().with(ParentEntity.ID, 2).with(ParentEntity.NAME, "david")
        );

        CreateResult<ParentEntity, Identifier<ParentEntity>> results = parentPersistence.create(commands, parentFlow(validator).build());

        assertThat(failedCommands(results), contains(commands.get(0)));
        assertThat(firstError(results).getErrorCode(), is("I found a duplication"));
        assertThat(firstError(results).getParameters().get("ID"), is("99"));
    }

    private ValidationError firstError(CreateResult<ParentEntity, Identifier<ParentEntity>> results) {
        return results.getChangeResults().iterator().next().getErrors().iterator().next();
    }

    @Test
    public void testDontFailWhenOnlyPartOfTheUniqueKeyMatched() {

        UniqueKey<ParentEntity> uniqueness = new UniqueKey<>(asList(
                ParentEntity.NAME,
                ParentEntity.ID_IN_TARGET
        ));

        create(new CreateParent().with(ParentEntity.ID, 99)
                .with(ParentEntity.NAME, "moshe")
                .with(ParentEntity.ID_IN_TARGET, 30)
        );

        UniquenessValidator<ParentEntity> validator = new UniquenessValidator.Builder<>(entitiesFetcher, uniqueness).build();

        List<CreateParent> commands = asList(
                new CreateParent()
                        .with(ParentEntity.ID, 1)
                        .with(ParentEntity.NAME, "moshe")
                        .with(ParentEntity.ID_IN_TARGET, null)
        );

        CreateResult<ParentEntity, Identifier<ParentEntity>> results = parentPersistence.create(commands, parentFlow(validator).build());

        assertFalse(results.hasErrors());
    }

    @Test
    public void testFailWhenAllPartsOfTheUniqueKeyMatched() {

        UniqueKey<ParentEntity> uniqueness = new UniqueKey<>(asList(
                ParentEntity.NAME,
                ParentEntity.ID_IN_TARGET
        ));

        create(new CreateParent().with(ParentEntity.ID, 99)
                .with(ParentEntity.NAME, "moshe")
                .with(ParentEntity.ID_IN_TARGET, 3333)
        );

        UniquenessValidator<ParentEntity> validator = new UniquenessValidator.Builder<>(entitiesFetcher, uniqueness).build();

        List<CreateParent> commands = asList(
                new CreateParent()
                        .with(ParentEntity.ID, 1)
                        .with(ParentEntity.NAME, "moshe")
                        .with(ParentEntity.ID_IN_TARGET, 3333)
        );

        CreateResult<ParentEntity, Identifier<ParentEntity>> results = parentPersistence.create(commands, parentFlow(validator).build());

        assertTrue(results.hasErrors());
    }

    @Test
    public void testDontFailBulkCommandsWhenConditionIsUnmatched() {
        final var validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, new UniqueKey<>(List.of(ParentEntity.NAME)))
                .setCondition(PLCondition.not(ParentEntity.ID_IN_TARGET.isNull()))
                .build();

        final var entity1 = new CreateParent()
                .with(ParentEntity.ID, 99)
                .with(ParentEntity.NAME, "moshe")
                .with(ParentEntity.ID_IN_TARGET, null);

        final var entity2 = new CreateParent()
                .with(ParentEntity.ID, 1)
                .with(ParentEntity.NAME, "moshe")
                .with(ParentEntity.ID_IN_TARGET, 55);

        final var results = parentPersistence.create(List.of(entity1, entity2), parentFlow(validator).build());

        assertThat(results.hasErrors(), is(false));
    }

    @Test
    public void testFailBulkCommandsWhenConditionIsMatched() {
        final var validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, new UniqueKey<>(List.of(ParentEntity.NAME)))
                .setCondition(PLCondition.not(ParentEntity.ID_IN_TARGET.isNull()))
                .build();

        final var entity1 = new CreateParent()
                .with(ParentEntity.ID, 99)
                .with(ParentEntity.NAME, "moshe")
                .with(ParentEntity.ID_IN_TARGET, 44);

        final var entity2 = new CreateParent()
                .with(ParentEntity.ID, 1)
                .with(ParentEntity.NAME, "moshe")
                .with(ParentEntity.ID_IN_TARGET, 55);

        final var results = parentPersistence.create(List.of(entity1, entity2), parentFlow(validator).build());

        assertThat(results.hasErrors(), is(true));
    }

    @Test
    public void testDontFailCommandConditionIsUnmatched() {
        UniqueKey<ParentEntity> uniqueness = new UniqueKey<>(asList(ParentEntity.NAME));

        UniquenessValidator<ParentEntity> validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, uniqueness)
                .setCondition(PLCondition.not(ParentEntity.ID_IN_TARGET.isNull()))
                .build();

        create(new CreateParent().with(ParentEntity.ID, 99)
                .with(ParentEntity.NAME, "moshe")
                .with(ParentEntity.ID_IN_TARGET, null));

        List<CreateParent> commands = asList(
                new CreateParent().with(ParentEntity.ID, 1)
                        .with(ParentEntity.NAME, "moshe")
                        .with(ParentEntity.ID_IN_TARGET, 12345)
        );

        CreateResult<ParentEntity, Identifier<ParentEntity>> results = parentPersistence.create(commands, parentFlow(validator).build());

        assertFalse(results.hasErrors());
    }


    private List<CreateEntityCommand<ParentEntity>> failedCommands(CreateResult<ParentEntity, Identifier<ParentEntity>> results) {
        return results.getChangeResults().stream().filter(not(r -> r.isSuccess())).map(r -> r.getCommand()).collect(toList());
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
