package com.kenshoo.pl.secondary;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.one2many.relatedByNonPK.Type;
import org.hamcrest.Matchers;
import org.jooq.DSLContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static com.kenshoo.pl.secondary.MainEntity.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SecondaryTablesAllCasesTest {

    private static DSLContext dslContext = TestJooqConfig.create();
    private PLContext plContext = new PLContext.Builder(dslContext).build();
    private PersistenceLayer<MainEntity> persistenceLayer = new PersistenceLayer<>(dslContext);

    private MainTable mainTable = MainTable.INSTANCE;
    private Secondary1ById secondary1ById = Secondary1ById.INSTANCE;
    private Secondary2ByIdInTarget secondary2ByIdInTarget = Secondary2ByIdInTarget.INSTANCE;
    private Secondary3ByNameAndType secondary3ByNameAndType = Secondary3ByNameAndType.INSTANCE;

    //main params
    private static final int ID_1 = 1;
    private static final int ID_2 = 2;
    private static final int ID_3 = 3;
    private static final int ID_IN_TARGET_1 = 11;
    private static final int ID_IN_TARGET_2 = 22;
    private static final int ID_IN_TARGET_3 = 33;
    private static final String NAME_1 = "main1";
    private static final String NAME_2 = "main2";
    private static final Type TYPE_1 = Type.T1;
    private static final Type TYPE_2 = Type.T2;
    private static final Type TYPE_3 = Type.T3;

    //secondary1ById params
    private static final String GOOGLE_URL = "http://google.com";
    private static final String DOODLE_URL = "http://doodle.com";
    private static final String PARAM_1 = "PARAM_1";
    private static final String PARAM_2 = "PARAM_2";

    //secondary2ByIdInTarget params
    private static final Double BUDGET_1 = 11.11;
    private static final Double BUDGET_2 = 22.22;

    //secondary3ByNameAndType params
    private static final String LOCATION_1 = "USD";
    private static final String LOCATION_2 = "ISRAEL";

    private static final Object[][] MAIN_DATA = {
            {ID_1, ID_IN_TARGET_1, NAME_1, TYPE_1.name()},
            {ID_2, ID_IN_TARGET_2, NAME_2, TYPE_2.name()},
    };

    private static final Object[][] SECONDARY1_BY_ID_DATE = {{ID_1, BUDGET_1}};
    private static final Object[][] SECONDARY2_BY_ID_IN_TARGET_DATA = {{ID_IN_TARGET_1, GOOGLE_URL, ""}};
    private static final Object[][] SECONDARY3_BY_NAME_AND_TYPE_DATE = {{NAME_1, TYPE_1.name(), LOCATION_1}};


    @Before
    public void populateTables() {
        ImmutableList.of(mainTable, secondary1ById, secondary2ByIdInTarget, secondary3ByNameAndType).forEach(table -> DataTableUtils.createTable(dslContext, table));
        DataTableUtils.populateTable(dslContext, mainTable, MAIN_DATA);
        DataTableUtils.populateTable(dslContext, secondary1ById, SECONDARY1_BY_ID_DATE);
        DataTableUtils.populateTableWithoutAutoIncFields(dslContext, secondary2ByIdInTarget, SECONDARY2_BY_ID_IN_TARGET_DATA);
        DataTableUtils.populateTableWithoutAutoIncFields(dslContext, secondary3ByNameAndType, SECONDARY3_BY_NAME_AND_TYPE_DATE);
    }

    @AfterClass
    public static void dropTables() {
        dslContext.dropTableIfExists(MainTable.INSTANCE).execute();
        dslContext.dropTableIfExists(Secondary1ById.INSTANCE).execute();
        dslContext.dropTableIfExists(Secondary2ByIdInTarget.INSTANCE).execute();
        dslContext.dropTableIfExists(Secondary3ByNameAndType.INSTANCE).execute();
    }

    @Test
    public void when_secondary_table_connected_by_primary_key_then_update_it_as_expected() {
        var commands = ImmutableList.of(
                new UpdateMainCommand(ID_1).with(BUDGET, BUDGET_2)
        );

        var updateResult = persistenceLayer.update(commands, changeFlowConfig().build());
        var tableStats = updateResult.getStats().getAffectedRowsOf(secondary1ById.getName());
        assertThat(tableStats.getUpdated(), is(1));
        assertThat(tableStats.getInserted(), is(0));

        var results = dslContext.selectFrom(secondary1ById).fetch();

        assertThat(results, Matchers.hasSize(1));

        assertThat(results.get(0).get(0), is(ID_1));
        assertThat(results.get(0).get(1), is(BUDGET_2));
    }

    @Test
    public void when_secondary_table_connected_by_primary_key_then_create_it_as_expected() {
        var commands = ImmutableList.of(
                new UpdateMainCommand(ID_2).with(BUDGET, BUDGET_2)
        );

        var updateResult = persistenceLayer.update(commands, changeFlowConfig().build());

        var tableStats = updateResult.getStats().getAffectedRowsOf(secondary1ById.getName());
        assertThat(tableStats.getUpdated(), is(0));
        assertThat(tableStats.getInserted(), is(1));

        var results = dslContext.selectFrom(secondary1ById).where(secondary1ById.id1.eq(ID_2)).fetch();

        assertThat(results.get(0).get(0), is(ID_2));
        assertThat(results.get(0).get(1), is(BUDGET_2));
    }

    @Test
    public void when_secondary_table_connected_by_both_keys_then_update_it_as_expected() {
        var commands = ImmutableList.of(
                new UpdateMainCommand(ID_1).with(LOCATION, LOCATION_2)
        );

        var updateResult = persistenceLayer.update(commands, changeFlowConfig().build());
        var tableStats = updateResult.getStats().getAffectedRowsOf(secondary3ByNameAndType.getName());
        assertThat(tableStats.getUpdated(), is(1));
        assertThat(tableStats.getInserted(), is(0));

        var results = dslContext.selectFrom(secondary3ByNameAndType).fetch();

        assertThat(results, Matchers.hasSize(1));

        assertThat(results.get(0).get(1), is(NAME_1));
        assertThat(results.get(0).get(2), is(TYPE_1.name()));
        assertThat(results.get(0).get(3), is(LOCATION_2));
    }

    @Test
    public void when_secondary_table_connected_by_both_keys_then_create_it_as_expected() {
        var commands = ImmutableList.of(
                new UpdateMainCommand(ID_2).with(LOCATION, LOCATION_2)
        );

        var updateResult = persistenceLayer.update(commands, changeFlowConfig().build());

        var tableStats = updateResult.getStats().getAffectedRowsOf(secondary3ByNameAndType.getName());
        assertThat(tableStats.getUpdated(), is(0));
        assertThat(tableStats.getInserted(), is(1));

        var results = dslContext.selectFrom(secondary3ByNameAndType).where(secondary3ByNameAndType.name3.eq(NAME_2).and(secondary3ByNameAndType.type3.eq(TYPE_2.name()))).fetch();

        assertThat(results.get(0).get(1), is(NAME_2));
        assertThat(results.get(0).get(2), is(TYPE_2.name()));
        assertThat(results.get(0).get(3), is(LOCATION_2));
    }

    @Test
    public void when_nonNullable_field_configured_on_secondary_table_then_update_other_fields() {
        var commands = ImmutableList.of(
                new UpdateMainCommand(ID_1).with(ID_IN_TARGET, ID_IN_TARGET_1).with(URL_PARAM, PARAM_1)
        );

        var updateResult = persistenceLayer.update(commands, changeFlowConfig().build());
        var tableStats = updateResult.getStats().getAffectedRowsOf(secondary2ByIdInTarget.getName());
        assertThat(tableStats.getUpdated(), is(1));
        assertThat(tableStats.getInserted(), is(0));

        var results = dslContext.selectFrom(secondary2ByIdInTarget).fetch();

        assertThat(results, Matchers.hasSize(1));

        assertThat(results.get(0).get(1), is(ID_IN_TARGET_1));
        assertThat(results.get(0).get(2), is(GOOGLE_URL));
        assertThat(results.get(0).get(3), is(PARAM_1));
    }

    @Test
    public void when_not_exists_secondary_record_then_create_it_as_expected() {
        var commands = ImmutableList.of(
                new UpdateMainCommand(ID_2).with(ID_IN_TARGET, ID_IN_TARGET_1).with(URL_PARAM, PARAM_2).with(URL, GOOGLE_URL)
        );

        var updateResult = persistenceLayer.update(commands, changeFlowConfig().build());

        var tableStats = updateResult.getStats().getAffectedRowsOf(secondary2ByIdInTarget.getName());
        assertThat(tableStats.getUpdated(), is(0));
        assertThat(tableStats.getInserted(), is(1));

        var results = dslContext.selectFrom(secondary2ByIdInTarget).where(secondary2ByIdInTarget.id_in_target2.eq(ID_IN_TARGET_2)).fetch();

        assertThat(results, Matchers.hasSize(1));

        assertThat(results.get(0).get(1), is(ID_IN_TARGET_2));
        assertThat(results.get(0).get(2), is(GOOGLE_URL));
        assertThat(results.get(0).get(3), is(PARAM_2));
    }

    @Test
    public void when_not_exists_main_record_then_create_main_and_secondary_records_as_expected() {
        var commands = ImmutableList.of(
                new CreateMainCommand()
                        .with(ID, ID_3)
                        .with(ID_IN_TARGET, ID_IN_TARGET_3)
                        .with(NAME, NAME_2)
                        .with(TYPE, TYPE_3)
                        .with(LOCATION, LOCATION_2)
        );

        var createdResults = persistenceLayer.create(commands, changeFlowConfig().build());

        var mainTableStats = createdResults.getStats().getAffectedRowsOf(mainTable.getName());
        assertThat(mainTableStats.getUpdated(), is(0));
        assertThat(mainTableStats.getInserted(), is(1));

        var secondaryStats = createdResults.getStats().getAffectedRowsOf(secondary3ByNameAndType.getName());
        assertThat(secondaryStats.getUpdated(), is(0));
        assertThat(secondaryStats.getInserted(), is(1));

        var results = dslContext.selectFrom(secondary3ByNameAndType).where(secondary3ByNameAndType.name3.eq(NAME_2).and(secondary3ByNameAndType.type3.eq(TYPE_3.name()))).fetch();

        assertThat(results.get(0).get(1), is(NAME_2));
        assertThat(results.get(0).get(2), is(TYPE_3.name()));
        assertThat(results.get(0).get(3), is(LOCATION_2));
    }

    @Test
    public void update_multi_secondary_tables_in_single_command() {
        var commands = ImmutableList.of(
                new UpdateMainCommand(ID_1).with(BUDGET, BUDGET_2).with(URL_PARAM, PARAM_2).with(LOCATION, LOCATION_2)
        );

        persistenceLayer.update(commands, changeFlowConfig().build());

        var secondaryTable2Results = dslContext.selectFrom(secondary2ByIdInTarget).fetch();

        assertThat(secondaryTable2Results.get(0).get(1), is(ID_IN_TARGET_1));
        assertThat(secondaryTable2Results.get(0).get(2), is(GOOGLE_URL));
        assertThat(secondaryTable2Results.get(0).get(3), is(PARAM_2));

        var secondaryTable1Results = dslContext.selectFrom(secondary1ById).fetch();

        assertThat(secondaryTable1Results.get(0).get(0), is(ID_1));
        assertThat(secondaryTable1Results.get(0).get(1), is(BUDGET_2));

        var secondaryTable3Results = dslContext.selectFrom(secondary3ByNameAndType).fetch();

        assertThat(secondaryTable3Results.get(0).get(1), is(NAME_1));
        assertThat(secondaryTable3Results.get(0).get(2), is(TYPE_1.name()));
        assertThat(secondaryTable3Results.get(0).get(3), is(LOCATION_2));
    }

    private static class UpdateMainCommand extends UpdateEntityCommand<MainEntity, MainEntity.Key> {
        public UpdateMainCommand(int id) {
            super(MainEntity.INSTANCE, new MainEntity.Key(id));
        }

        public <T> UpdateMainCommand with(EntityField<MainEntity, T> field, T value) {
            this.set(field, value);
            return this;
        }
    }

    private static class CreateMainCommand extends CreateEntityCommand<MainEntity> {

        public CreateMainCommand() {
            super(MainEntity.INSTANCE);
        }

        public <T> CreateMainCommand with(EntityField<MainEntity, T> field, T value) {
            this.set(field, value);
            return this;
        }
    }

    private ChangeFlowConfig.Builder<MainEntity> changeFlowConfig() {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, MainEntity.INSTANCE);
    }
}
