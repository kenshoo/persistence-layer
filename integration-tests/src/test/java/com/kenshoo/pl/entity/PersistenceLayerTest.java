package com.kenshoo.pl.entity;

import com.google.common.collect.*;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TempTableResource;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.data.*;
import com.kenshoo.pl.entity.internal.ChangesContainer;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.entity.internal.EntityDbUtil;
import com.kenshoo.pl.entity.internal.Errors;
import com.kenshoo.pl.entity.spi.*;
import com.kenshoo.pl.entity.spi.helpers.CommandsFieldMatcher;
import com.kenshoo.pl.entity.spi.helpers.EntitiesTempTableCreator;
import com.kenshoo.pl.entity.spi.helpers.EntityChangeCompositeValidator;
import com.kenshoo.pl.entity.spi.helpers.FixedFieldValueSupplier;
import org.hamcrest.Matchers;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.EntityForTest.URL;
import static com.kenshoo.pl.entity.spi.FieldValueSupplier.fromOldValue;
import static com.kenshoo.pl.entity.spi.FieldValueSupplier.fromValues;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PersistenceLayerTest {

    public static final String DOODLE_URL = "http://doodle.com";
    public static final String GOOGLE_URL = "http://google.com";

    private static boolean tablesCreated = false;
    private static DSLContext staticDSLContext;

    private DSLContext dslContext = TestJooqConfig.create();

    private EntityForTestTable mainTable;
    private EntityForTestSecondaryTable secondaryTable;
    private EntityForTestParentTable parentTable;
    private EntityForTestComplexKeyParentTable complexKeyParentTable;
    private PLContext plContext;

    private PersistenceLayer<EntityForTest> persistenceLayer;
    private EntitiesFetcher entitiesFetcher;
    private PersistenceLayer<EntityForTestParent> persistenceLayerParent;
    private EntitiesTempTableCreator entitiesTempTableCreator;

    private static final int ID_1 = 1;
    private static final int ID_2 = 2;
    private static final int PARENT_ID_1 = 10;
    private static final int PARENT_ID_2 = 22;
    private static final int COMPLEX_PARENT_ID_1_1 = 101;
    private static final int COMPLEX_PARENT_ID_1_2 = 102;
    private static final int COMPLEX_PARENT_ID_2_1 = 201;
    private static final int COMPLEX_PARENT_ID_2_2 = 102;
    private static final int FIELD2_1_ORIGINAL_VALUE = 10;
    private static final int FIELD2_2_ORIGINAL_VALUE = 20;
    private static final int IGNORABLE_1_ORIGINAL_VALUE = 10;
    private static final int IGNORABLE_2_ORIGINAL_VALUE = 20;

    private static final Object[][] DATA = {
            {ID_1, TestEnum.Alpha.name(), FIELD2_1_ORIGINAL_VALUE, new Timestamp(Instant.now().toEpochMilli()), "key1", "value1", PARENT_ID_1, COMPLEX_PARENT_ID_1_1, COMPLEX_PARENT_ID_1_2, IGNORABLE_1_ORIGINAL_VALUE},
            {ID_2, TestEnum.Bravo.name(), FIELD2_2_ORIGINAL_VALUE, new Timestamp(Instant.now().toEpochMilli()), "key2", "value2", PARENT_ID_2, COMPLEX_PARENT_ID_2_1, COMPLEX_PARENT_ID_2_2, IGNORABLE_2_ORIGINAL_VALUE},
    };
    private static final Object[][] SECONDARY_DATA = {
            {ID_1, GOOGLE_URL, ""},
            {ID_2, DOODLE_URL, ""},
    };
    private static final Object[][] PARENT_DATA = {
            {PARENT_ID_1, "1000"},
            {PARENT_ID_2, "2000"},
    };
    private static final Object[][] COMPLEX_PARENT_DATA = {
            {COMPLEX_PARENT_ID_1_1, COMPLEX_PARENT_ID_1_2, "256"},
            {COMPLEX_PARENT_ID_2_1, COMPLEX_PARENT_ID_2_2, "512"},
    };

    private static final TestEnum FIELD1_VALID_VALUE = TestEnum.Zeta;
    private static final TestEnum FIELD1_INVALID_VALUE = TestEnum.Alpha;
    private static final int FIELD2_VALID_VALUE = 100;
    private static final int FIELD2_INVALID_VALUE = 1000;
    private static final String FIELD1_ERROR = "Invalid.FIELD1.Value";
    private static final String FIELD2_ERROR = "Invalid.FIELD2.Value";
    private static final String CANNOT_CREATE_IN_PARENT = "Cannot";

    @Before
    public void populateTables() {

        persistenceLayer = new PersistenceLayer<>(dslContext);
        persistenceLayerParent = new PersistenceLayer<>(dslContext);
        entitiesFetcher = new EntitiesFetcher(dslContext);
        entitiesTempTableCreator = new EntitiesTempTableCreator(dslContext);

        staticDSLContext = dslContext;
        plContext = new PLContext.Builder(dslContext).build();

        mainTable = EntityForTestTable.INSTANCE;
        if (!tablesCreated) {
            DataTableUtils.createTable(dslContext, mainTable);
            DataTableUtils.createTable(dslContext, ChildForTestTable.INSTANCE);
        }
        DataTableUtils.populateTable(dslContext, mainTable, DATA);

        secondaryTable = EntityForTestSecondaryTable.INSTANCE;
        if (!tablesCreated) {
            DataTableUtils.createTable(dslContext, secondaryTable);
            dslContext.execute(String.format("ALTER TABLE %s MODIFY COLUMN %s %s auto_increment",
                    secondaryTable.getName(),
                    secondaryTable.id.getName(),
                    secondaryTable.id.getDataType().getTypeName()));
            dslContext.alterTable(secondaryTable).add(DSL.constraint("indexName").unique(secondaryTable.entityId)).execute();
        }
        // Not using DataTableUtils because it doesn't support auto-increment
        BatchBindStep batch = dslContext.batch(dslContext.insertInto(secondaryTable, secondaryTable.entityId, secondaryTable.url).values((Integer) null, null));
        for (Object[] values : SECONDARY_DATA) {
            batch.bind(values);
        }
        batch.execute();

        parentTable = EntityForTestParentTable.INSTANCE;
        if (!tablesCreated) {
            DataTableUtils.createTable(dslContext, parentTable);
        }
        DataTableUtils.populateTable(dslContext, parentTable, PARENT_DATA);

        complexKeyParentTable = EntityForTestComplexKeyParentTable.INSTANCE;
        if (!tablesCreated) {
            DataTableUtils.createTable(dslContext, complexKeyParentTable);
        }
        DataTableUtils.populateTable(dslContext, complexKeyParentTable, COMPLEX_PARENT_DATA);

        tablesCreated = true;
    }

    @After
    public void clearTables() {
        dslContext.deleteFrom(mainTable).execute();
        dslContext.deleteFrom(secondaryTable).execute();
        dslContext.deleteFrom(parentTable).execute();
        dslContext.deleteFrom(complexKeyParentTable).execute();
        dslContext.truncate("ChildForTest").execute();
    }

    @AfterClass
    public static void dropTables() {
        staticDSLContext.dropTableIfExists(EntityForTestTable.INSTANCE).execute();
        staticDSLContext.dropTableIfExists(EntityForTestSecondaryTable.INSTANCE).execute();
        staticDSLContext.dropTableIfExists(EntityForTestParentTable.INSTANCE).execute();
        staticDSLContext.dropTableIfExists(EntityForTestComplexKeyParentTable.INSTANCE).execute();
        staticDSLContext.dropTableIfExists(ChildForTestTable.INSTANCE).execute();
    }

    @Test
    public void fetchFieldFromSecondaryTableOfParentTableWhereParentItselfIsNotRequired() {

        CreateEntityCommand<ChildForTest> cmd = new CreateEntityCommand<>(ChildForTest.INSTANCE);
        cmd.set(ChildForTest.ID, 1);
        cmd.set(ChildForTest.PARENT_ID, ID_1);
        cmd.set(ChildForTest.FIELD, fromOldValue(URL, parentUrl -> parentUrl));

        childPL().create(asList(cmd), childFlow());

        Record childInDB = dslContext.selectFrom(ChildForTestTable.INSTANCE).where(ChildForTestTable.INSTANCE.id.eq(1)).fetchOne();

        assertThat(childInDB.get(ChildForTestTable.INSTANCE.field), is(GOOGLE_URL));
    }

    @Test
    public void fetchFieldFromSecondaryTableOfParentTableWhereParentIsAlsoRequired() {
        CreateEntityCommand<ChildForTest> cmd = new CreateEntityCommand<>(ChildForTest.INSTANCE);
        cmd.set(ChildForTest.ID, 1);
        cmd.set(ChildForTest.PARENT_ID, ID_1);
        cmd.set(ChildForTest.FIELD, fromValues(EntityForTest.FIELD1, URL, (v1, v2) -> v1.toString() + " " + v2));

        childPL().create(asList(cmd), childFlow());

        Record childInDB = dslContext.selectFrom(ChildForTestTable.INSTANCE).where(ChildForTestTable.INSTANCE.id.eq(1)).fetchOne();

        assertThat(childInDB.get(ChildForTestTable.INSTANCE.field), is("Alpha " + GOOGLE_URL));
    }

    @Test
    public void updateMainTable() {
        ArrayList<UpdateTestCommand> commands = new ArrayList<>();

        commands.add(createChangeCommand(ID_1, TestEnum.Zeta, 100));
        commands.add(createChangeCommand(ID_2, TestEnum.Alpha, 200));

        UpdateResult<EntityForTest, EntityForTest.Key> updateResult = persistenceLayer.update(commands, changeFlowConfig().build());

        assertThat(updateResult.getStats().getAffectedRowsOf(mainTable.getName()).getUpdated(), is(2));
        Map<Integer, Record3<Integer, String, Integer>> result =
                dslContext.select(mainTable.id, EntityForTestTable.INSTANCE.field1, EntityForTestTable.INSTANCE.field2)
                        .from(mainTable)
                        .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is(TestEnum.Zeta.name()));
        assertThat(result.get(ID_1).value3(), is(100));

        assertThat(result.get(ID_2).value2(), is(TestEnum.Alpha.name()));
        assertThat(result.get(ID_2).value3(), is(200));

    }

    @Test
    public void createMainTable() {
        int newId = 51;
        CreateTestCommand command1 = new CreateTestCommand();
        command1.set(EntityForTest.ID, newId);
        command1.set(EntityForTest.FIELD1, TestEnum.Charlie);
        command1.set(EntityForTest.PARENT_ID, PARENT_ID_1);
        CreateTestCommand command2 = new CreateTestCommand();
        command2.set(EntityForTest.ID, newId + 1);
        command2.set(EntityForTest.FIELD1, TestEnum.Delta);
        command2.set(EntityForTest.PARENT_ID, PARENT_ID_2);

        CreateResult<EntityForTest, Identifier<EntityForTest>> createResult = persistenceLayer.create(ImmutableList.of(command1, command2), changeFlowConfig().build());

        assertThat(createResult.getStats().getAffectedRowsOf(mainTable.getName()).getInserted(), is(2));
        assertThat(createResult.getStats().getAffectedRowsOf(mainTable.getName()).getUpdated(), is(0));
        Map<Integer, Record3<Integer, String, Integer>> result =
                dslContext.select(mainTable.id, EntityForTestTable.INSTANCE.field1, EntityForTestTable.INSTANCE.parent_id)
                        .from(mainTable)
                        .fetchMap(mainTable.id);

        assertThat(command1.getIdentifier().getValues().findFirst().get(), is(newId));
        assertThat(result.get(newId).value2(), is(TestEnum.Charlie.name()));
        assertThat(result.get(newId).value3(), is(PARENT_ID_1));

        assertThat(command2.getIdentifier().getValues().findFirst().get(), is(newId + 1));
        assertThat(result.get(newId + 1).value2(), is(TestEnum.Delta.name()));
        assertThat(result.get(newId + 1).value3(), is(PARENT_ID_2));
    }

    @Test
    public void updateToNull() {
        UpdateTestCommand command = new UpdateTestCommand(ID_1);

        command.set(EntityForTest.FIELD1, new FixedFieldValueSupplier<>(null));
        command.set(EntityForTest.FIELD2, new FixedFieldValueSupplier<>(null));

        persistenceLayer.update(Collections.singletonList(command), changeFlowConfig().build());

        Map<Integer, Record3<Integer, String, Integer>> result = dslContext.select(mainTable.id, mainTable.field1, mainTable.field2)
                .from(mainTable)
                .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), nullValue());
        assertThat(result.get(ID_1).value3(), nullValue());
    }

    @Test
    public void successfulMultiFieldSupplier() {
        UpdateTestCommand command = new UpdateTestCommand(ID_1);

        command.set(ImmutableList.of(EntityForTest.FIELD1, EntityForTest.FIELD2), new TestMultiFieldSupplier(null));

        persistenceLayer.update(Collections.singletonList(command), changeFlowConfig().build());

        Map<Integer, Record3<Integer, String, Integer>> result = dslContext.select(mainTable.id, mainTable.field1, mainTable.field2)
                .from(mainTable)
                .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is(FIELD1_VALID_VALUE.name()));
        assertThat(result.get(ID_1).value3(), is(FIELD2_VALID_VALUE));
    }

    @Test
    public void successfulForNotAllFieldsMultiFieldSupplier() {
        UpdateTestCommand command = new UpdateTestCommand(ID_1);

        command.set(ImmutableList.of(EntityForTest.FIELD1, EntityForTest.FIELD2), new TestField1MultiFieldSupplier());

        persistenceLayer.update(Collections.singletonList(command), changeFlowConfig().build());

        Map<Integer, Record3<Integer, String, Integer>> result = dslContext.select(mainTable.id, mainTable.field1, mainTable.field2)
                .from(mainTable)
                .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is(FIELD1_VALID_VALUE.name()));
        assertThat(result.get(ID_1).value3(), is(FIELD2_1_ORIGINAL_VALUE));
    }

    @Test
    public void multiFieldSupplierUpdatingToNull() {
        UpdateTestCommand command = new UpdateTestCommand(ID_1);

        command.set(ImmutableList.of(EntityForTest.FIELD1, EntityForTest.FIELD2),
                new NullingMultiFieldSupplier(Collections.singletonList(EntityForTest.FIELD1)));

        persistenceLayer.update(Collections.singletonList(command), changeFlowConfig().build());

        Map<Integer, Record3<Integer, String, Integer>> result = dslContext.select(mainTable.id, mainTable.field1, mainTable.field2)
                .from(mainTable)
                .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), nullValue());
        assertThat(result.get(ID_1).value3(), is(FIELD2_1_ORIGINAL_VALUE));
    }

    @Test
    public void fieldNotDeclaredOnMultiFieldSupplierRegistrationIsNotUpdated() {
        UpdateTestCommand command = new UpdateTestCommand(ID_1);

        command.set(ImmutableList.of(EntityForTest.FIELD1), new TestMultiFieldSupplier(null));

        persistenceLayer.update(Collections.singletonList(command), changeFlowConfig().build());

        Map<Integer, Record3<Integer, String, Integer>> result = dslContext.select(mainTable.id, mainTable.field1, mainTable.field2)
                .from(mainTable)
                .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is(FIELD1_VALID_VALUE.name()));
        assertThat(result.get(ID_1).value3(), is(not(FIELD2_VALID_VALUE)));
        assertThat(result.get(ID_1).value3(), is(notNullValue()));
    }

    @Test
    public void failedMultiFieldSupplierWillCauseCommandToBeFilterOut() {
        UpdateTestCommand command = new UpdateTestCommand(ID_1);

        command.set(ImmutableList.of(EntityForTest.FIELD1, EntityForTest.FIELD2), new TestMultiFieldSupplier(new ValidationError("I'm a meaningless error")));

        UpdateResult<EntityForTest, EntityForTest.Key> updateResults = persistenceLayer.update(Collections.singletonList(command), changeFlowConfig().build());

        Map<Integer, Record3<Integer, String, Integer>> result = dslContext.select(mainTable.id, mainTable.field1, mainTable.field2)
                .from(mainTable)
                .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is(not(FIELD1_VALID_VALUE.name())));
        assertThat(result.get(ID_1).value3(), is(not(FIELD2_VALID_VALUE)));

        assertThat(updateResults.hasErrors(command), is(true));
    }


    @Test
    public void updateComplexField() {
        UpdateTestCommand command = new UpdateTestCommand(ID_1);
        command.set(EntityForTest.COMPLEX_FIELD, "newKey:newValue");
        persistenceLayer.update(ImmutableList.of(command), changeFlowConfig().build());

        Map<Integer, Record3<Integer, String, String>> result =
                dslContext.select(mainTable.id, EntityForTestTable.INSTANCE.complexFieldKey, EntityForTestTable.INSTANCE.complexFieldValue)
                        .from(mainTable)
                        .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is("newKey"));
        assertThat(result.get(ID_1).value3(), is("newValue"));
    }

    @Test
    public void lambdaSupplier() {
        UpdateTestCommand command = new UpdateTestCommand(ID_2);

        command.set(EntityForTest.FIELD2, fromOldValue(EntityForTest.FIELD2, x -> x + 10));

        persistenceLayer.update(asList(command), changeFlowConfig().build());

        assertThat(fetchField(ID_2, mainTable.field2), is(FIELD2_2_ORIGINAL_VALUE + 10));
    }

    @Test
    public void lambdaSupplierWithParentField() {
        UpdateTestCommand command = new UpdateTestCommand(ID_2);

        command.set(EntityForTest.FIELD2, fromValues(EntityForTest.FIELD2, EntityForTest.PARENT_ID, (field2, parentId) -> field2 + 2 * parentId));

        persistenceLayer.update(Collections.singletonList(command), changeFlowConfig().build());

        assertThat(fetchField(ID_2, mainTable.field2), is(20 + 2 * 22));
    }


    private ChangeFlowConfig.Builder<EntityForTest> changeFlowConfig() {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, EntityForTest.INSTANCE);
    }

    private UpdateTestCommand createChangeCommand(int id, TestEnum field1Value, int field2Value) {
        UpdateTestCommand command = new UpdateTestCommand(id);
        command.set(EntityForTest.FIELD1, field1Value);
        command.set(EntityForTest.FIELD2, field2Value);
        return command;
    }

    @Test
    public void updateVirtualField() {
        UpdateTestCommand command = new UpdateTestCommand(ID_1);
        command.set(EntityForTest.VIRTUAL_FIELD, "Garbage");

        persistenceLayer.update(ImmutableList.of(command), changeFlowConfig().build());

        Map<Integer, Record3<Integer, String, Integer>> result =
                dslContext.select(mainTable.id, EntityForTestTable.INSTANCE.field1, EntityForTestTable.INSTANCE.field2)
                        .from(mainTable)
                        .fetchMap(mainTable.id);

        // There should be no change
        assertThat(result.get(ID_1).value2(), is(TestEnum.Alpha.name()));
        assertThat(result.get(ID_1).value3(), is(10));
    }

    @Test
    public void outputGeneratorFetchWithSecondary() {
        ArrayList<UpdateTestCommand> commands = new ArrayList<>();
        commands.add(createChangeCommand(ID_1, TestEnum.Zeta, 100));
        commands.add(createChangeCommand(ID_2, TestEnum.Alpha, 200));

        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig().withOutputGenerator(new TestOutputGeneratorWithSecondary(plContext)).build();

        persistenceLayer.update(commands, flowConfig);

        Map<Integer, Record2<Integer, String>> result = dslContext.select(secondaryTable.id, secondaryTable.url)
                .from(secondaryTable)
                .fetchMap(secondaryTable.id);

        assertThat(result.get(ID_1).value2(), is(GOOGLE_URL.replace("http", "https")));
        assertThat(result.get(ID_2).value2(), is(DOODLE_URL.replace("http", "https")));
    }

    @Test
    public void outputGeneratorFetchWithParent() {
        ArrayList<UpdateTestCommand> commands = new ArrayList<>();
        commands.add(createChangeCommand(ID_1, TestEnum.Zeta, 100));
        commands.add(createChangeCommand(ID_2, TestEnum.Alpha, 200));

        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig().withOutputGenerator(new TestOutputGeneratorWithParent(plContext)).build();

        persistenceLayer.update(commands, flowConfig);

        Map<Integer, Record2<Integer, Integer>> result = dslContext.select(mainTable.id, mainTable.field2)
                .from(mainTable)
                .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is(1000));
        assertThat(result.get(ID_2).value2(), is(2000));
    }

    @Test
    public void commandEnricherFetchWithParent() {
        ArrayList<UpdateTestCommand> commands = new ArrayList<>();
        commands.add(createChangeCommand(ID_1, TestEnum.Zeta, 100));
        commands.add(createChangeCommand(ID_2, TestEnum.Alpha, 200));

        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig().withPostFetchCommandEnricher(new TestEnricherWithParent()).build();

        persistenceLayer.update(commands, flowConfig);

        Map<Integer, Record2<Integer, Integer>> result = dslContext.select(mainTable.id, mainTable.field2)
                .from(mainTable)
                .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is(1000));
        assertThat(result.get(ID_2).value2(), is(2000));
    }

    @Test
    public void fetchWithComplexKeyParent() {
        ArrayList<UpdateTestCommand> commands = new ArrayList<>();
        UpdateTestCommand command = new UpdateTestCommand(ID_1);
        command.set(EntityForTest.FIELD1, TestEnum.Charlie);
        commands.add(command);
        PostFetchCommandEnricher<EntityForTest> enricher = new PostFetchCommandEnricher<EntityForTest>() {
            @Override
            public void enrich(Collection<? extends ChangeEntityCommand<EntityForTest>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
                ChangeEntityCommand<EntityForTest> command = Iterables.getFirst(changeEntityCommands, null);
                assertNotNull(command);
                CurrentEntityState entity = changeContext.getEntity(command);
                command.set(EntityForTest.FIELD2, Integer.parseInt(entity.get(EntityForTestComplexKeyParent.FIELD1)));
            }

            @Override
            public Stream<EntityField<EntityForTest, ?>> fieldsToEnrich() {
                return Stream.of(EntityForTest.FIELD2);
            }

            @Override
            public boolean shouldRun(Collection<? extends EntityChange<EntityForTest>> changeEntityCommands) {
                return CommandsFieldMatcher.isAnyFieldContainedInAnyCommand(changeEntityCommands, EntityForTest.FIELD2);
            }

            @Override
            public SupportedChangeOperation getSupportedChangeOperation() {
                return SupportedChangeOperation.UPDATE;
            }

            @Override
            public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<EntityForTest, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
                return Stream.of(EntityForTestComplexKeyParent.FIELD1);
            }
        };

        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig().withPostFetchCommandEnricher(enricher).build();

        persistenceLayer.update(commands, flowConfig);

        Record3<Integer, String, Integer> result = dslContext.select(mainTable.id, mainTable.field1, mainTable.field2)
                .from(mainTable)
                .where(mainTable.id.eq(1))
                .fetchOne();

        assertThat(result.value2(), is(TestEnum.Charlie.name()));
        assertThat(result.value3(), is(256));
    }

    @Test
    public void validField2value() {
        ArrayList<UpdateTestCommand> commands = new ArrayList<>();
        commands.add(createChangeCommand(ID_1, TestEnum.Zeta, FIELD2_VALID_VALUE));

        EntityChangeCompositeValidator<EntityForTest> changesCompositeValidator = new EntityChangeCompositeValidator<>();
        changesCompositeValidator.register(new TestField2Validator());
        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig().withValidator(changesCompositeValidator).build();

        persistenceLayer.update(commands, flowConfig);

        Map<Integer, Record2<Integer, Integer>> result = dslContext.select(mainTable.id, mainTable.field2)
                .from(mainTable)
                .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is(100));

    }

    @Test
    public void validField2InvalidValueForUpdate() {
        ArrayList<UpdateTestCommand> commands = new ArrayList<>();
        commands.add(createChangeCommand(ID_1, TestEnum.Zeta, FIELD2_INVALID_VALUE));

        EntityChangeCompositeValidator<EntityForTest> changesCompositeValidator = new EntityChangeCompositeValidator<>();
        changesCompositeValidator.register(new TestField2Validator());
        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig().withValidator(changesCompositeValidator).build();

        UpdateResult<EntityForTest, EntityForTest.Key> updateResult = persistenceLayer.update(commands, flowConfig);

        Collection<ValidationError> validationErrors = updateResult.getErrors(commands.get(0));
        assertThat(validationErrors.iterator().next().getErrorCode(), is(FIELD2_ERROR));
    }


    @Test
    public void validField2InvalidValueForCreateValidation() {
        ArrayList<CreateTestCommand> commands = new ArrayList<>();
        CreateTestCommand command = new CreateTestCommand();
        command.set(EntityForTest.ID, 11);
        command.set(EntityForTest.FIELD1, TestEnum.Zeta);
        command.set(EntityForTest.FIELD2, FIELD2_INVALID_VALUE);
        command.set(EntityForTest.PARENT_ID, 10);
        commands.add(command);

        EntityChangeCompositeValidator<EntityForTest> changesCompositeValidator = new EntityChangeCompositeValidator<>();
        changesCompositeValidator.register(new TestField2Validator());
        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig()
                .withValidator(changesCompositeValidator).build();

        CreateResult<EntityForTest, EntityForTest.Key> createResult = persistenceLayer.create(commands, flowConfig, EntityForTest.Key.DEFINITION);

        Collection<ValidationError> validationErrors = createResult.getErrors(command);
        assertThat(validationErrors.iterator().next().getErrorCode(), is(FIELD2_ERROR));
    }

    @Test
    public void immutableFieldsValidation() {
        UpdateTestCommand command = new UpdateTestCommand(ID_1);
        command.set(EntityForTest.IMMUTABLE_FIELD, "test");

        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig().build();

        UpdateResult<EntityForTest, EntityForTest.Key> updateResult = persistenceLayer.update(ImmutableList.of(command), flowConfig);

        assertThat(updateResult.hasErrors(), is(true));
        Collection<ValidationError> validationErrors = updateResult.getErrors(command);
        assertThat(validationErrors, hasSize(1));
        ValidationError error = validationErrors.iterator().next();
        assertThat(error.getErrorCode(), is(Errors.FIELD_IS_IMMUTABLE));
        assertThat(error.getField(), is(EntityForTest.IMMUTABLE_FIELD));
    }

    @Test
    public void creationDate() {
        int newId = 11;
        CreateTestCommand command = new CreateTestCommand();
        command.set(EntityForTest.ID, newId);
        command.set(EntityForTest.FIELD1, TestEnum.Alpha);
        command.set(EntityForTest.PARENT_ID, PARENT_ID_1);
        Instant expectedCreationDate = Instant.now();
        CreateResult<EntityForTest, EntityForTest.Key> results = persistenceLayer.create(ImmutableList.of(command), changeFlowConfig().build(), EntityForTest.Key.DEFINITION);
        assertThat(results.hasErrors(), is(false));
        Map<Identifier<EntityForTest>, CurrentEntityState> entityMap = entitiesFetcher.fetchEntitiesByIds(ImmutableList.of(new EntityForTest.Key(newId)),
                                                                                               EntityForTest.CREATION_DATE);
        assertThat(entityMap.size(), is(1));
        Instant actualCreationDate = entityMap.values().iterator().next().get(EntityForTest.CREATION_DATE);
        assertThat(Math.abs(actualCreationDate.toEpochMilli() - expectedCreationDate.toEpochMilli()), lessThan(2000L));
    }

    @Deprecated
    @Test
    public void creationDateWithDeprecatedFetcherAPI() {
        int newId = 11;
        CreateTestCommand command = new CreateTestCommand();
        command.set(EntityForTest.ID, newId);
        command.set(EntityForTest.FIELD1, TestEnum.Alpha);
        command.set(EntityForTest.PARENT_ID, PARENT_ID_1);
        Instant expectedCreationDate = Instant.now();
        CreateResult<EntityForTest, EntityForTest.Key> results = persistenceLayer.create(ImmutableList.of(command), changeFlowConfig().build(), EntityForTest.Key.DEFINITION);
        assertThat(results.hasErrors(), is(false));
        Map<Identifier<EntityForTest>, CurrentEntityState> entityMap = entitiesFetcher.fetchEntitiesByIds(singleton(new EntityForTest.Key(newId)),
                                                                                               singleton(EntityForTest.CREATION_DATE));
        assertThat(entityMap.size(), is(1));
        Instant actualCreationDate = entityMap.values().iterator().next().get(EntityForTest.CREATION_DATE);
        assertThat(Math.abs(actualCreationDate.toEpochMilli() - expectedCreationDate.toEpochMilli()), lessThan(2000L));
    }

    @Test
    public void createEntityWithoutParents() {
        CreateEntityCommand<EntityForTestParent> command = new CreateEntityCommand<>(EntityForTestParent.INSTANCE);
        int newId = 1001;
        command.set(EntityForTestParent.ID, newId);
        command.set(EntityForTestParent.FIELD1, "3000");
        ChangeFlowConfig<EntityForTestParent> flowConfig = ChangeFlowConfigBuilderFactory.newInstance(plContext, EntityForTestParent.INSTANCE).build();

        CreateResult<EntityForTestParent, EntityForTestParent.Key> results = persistenceLayerParent.create(ImmutableList.of(command), flowConfig, EntityForTestParent.Key.DEFINITION);

        assertThat(results.hasErrors(), is(false));
        Map<Integer, String> result = dslContext.select(parentTable.id, parentTable.field1)
                .from(parentTable)
                .where(parentTable.id.eq(newId))
                .fetchMap(parentTable.id, parentTable.field1);
        assertThat(result.keySet(), hasSize(1));
        assertThat(result.keySet(), contains(newId));
        assertThat(result.get(newId), is("3000"));
    }

    @Test
    public void createEntityIgnoringDuplicates() {
        CreateEntityCommand<EntityForTestIgnoringOnCreate> command = new CreateEntityCommand<>(EntityForTestIgnoringOnCreate.INSTANCE);
        command.set(EntityForTestIgnoringOnCreate.ID, ID_1);
        command.set(EntityForTestIgnoringOnCreate.PARENT_ID, PARENT_ID_1);
        command.set(EntityForTestIgnoringOnCreate.FIELD1, TestEnum.Charlie);

        ChangeFlowConfig<EntityForTestIgnoringOnCreate> flowConfig =
                ChangeFlowConfigBuilderFactory.newInstance(plContext, EntityForTestIgnoringOnCreate.INSTANCE).build();

        //noinspection unchecked
        CreateResult<EntityForTestIgnoringOnCreate, EntityForTestIgnoringOnCreate.Key> results =
                ((PersistenceLayer) persistenceLayer).create(ImmutableList.of(command), flowConfig, EntityForTestIgnoringOnCreate.Key.DEFINITION);

        assertThat(results.hasErrors(), is(false));
        Map<Integer, String> result = dslContext.select(mainTable.id, mainTable.field1)
                .from(mainTable)
                .where(mainTable.id.eq(ID_1))
                .fetchMap(mainTable.id, mainTable.field1);
        assertThat(result.keySet(), hasSize(1));
        assertThat(result.keySet(), contains(ID_1));
        assertThat(result.get(ID_1), is(TestEnum.Alpha.name()));
    }

    @Test
    public void createEntityUpdatingDuplicates() {
        final TestEnum newFieldValue = TestEnum.Charlie;

        CreateEntityCommand<EntityForTestUpdatingOnCreate> command = new CreateEntityCommand<>(EntityForTestUpdatingOnCreate.INSTANCE);
        command.set(EntityForTestUpdatingOnCreate.ID, ID_1);
        command.set(EntityForTestUpdatingOnCreate.PARENT_ID, PARENT_ID_1);
        command.set(EntityForTestUpdatingOnCreate.FIELD1, newFieldValue);

        ChangeFlowConfig<EntityForTestUpdatingOnCreate> flowConfig =
                ChangeFlowConfigBuilderFactory.newInstance(plContext, EntityForTestUpdatingOnCreate.INSTANCE).build();

        //noinspection unchecked
        CreateResult<EntityForTestUpdatingOnCreate, EntityForTestUpdatingOnCreate.Key> results =
                ((PersistenceLayer) persistenceLayer).create(ImmutableList.of(command), flowConfig, EntityForTestUpdatingOnCreate.Key.DEFINITION);

        assertThat(results.hasErrors(), is(false));
        Map<Integer, String> result = dslContext.select(mainTable.id, mainTable.field1)
                .from(mainTable)
                .where(mainTable.id.eq(ID_1))
                .fetchMap(mainTable.id, mainTable.field1);
        assertThat(result.keySet(), hasSize(1));
        assertThat(result.keySet(), contains(ID_1));
        assertThat(result.get(ID_1), is(newFieldValue.name()));
    }

    @Test
    public void createFlowWithParentValidators() {
        int newId1 = 11;
        CreateTestCommand command1 = new CreateTestCommand();
        command1.set(EntityForTest.ID, newId1);
        command1.set(EntityForTest.FIELD1, TestEnum.Alpha);
        command1.set(EntityForTest.PARENT_ID, PARENT_ID_1);
        int newId2 = 12;
        CreateTestCommand command2 = new CreateTestCommand();
        command2.set(EntityForTest.ID, newId2);
        command1.set(EntityForTest.FIELD1, TestEnum.Alpha);
        command2.set(EntityForTest.PARENT_ID, PARENT_ID_2);
        String entity2Url = "http://test";
        command2.set(URL, entity2Url);

        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig()
                .withValidator(new CannotCreateInParentValidator(FIELD2_INVALID_VALUE))
                .build();

        CreateResult<EntityForTest, EntityForTest.Key> createResult = persistenceLayer.create(ImmutableList.of(command1, command2), flowConfig, EntityForTest.Key.DEFINITION);

        Collection<ValidationError> validationErrors = createResult.getErrors(command1);
        assertThat(Collections2.transform(validationErrors, ValidationError::getErrorCode), Matchers.hasItem(CANNOT_CREATE_IN_PARENT));
        assertThat(createResult.getErrors(command2).size(), is(0));
        Map<Integer, Record3<Integer, Integer, String>> result = dslContext.select(mainTable.id, mainTable.field2, secondaryTable.url)
                .from(mainTable)
                .leftOuterJoin(secondaryTable).on(mainTable.id.eq(secondaryTable.entityId))
                .where(mainTable.id.in(newId1, newId2))
                .fetchMap(mainTable.id);

        assertThat(result.get(newId1), is(nullValue()));
        assertThat(result.get(newId2).value2(), is(999)); // default value
        assertThat(result.get(newId2).value3(), is(entity2Url));
    }

    @Test
    public void nonExistingIdWithSupplier() {
        final int NON_EXISTING_ID = 999;
        UpdateTestCommand command = new UpdateTestCommand(NON_EXISTING_ID);
        command.set(EntityForTest.FIELD2, new FieldValueSupplier<Integer>() {
            @Override
            public Integer supply(CurrentEntityState entity) {
                return entity.get(EntityForTest.FIELD2) + 20;
            }

            @Override
            public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
                return Stream.of(EntityForTest.FIELD2);
            }
        });

        UpdateResult<EntityForTest, EntityForTest.Key> updateResult = persistenceLayer.update(Collections.singletonList(command), changeFlowConfig().build());
        assertThat(updateResult.hasErrors(command), is(true));
        assertThat(updateResult.getErrors(command).iterator().next().getErrorCode(), is(Errors.ENTITY_NOT_FOUND));
    }

    @Test
    public void falseUpdatesFilter() {
        ArrayList<UpdateTestCommand> commands = new ArrayList<>();
        commands.add(createChangeCommand(ID_1, TestEnum.Alpha, 100)); // only one field really updated
        commands.add(createChangeCommand(ID_2, TestEnum.Bravo, 20)); // false update

        final Multimap<EntityChange<EntityForTest>, EntityField<EntityForTest, ?>> changedFields = HashMultimap.create();
        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig().withOutputGenerator(new OutputGenerator<EntityForTest>() {
            @Override
            public void generate(Collection<? extends EntityChange<EntityForTest>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
                entityChanges.forEach(entityChange -> entityChange.getChanges().forEach(fieldChange -> changedFields.put(entityChange, fieldChange.getField())));
            }

            @Override
            public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<EntityForTest, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
                return Stream.empty();
            }
        }).build();

        UpdateResult<EntityForTest, EntityForTest.Key> result = persistenceLayer.update(commands, flowConfig);
        assertThat(result.getStats().getAffectedRowsOf(mainTable.getName()).getUpdated(), is(1));

        assertThat(changedFields.asMap().size(), is(1));
        assertThat(changedFields.containsKey(commands.get(0)), is(true));
        Collection<EntityField<EntityForTest, ?>> changeFieldsFirstCommand = changedFields.get(commands.get(0));
        //noinspection unchecked
        assertThat(changeFieldsFirstCommand, contains(EntityForTest.FIELD2));
    }

    @Test
    public void validMultiFields() {
        ArrayList<UpdateTestCommand> commands = new ArrayList<>();
        commands.add(createChangeCommand(ID_1, FIELD1_VALID_VALUE, FIELD2_VALID_VALUE));

        EntityChangeCompositeValidator<EntityForTest> changesCompositeValidator = new EntityChangeCompositeValidator<>();
        changesCompositeValidator.register(new TestEntityChangeFieldValidator());
        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig().withValidator(changesCompositeValidator).build();

        persistenceLayer.update(commands, flowConfig);

        Map<Integer, Record3<Integer, String, Integer>> result = dslContext.select(mainTable.id, mainTable.field1, mainTable.field2)
                .from(mainTable)
                .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is(FIELD1_VALID_VALUE.name()));
        assertThat(result.get(ID_1).value3(), is(FIELD2_VALID_VALUE));

    }

    @Test
    public void validMultiField2InvalidValueForUpdateValidation() {
        ArrayList<UpdateTestCommand> commands = new ArrayList<>();
        commands.add(createChangeCommand(ID_1, TestEnum.Zeta, FIELD2_INVALID_VALUE));

        EntityChangeCompositeValidator<EntityForTest> changesCompositeValidator = new EntityChangeCompositeValidator<>();
        changesCompositeValidator.register(new TestEntityChangeFieldValidator());
        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig().withValidator(changesCompositeValidator).build();

        UpdateResult<EntityForTest, EntityForTest.Key> updateResult = persistenceLayer.update(commands, flowConfig);

        Collection<ValidationError> validationErrors = updateResult.getErrors(commands.get(0));
        assertThat(validationErrors.iterator().next().getErrorCode(), is(FIELD2_ERROR));
    }

    @Test
    public void requiredRelationsMissingInCreate() {
        CreateTestCommand command = new CreateTestCommand();
        command.set(EntityForTest.ID, ID_1);

        CreateResult<EntityForTest, EntityForTest.Key> createResult = persistenceLayer.create(ImmutableList.of(command),
                changeFlowConfig().build(), EntityForTest.Key.DEFINITION);

        Collection<ValidationError> validationErrors = createResult.getErrors(command);
        ValidationError validationError = validationErrors.iterator().next();
        assertThat(validationError.getErrorCode(), is(Errors.FIELD_IS_REQUIRED));
        assertThat(validationError.getParameters().size(), is(1));
        assertThat(validationError.getParameters().get("field"), is(EntityForTest.INSTANCE.toFieldName(EntityForTest.PARENT_ID)));
    }

    @Test
    public void validMultiField2InvalidValueOnlyForCreateValidation() {
        CreateTestCommand command = new CreateTestCommand();
        command.set(EntityForTest.ID, 11);
        command.set(EntityForTest.FIELD1, TestEnum.Zeta);
        command.set(EntityForTest.FIELD2, FIELD2_INVALID_VALUE);
        command.set(EntityForTest.PARENT_ID, 10);

        EntityChangeCompositeValidator<EntityForTest> changesCompositeValidator = new EntityChangeCompositeValidator<>();
        TestEntityChangeFieldValidator changeFieldValidator = new TestEntityChangeFieldValidator();
        changesCompositeValidator.register(changeFieldValidator);
        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig()
                .withValidator(changesCompositeValidator)
                .build();

        CreateResult<EntityForTest, EntityForTest.Key> createResult = persistenceLayer.create(ImmutableList.of(command), flowConfig, EntityForTest.Key.DEFINITION);

        Collection<ValidationError> validationErrors = createResult.getErrors(command);
        assertThat(validationErrors.iterator().next().getErrorCode(), is(FIELD2_ERROR));
    }

    @Test
    public void missingRequiredFieldValidation() {
        CreateTestCommand command = new CreateTestCommand();
        command.set(EntityForTest.FIELD2, 10);

        ChangeFlowConfig<EntityForTest> flowConfig = changeFlowConfig()
                .build();

        CreateResult<EntityForTest, EntityForTest.Key> createResult = persistenceLayer.create(ImmutableList.of(command), flowConfig, EntityForTest.Key.DEFINITION);

        Collection<ValidationError> validationErrors = createResult.getErrors(command);
        ValidationError validationError = validationErrors.iterator().next();
        assertThat(validationError.getErrorCode(), is(Errors.FIELD_IS_REQUIRED));
        assertThat(validationError.getParameters(), hasKey("field"));
        assertThat(validationError.getParameters().get("field"), is("PARENT_ID"));
    }

    static class CustomKey extends PairUniqueKeyValue<EntityForTest, TestEnum, Integer> {
        private static final PairUniqueKey<EntityForTest, TestEnum, Integer> DEFINITION = new PairUniqueKey<EntityForTest, TestEnum, Integer>(EntityForTest.FIELD1, EntityForTest.FIELD2) {
            @Override
            protected CustomKey createValue(TestEnum v1, Integer v2) {
                return new CustomKey(v1, v2);
            }
        };

        public CustomKey(TestEnum v1, Integer v2) {
            super(DEFINITION, v1, v2);
        }
    }

    @Test
    public void updateByKeys() {
        UpdateEntityCommand<EntityForTest, CustomKey> command = new UpdateEntityCommand<>(EntityForTest.INSTANCE, new CustomKey(TestEnum.Alpha, 10));
        command.set(EntityForTest.FIELD1, TestEnum.Zeta);

        persistenceLayer.update(Collections.singletonList(command), changeFlowConfig().build());

        Map<Integer, Record3<Integer, String, Integer>> result =
                dslContext.select(mainTable.id, EntityForTestTable.INSTANCE.field1, EntityForTestTable.INSTANCE.field2)
                        .from(mainTable)
                        .fetchMap(mainTable.id);
        assertThat(result.get(ID_1).value2(), is(TestEnum.Zeta.name()));
    }

    @Test
    public void delete() {
        DeleteEntityCommand<EntityForTest, EntityForTest.Key> command = new DeleteEntityCommand<>(EntityForTest.INSTANCE, new EntityForTest.Key(ID_1));

        DeleteResult<EntityForTest, EntityForTest.Key> changeResults = persistenceLayer.delete(ImmutableList.of(command), changeFlowConfig().build());
        assertFalse(changeResults.hasErrors());

        int count = dslContext.selectCount().from(mainTable).fetchOne().value1();
        assertThat(count, is(1));
    }

    @Test
    public void tempTableTest() {
        int newId1 = 11;
        UpdateTestCommand command = new UpdateTestCommand(newId1);
        command.set(EntityForTest.ID, newId1);
        command.set(EntityForTest.PARENT_ID, PARENT_ID_1);
        command.set(EntityForTest.FIELD2, FIELD2_VALID_VALUE);

        ImmutableList<EntityField<EntityForTest, Integer>> entityFields = ImmutableList.of(EntityForTest.ID, EntityForTest.PARENT_ID, EntityForTest.FIELD2);
        try (TempTableResource<ImpersonatorTable> tempTableResource = entitiesTempTableCreator.createTempTable(entityFields, Collections.<EntityChange<EntityForTest>>singletonList(command))) {
            ImpersonatorTable tempTable = tempTableResource.getTable();
            Result<? extends Record3<?, ?, ?>> records = dslContext.select(
                    tempTable.getField(EntityForTestTable.INSTANCE.id),
                    tempTable.getField(EntityForTestTable.INSTANCE.parent_id),
                    tempTable.getField(EntityForTestTable.INSTANCE.field2))
                    .from(tempTable)
                    .fetch();
            Record3<?, ?, ?> record = records.iterator().next();
            //noinspection unchecked
            assertThat(record.getValue(tempTable.getField(EntityForTestTable.INSTANCE.id)), is(newId1));
            //noinspection unchecked
            assertThat(record.getValue(tempTable.getField(EntityForTestTable.INSTANCE.parent_id)), is(PARENT_ID_1));
            //noinspection unchecked
            assertThat(record.getValue(tempTable.getField(EntityForTestTable.INSTANCE.field2)), is(FIELD2_VALID_VALUE));
        }
    }

    @Test
    public void freeFetchTest() {
        EntityForTestKeyValue k1 = new EntityForTestKeyValue(TestEnum.Alpha, 10);
        EntityForTestKeyValue k2 = new EntityForTestKeyValue(TestEnum.Charlie, 20);
        Map<EntityForTestKeyValue, PartialEntityForTest> map = entitiesFetcher.fetchPartialEntities(EntityForTest.INSTANCE,
                ImmutableList.of(k1, k2), PartialEntityForTest.class);
        assertThat(map.size(), is(1));
        assertThat(map, hasKey(k1));
        PartialEntityForTest entityForTest = map.get(k1);
        assertThat(entityForTest.getField2(), is(10));
        assertThat(entityForTest.getUrl(), is(GOOGLE_URL));
        assertThat(entityForTest.getComplexField(), is("key1:value1"));
    }

    @Test
    public void fetchByCondition() {
        List<PartialEntityForTest> entities = entitiesFetcher.fetchByCondition(EntityForTest.INSTANCE,
                EntityForTestTable.INSTANCE.field2.ge(15), PartialEntityForTest.class);
        assertThat(entities.size(), is(1));
        assertThat(entities.get(0).getField2(), is(20));
        assertThat(entities.get(0).getUrl(), is(DOODLE_URL));
    }

    @Test
    public void doNotUpdateIgnorableFields() {
        UpdateTestCommand command = new UpdateTestCommand(ID_1);

        command.set(EntityForTest.IGNORABLE_FIELD, 20);

        persistenceLayer.update(Collections.singletonList(command), changeFlowConfig().build());

        Map<Integer, Record2<Integer, Integer>> result = dslContext.select(mainTable.id, mainTable.ignorableField)
                .from(mainTable)
                .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is(IGNORABLE_1_ORIGINAL_VALUE));
    }

    @Test
    public void updateIgnorableFields() {
        UpdateTestCommand command = new UpdateTestCommand(ID_1);

        command.set(EntityForTest.FIELD2, 20);
        command.set(EntityForTest.IGNORABLE_FIELD, 20);

        persistenceLayer.update(Collections.singletonList(command), changeFlowConfig().build());

        Map<Integer, Record3<Integer, Integer, Integer>> result = dslContext.select(mainTable.id, mainTable.field2, mainTable.ignorableField)
                .from(mainTable)
                .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is(20));
        assertThat(result.get(ID_1).value3(), is(20));
    }

    @Test
    public void insertOnDuplicateUpdateCreateNotExistMainTable() {
        int newId = 51;
        InsertOnDuplicateUpdateTestCommand command1 = new InsertOnDuplicateUpdateTestCommand(newId);
        command1.set(EntityForTest.FIELD1, TestEnum.Charlie);
        command1.set(EntityForTest.PARENT_ID, PARENT_ID_1);
        InsertOnDuplicateUpdateTestCommand command2 = new InsertOnDuplicateUpdateTestCommand(newId + 1);
        command2.set(EntityForTest.FIELD1, TestEnum.Delta);
        command2.set(EntityForTest.PARENT_ID, PARENT_ID_2);

        InsertOnDuplicateUpdateResult<EntityForTest, EntityForTest.Key> insertOnDuplicateUpdateResult = persistenceLayer.upsert(ImmutableList.of(command1, command2), changeFlowConfig().build());

        assertThat(insertOnDuplicateUpdateResult.getStats().getAffectedRowsOf(mainTable.getName()).getInserted(), is(2));
        assertThat(insertOnDuplicateUpdateResult.getStats().getAffectedRowsOf(mainTable.getName()).getUpdated(), is(0));

        Map<Integer, Record3<Integer, String, Integer>> result =
                dslContext.select(mainTable.id, EntityForTestTable.INSTANCE.field1, EntityForTestTable.INSTANCE.parent_id)
                        .from(mainTable)
                        .fetchMap(mainTable.id);

        assertThat(result.get(newId).value2(), is(TestEnum.Charlie.name()));
        assertThat(result.get(newId).value3(), is(PARENT_ID_1));

        assertThat(result.get(newId + 1).value2(), is(TestEnum.Delta.name()));
        assertThat(result.get(newId + 1).value3(), is(PARENT_ID_2));

    }

    @Test
    public void insertOnDuplicateUpdateUpdateByKeys() {
        InsertOnDuplicateUpdateCommand<EntityForTest, CustomKey> command = new InsertOnDuplicateUpdateCommand<>(EntityForTest.INSTANCE, new CustomKey(TestEnum.Alpha, 10));
        command.set(EntityForTest.FIELD1, TestEnum.Zeta);

        InsertOnDuplicateUpdateResult<EntityForTest, CustomKey> insertOnDuplicateUpdateResult = persistenceLayer.upsert(Collections.singletonList(command), changeFlowConfig().build());

        assertThat(insertOnDuplicateUpdateResult.getStats().getAffectedRowsOf(mainTable.getName()).getInserted(), is(0));
        assertThat(insertOnDuplicateUpdateResult.getStats().getAffectedRowsOf(mainTable.getName()).getUpdated(), is(1));

        Map<Integer, Record3<Integer, String, Integer>> result =
                dslContext.select(mainTable.id, EntityForTestTable.INSTANCE.field1, EntityForTestTable.INSTANCE.field2)
                        .from(mainTable)
                        .fetchMap(mainTable.id);
        assertThat(result.get(ID_1).value2(), is(TestEnum.Zeta.name()));
    }

    @Test
    public void insertOnDuplicateUpdateInsertByKeys() {
        int initId = 50;
        InsertOnDuplicateUpdateCommand<EntityForTest, CustomKey> command = new InsertOnDuplicateUpdateCommand<>(EntityForTest.INSTANCE, new CustomKey(TestEnum.Alpha, 1000));
        command.set(EntityForTest.PARENT_ID, PARENT_ID_1);

        InsertOnDuplicateUpdateResult<EntityForTest, CustomKey> insertOnDuplicateUpdateResult = persistenceLayer.upsert(Collections.singletonList(command), changeFlowConfig().withPostFetchCommandEnricher(new TestIdGeneratorEnricher<>(EntityForTest.ID, initId)).build());

        assertThat(insertOnDuplicateUpdateResult.getStats().getAffectedRowsOf(mainTable.getName()).getInserted(), is(1));
        assertThat(insertOnDuplicateUpdateResult.getStats().getAffectedRowsOf(mainTable.getName()).getUpdated(), is(0));

        Map<Integer, Record3<Integer, String, Integer>> result =
                dslContext.select(mainTable.id, EntityForTestTable.INSTANCE.field1, EntityForTestTable.INSTANCE.field2)
                        .from(mainTable)
                        .fetchMap(mainTable.id);
        assertThat(result.get(initId + 1).value2(), is(TestEnum.Alpha.name()));
    }

    @Test
    public void insertOnDuplicateUpdateInsertAndUpdateByKeys() {
        int initId = 50;
        InsertOnDuplicateUpdateCommand<EntityForTest, CustomKey> command1 = new InsertOnDuplicateUpdateCommand<>(EntityForTest.INSTANCE, new CustomKey(TestEnum.Alpha, 10));
        command1.set(EntityForTest.FIELD1, TestEnum.Zeta);

        InsertOnDuplicateUpdateCommand<EntityForTest, CustomKey> command2 = new InsertOnDuplicateUpdateCommand<>(EntityForTest.INSTANCE, new CustomKey(TestEnum.Charlie, 1000));
        command2.set(EntityForTest.PARENT_ID, PARENT_ID_1);

        InsertOnDuplicateUpdateResult<EntityForTest, CustomKey> insertOnDuplicateUpdateResult = persistenceLayer.upsert(ImmutableList.of(command1, command2), changeFlowConfig().withPostFetchCommandEnricher(new TestIdGeneratorEnricher<>(EntityForTest.ID, initId)).build());

        assertThat(insertOnDuplicateUpdateResult.getStats().getAffectedRowsOf(mainTable.getName()).getInserted(), is(1));
        assertThat(insertOnDuplicateUpdateResult.getStats().getAffectedRowsOf(mainTable.getName()).getUpdated(), is(1));

        Map<Integer, Record3<Integer, String, Integer>> result =
                dslContext.select(mainTable.id, EntityForTestTable.INSTANCE.field1, EntityForTestTable.INSTANCE.field2)
                        .from(mainTable)
                        .fetchMap(mainTable.id);

        assertThat(result.get(ID_1).value2(), is(TestEnum.Zeta.name()));
        assertThat(result.get(initId + 1).value2(), is(TestEnum.Charlie.name()));
    }

    private static class TestEnricherWithParent implements PostFetchCommandEnricher<EntityForTest> {
        @Override
        public void enrich(Collection<? extends ChangeEntityCommand<EntityForTest>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
            for (ChangeEntityCommand<EntityForTest> command : changeEntityCommands) {
                command.set(EntityForTest.FIELD2, Integer.valueOf(changeContext.getEntity(command).get(EntityForTestParent.FIELD1)));
            }
        }

        @Override
        public Stream<EntityField<EntityForTest, ?>> fieldsToEnrich() {
            return Stream.of(EntityForTest.FIELD2);
        }

        @Override
        public boolean shouldRun(Collection<? extends EntityChange<EntityForTest>> changeEntityCommands) {
            return CommandsFieldMatcher.isAnyFieldMissingInAnyCommand(changeEntityCommands, EntityForTest.FIELD2);
        }

        @Override
        public SupportedChangeOperation getSupportedChangeOperation() {
            return SupportedChangeOperation.CREATE_AND_UPDATE;
        }

        @Override
        public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<EntityForTest, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
            return Stream.of(EntityForTestParent.FIELD1);
        }
    }

    private static class TestOutputGeneratorWithSecondary implements OutputGenerator<EntityForTest> {

        private final CommandsExecutor commandsExecutor;

        public TestOutputGeneratorWithSecondary(PLContext plContext) {
            this.commandsExecutor = CommandsExecutor.of(plContext.dslContext());
        }

        @Override
        public void generate(Collection<? extends EntityChange<EntityForTest>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
            ChangesContainer changesContainer = new ChangesContainer(EntityForTest.INSTANCE.onDuplicateKey());
            for (EntityChange<EntityForTest> entityChange : entityChanges) {
                String url = changeContext.getEntity(entityChange).get(URL);
                AbstractRecordCommand update = changesContainer.getInsertOnDuplicateUpdate(EntityForTestSecondaryTable.INSTANCE, entityChange,
                        () -> new CreateRecordCommand(EntityForTestSecondaryTable.INSTANCE));
                update.set(EntityForTestSecondaryTable.INSTANCE.id, changeContext.getEntity(entityChange).get(EntityForTest.ID));
                update.set(EntityForTestSecondaryTable.INSTANCE.entityId, changeContext.getEntity(entityChange).get(EntityForTest.ID));
                update.set(EntityForTestSecondaryTable.INSTANCE.url, url.replace("http", "https"));
            }
            changesContainer.commit(commandsExecutor, changeContext.getStats());
        }

        @Override
        public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<EntityForTest, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
            return Stream.of(EntityForTest.FIELD1, URL);
        }
    }

    private static class TestOutputGeneratorWithParent implements OutputGenerator<EntityForTest> {

        private final CommandsExecutor commandsExecutor;

        public TestOutputGeneratorWithParent(PLContext plContext) {
            this.commandsExecutor = CommandsExecutor.of(plContext.dslContext());

        }

        @Override
        public void generate(Collection<? extends EntityChange<EntityForTest>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
            ChangesContainer changesContainer = new ChangesContainer(EntityForTest.INSTANCE.onDuplicateKey());
            for (EntityChange<EntityForTest> entityChange : entityChanges) {
                String parentFieldValue = changeContext.getEntity(entityChange).get(EntityForTestParent.FIELD1);
                AbstractRecordCommand update = changesContainer.getUpdate(EntityForTestTable.INSTANCE, entityChange,
                        () -> new UpdateRecordCommand(EntityForTestTable.INSTANCE, EntityDbUtil.getDatabaseId(entityChange.getIdentifier())));
                update.set(EntityForTestTable.INSTANCE.field2, Integer.valueOf(parentFieldValue));
            }
            changesContainer.commit(commandsExecutor, changeContext.getStats());
        }

        @Override
        public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<EntityForTest, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
            return Stream.of(EntityForTestParent.FIELD1);
        }
    }

    private static class TestIdGeneratorEnricher<E extends EntityType<E>> implements PostFetchCommandEnricher<E> {

        private final AtomicInteger id;
        private final EntityField<E, Integer> idField;

        TestIdGeneratorEnricher(EntityField<E, Integer> idField, int initValue) {
            this.idField = idField;
            this.id = new AtomicInteger(initValue);
        }

        @Override
        public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
            changeEntityCommands.forEach(c -> c.set(idField, id.incrementAndGet()));
        }

        @Override
        public Stream<EntityField<E, ?>> fieldsToEnrich() {
            return Stream.of(idField);
        }

        @Override
        public SupportedChangeOperation getSupportedChangeOperation() {
            return SupportedChangeOperation.CREATE;
        }

        @Override
        public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
            return Stream.empty();
        }
    }

    private static class UpdateTestCommand extends UpdateEntityCommand<EntityForTest, EntityForTest.Key> {
        public UpdateTestCommand(int id) {
            super(EntityForTest.INSTANCE, new EntityForTest.Key(id));
        }

        public <T> UpdateTestCommand with(EntityField<EntityForTest, T> field, T value) {
            this.set(field, value);
            return this;
        }
    }

    private static class CreateTestCommand extends CreateEntityCommand<EntityForTest> {
        public CreateTestCommand() {
            super(EntityForTest.INSTANCE);
        }

        public <T> CreateTestCommand with(EntityField<EntityForTest, T> field, T value) {
            this.set(field, value);
            return this;
        }
    }

    private static class InsertOnDuplicateUpdateTestCommand extends InsertOnDuplicateUpdateCommand<EntityForTest, EntityForTest.Key> {
        public InsertOnDuplicateUpdateTestCommand(int id) {
            super(EntityForTest.INSTANCE, new EntityForTest.Key(id));
        }
    }

    private static class TestField2Validator implements FieldValidator<EntityForTest, Integer> {

        @Override
        public EntityField<EntityForTest, Integer> validatedField() {
            return EntityForTest.FIELD2;
        }

        @Override
        public ValidationError validate(Integer fieldValue) {

            if (fieldValue == FIELD2_INVALID_VALUE) {
                return new ValidationError(FIELD2_ERROR, EntityForTest.FIELD2);
            }
            return null;
        }
    }

    private static class CannotCreateInParentValidator implements ChangesValidator<EntityForTest> {
        private final int invalidParentValue;

        public CannotCreateInParentValidator(int invalidParentValue) {
            this.invalidParentValue = invalidParentValue;
        }

        @Override
        public void validate(Collection<? extends EntityChange<EntityForTest>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
            entityChanges.stream()
                    .filter(entityChange -> changeContext.getEntity(entityChange).get(EntityForTestParent.FIELD1).equals(Integer.toString(invalidParentValue)))
                    .forEach(entityChange -> changeContext.addValidationError(entityChange, new ValidationError(CANNOT_CREATE_IN_PARENT, EntityForTestParent.FIELD1)));
        }

        @Override
        public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<EntityForTest, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
            return Stream.of(EntityForTestParent.FIELD1);
        }
    }

    private static class TestEntityChangeFieldValidator implements FieldsCombinationValidator<EntityForTest> {

        @Override
        public Stream<EntityField<EntityForTest, ?>> validatedFields() {
            return Stream.of(EntityForTest.FIELD1, EntityForTest.FIELD2);
        }

        @Override
        public ValidationError validate(FieldsValueMap<EntityForTest> fieldsValueMap) {
            TestEnum value1 = fieldsValueMap.get(EntityForTest.FIELD1);
            Integer value2 = fieldsValueMap.get(EntityForTest.FIELD2);
            if (value1 == FIELD1_INVALID_VALUE) {
                return new ValidationError(FIELD1_ERROR, EntityForTest.FIELD1);
            }
            if (value2 == FIELD2_INVALID_VALUE) {
                return new ValidationError(FIELD2_ERROR, EntityForTest.FIELD2);
            }
            return null;
        }
    }


    private static class TestMultiFieldSupplier implements MultiFieldValueSupplier<EntityForTest> {

        private final Optional<ValidationError> validationError;

        public TestMultiFieldSupplier(ValidationError validationError) {
            this.validationError = Optional.ofNullable(validationError);
        }

        @Override
        public FieldsValueMap<EntityForTest> supply(CurrentEntityState entity) throws ValidationException {
            if (validationError.isPresent()) {
                throw new ValidationException(validationError.get());
            }

            FieldsValueMapImpl<EntityForTest> entityFieldsSupplier = new FieldsValueMapImpl<>();
            entityFieldsSupplier.set(EntityForTest.FIELD1, FIELD1_VALID_VALUE);
            entityFieldsSupplier.set(EntityForTest.FIELD2, FIELD2_VALID_VALUE);

            return entityFieldsSupplier;
        }

        @Override
        public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
            return Stream.empty();
        }
    }

    private static class NullingMultiFieldSupplier implements MultiFieldValueSupplier<EntityForTest> {

        private final List<EntityField<EntityForTest, ?>> fieldsToNull;

        public NullingMultiFieldSupplier(List<EntityField<EntityForTest, ?>> fieldsToNull) {
            this.fieldsToNull = fieldsToNull;
        }

        @Override
        public FieldsValueMap<EntityForTest> supply(CurrentEntityState entity) throws ValidationException {

            FieldsValueMapImpl<EntityForTest> entityFieldsSupplier = new FieldsValueMapImpl<>();
            for (EntityField<EntityForTest, ?> fieldToNull : fieldsToNull) {
                entityFieldsSupplier.set(fieldToNull, null);
            }

            return entityFieldsSupplier;
        }

        @Override
        public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
            return Stream.empty();
        }
    }

    private static class TestField1MultiFieldSupplier implements MultiFieldValueSupplier<EntityForTest> {

        @Override
        public FieldsValueMap<EntityForTest> supply(CurrentEntityState entity) throws ValidationException {
            FieldsValueMapImpl<EntityForTest> entityFieldsSupplier = new FieldsValueMapImpl<>();
            entityFieldsSupplier.set(EntityForTest.FIELD1, FIELD1_VALID_VALUE);
            return entityFieldsSupplier;
        }

        @Override
        public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
            return Stream.empty();
        }
    }

    private Integer fetchField(int id, TableField<Record, Integer> field) {
        return dslContext.select(field)
                .from(mainTable)
                .where(mainTable.id.eq(id))
                .fetchOne(mainTable.field2);
    }

    private PersistenceLayer<ChildForTest> childPL() {
        return new PersistenceLayer<>(dslContext);
    }

    private ChangeFlowConfig<ChildForTest> childFlow(Feature... features) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, ChildForTest.INSTANCE)
                .with(new FeatureSet(features))
                .build();
    }

}
