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
    private static final ChildTable childTable = ChildTable.INSTANCE;
    private static final Set<DataTable> ALL_TABLES = ImmutableSet.of(parentTable, childTable);
    private static final String UNIQUE_NAME = "Moshe";

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
    public void whenConditionChecksFieldsOfOtherRelatedEntities_dontFailBulkCommandsIfItIsUnmatched() {
        final var validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, new UniqueKey<>(List.of(ChildEntity.PARENT_ID, ChildEntity.ORDINAL)))
                .setFetchCondition(ParentEntity.NAME.eq("moshe"))
                .setPostFetchCondition(ParentEntity.NAME.postFetchEq("moshe"))
                .build();

        create(new CreateParent().with(ParentEntity.ID, 99).with(ParentEntity.NAME, "aaa"));

        final var child1 = new CreateChild()
                .with(ChildEntity.ID, 1)
                .with(ChildEntity.PARENT_ID, 99)
                .with(ChildEntity.ORDINAL, 22);

        final var child2 = new CreateChild()
                .with(ChildEntity.ID, 2)
                .with(ChildEntity.PARENT_ID, 99)
                .with(ChildEntity.ORDINAL, 22);

        final var results = childPersistence.create(List.of(child1, child2), childFlow(validator).build());

        assertThat(results.hasErrors(), is(false));
    }

    @Test
    public void whenConditionChecksFieldsOfOtherRelatedEntities_failBulkCommandsIfItIsMatched() {
        final var validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, new UniqueKey<>(List.of(ChildEntity.PARENT_ID, ChildEntity.ORDINAL)))
                .setFetchCondition(ParentEntity.NAME.eq("moshe"))
                .setPostFetchCondition(ParentEntity.NAME.postFetchEq("moshe"))
                .build();

        create(new CreateParent().with(ParentEntity.ID, 99).with(ParentEntity.NAME, "moshe"));

        final var child1 = new CreateChild()
                .with(ChildEntity.ID, 1)
                .with(ChildEntity.PARENT_ID, 99)
                .with(ChildEntity.ORDINAL, 22);

        final var child2 = new CreateChild()
                .with(ChildEntity.ID, 2)
                .with(ChildEntity.PARENT_ID, 99)
                .with(ChildEntity.ORDINAL, 22);

        final var results = childPersistence.create(List.of(child1, child2), childFlow(validator).build());

        assertThat(results.hasErrors(), is(true));
    }

    @Test
    public void testDontFailCommandWhenSameUniqueKeyInBulkButConditionIsUnmatched() {
        final var validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, new UniqueKey<>(List.of(ParentEntity.NAME)))
                .setFetchCondition(PLCondition.not(ParentEntity.ID_IN_TARGET.isNull()))
                .setPostFetchCondition(PLPostFetchCondition.not(ParentEntity.ID_IN_TARGET.postFetchIsNull()))
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
    public void testFailCommandWhenSameUniqueKeyInBulkAndConditionIsMatched() {
        final var validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, new UniqueKey<>(List.of(ParentEntity.NAME)))
                .setFetchCondition(PLCondition.not(ParentEntity.ID_IN_TARGET.isNull()))
                .setPostFetchCondition(PLPostFetchCondition.not(ParentEntity.ID_IN_TARGET.postFetchIsNull()))
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
    public void testDontFailWhenAllCommandsUnmatchedCondition() {
        final var validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, new UniqueKey<>(List.of(ParentEntity.NAME)))
                .setFetchCondition(PLCondition.not(ParentEntity.ID_IN_TARGET.isNull()))
                .setPostFetchCondition(PLPostFetchCondition.not(ParentEntity.ID_IN_TARGET.postFetchIsNull()))
                .build();

        final var entity1 = new CreateParent()
                .with(ParentEntity.ID, 99)
                .with(ParentEntity.NAME, "moshe")
                .with(ParentEntity.ID_IN_TARGET, null);

        final var entity2 = new CreateParent()
                .with(ParentEntity.ID, 1)
                .with(ParentEntity.NAME, "moshe")
                .with(ParentEntity.ID_IN_TARGET, null);

        final var results = parentPersistence.create(List.of(entity1, entity2), parentFlow(validator).build());

        assertThat(results.hasErrors(), is(false));
    }

    @Test
    public void testDontFailCommandWhenSameUniqueKeyInDBButConditionIsUnmatched() {
        UniqueKey<ParentEntity> uniqueness = new UniqueKey<>(asList(ParentEntity.NAME));

        UniquenessValidator<ParentEntity> validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, uniqueness)
                .setFetchCondition(PLCondition.not(ParentEntity.ID_IN_TARGET.isNull()))
                .setPostFetchCondition(PLPostFetchCondition.not(ParentEntity.ID_IN_TARGET.postFetchIsNull()))
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

    @Test
    public void testFailCommandWhenSameUniqueKeyInDBAndConditionIsMatched() {
        final var validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, new UniqueKey<>(List.of(ParentEntity.NAME)))
                .setFetchCondition(PLCondition.not(ParentEntity.ID_IN_TARGET.isNull()))
                .setPostFetchCondition(PLPostFetchCondition.not(ParentEntity.ID_IN_TARGET.postFetchIsNull()))
                .build();

        create(new CreateParent().with(ParentEntity.ID, 99)
                .with(ParentEntity.NAME, "moshe")
                .with(ParentEntity.ID_IN_TARGET, 333));

        final var commands = List.of(
                new CreateParent().with(ParentEntity.ID, 1)
                        .with(ParentEntity.NAME, "moshe")
                        .with(ParentEntity.ID_IN_TARGET, 12345)
        );

        final var results = parentPersistence.create(commands, parentFlow(validator).build());

        assertThat(results.hasErrors(), is(true));
    }

//    @Test
//    public void testFailCommandWhenSameUniqueKeyInDBExistsMoreAndAlreadyDuplicateAndConditionIsMatched() {
//        final var validator = new UniquenessValidator
//                .Builder<>(entitiesFetcher, new UniqueKey<>(List.of(ParentEntity.NAME)))
//                .setCondition(PLCondition.not(ParentEntity.ID_IN_TARGET.isNull()))
//                .build();
//
//        create(new CreateParent().with(ParentEntity.ID, 99)
//                .with(ParentEntity.NAME, UNIQUE_NAME) .with(ParentEntity.ID_IN_TARGET, 333),
//
//                new CreateParent().with(ParentEntity.ID, 98)
//                .with(ParentEntity.NAME, UNIQUE_NAME).with(ParentEntity.ID_IN_TARGET, 4444));
//
//
//
//        final var commands = List.of(
//                new CreateParent().with(ParentEntity.ID, 1)
//                        .with(ParentEntity.NAME, UNIQUE_NAME)
//                        .with(ParentEntity.ID_IN_TARGET, 12345)
//        );
//
//        final var results = parentPersistence.create(commands, parentFlow(validator).build());
//
//        assertThat(results.hasErrors(), is(true));
//    }

    @Test
    public void testDontFailUpdateCommandOnItselfInDBWhenNoChangeInUniqueKeyFields() {
        final var validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, new UniqueKey<>(List.of(ParentEntity.NAME)))
                .setOperation(SupportedChangeOperation.CREATE_AND_UPDATE)
                .build();

        create(new CreateParent().with(ParentEntity.ID, 99).with(ParentEntity.NAME, "moshe"));

        final var updateCommand = new UpdateParent(99).with(ParentEntity.NAME, "moshe");

        final var results = parentPersistence.update(List.of(updateCommand), parentFlow(validator).build());

        assertThat(results.hasErrors(), is(false));
    }

    @Test
    public void testNullInUniqueKeyIsConsideredAsValueForComparison() {
        final var validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, new UniqueKey<>(List.of(ParentEntity.NAME)))
                .build();

        final var entity1 = new CreateParent()
                .with(ParentEntity.ID, 99)
                .with(ParentEntity.NAME, null);

        final var entity2 = new CreateParent()
                .with(ParentEntity.ID, 1)
                .with(ParentEntity.NAME, null);

        final var entity3 = new CreateParent()
                .with(ParentEntity.ID, 1)
                .with(ParentEntity.NAME, "moshe");

        final var results = parentPersistence.create(List.of(entity1, entity2, entity3), parentFlow(validator).build());

        assertThat(results.hasErrors(), is(true));
        assertThat(results.getErrors(entity2).isEmpty(), is(false));
        assertThat(results.getErrors(entity3).isEmpty(), is(true));
    }

    @Test
    public void testNoExceptionsWhenCommandHasNoValueForUniqueKeyField() {
        final var validator = new UniquenessValidator
                .Builder<>(entitiesFetcher, new UniqueKey<>(List.of(ParentEntity.NAME)))
                .build();

        final var entity1 = new CreateParent()
                .with(ParentEntity.ID, 1);

        final var results = parentPersistence.create(List.of(entity1), parentFlow(validator).build());

        assertThat(results.hasErrors(), is(false));
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

    private static class CreateChild extends CreateEntityCommand<ChildEntity> implements EntityCommandExt<ChildEntity, CreateChild> {
        public CreateChild() {
            super(ChildEntity.INSTANCE);
        }
    }

    private static class UpdateParent extends UpdateEntityCommand<ParentEntity, ParentEntity.Key> implements EntityCommandExt<ParentEntity, UpdateParent> {
        public UpdateParent(int id) {
            super(ParentEntity.INSTANCE, new ParentEntity.Key(id));
        }
    }

    private void create(CreateParent... parents) {
        parentPersistence.create(asList(parents), parentFlow().build());
    }
}
