package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.kenshoo.pl.entity.TestEntityWithTransient.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PersistenceLayerWithTransientTest {

    private static boolean tablesCreated = false;
    private static DSLContext staticDSLContext;

    private static final int ID_1 = 1;
    private static final int ID_2 = 2;
    private static final String FIELD_1_VALUE_1 = "field1Val1";
    private static final String FIELD_1_VALUE_2 = "field1Val2";
    private static final String TRANSIENT_1_VALUE_1 = "transient1Val1";
    private static final String TRANSIENT_1_VALUE_2 = "transient1Val2";
    private static final String TRANSIENT_2_VALUE_1 = "transient2Val1";
    private static final String TRANSIENT_2_VALUE_2 = "transient2Val2";

    private DSLContext dslContext;
    private TestEntityTable mainTable;
    private ChangeFlowConfig<TestEntityWithTransient> flowConfig;
    private PersistenceLayer<TestEntityWithTransient> persistenceLayer;


    @Before
    public void setUp() {

        dslContext = TestJooqConfig.create();
        staticDSLContext = dslContext;
        final var plContext = new PLContext.Builder(dslContext).build();
        persistenceLayer = new PersistenceLayer<>(dslContext);
        flowConfig = ChangeFlowConfigBuilderFactory.newInstance(plContext, TestEntityWithTransient.INSTANCE).build();

        mainTable = TestEntityTable.TABLE;
        if (!tablesCreated) {
            DataTableUtils.createTable(dslContext, mainTable);
        }

        tablesCreated = true;
    }

    @After
    public void clearTables() {
        dslContext.deleteFrom(mainTable).execute();
    }

    @AfterClass
    public static void dropTables() {
        staticDSLContext.dropTableIfExists(TestEntityTable.TABLE).execute();
    }

    @Test
    public void transientValuesArePassedThroughTheFlow() {

        final var cmd1 = new CreateEntityCommand<>(TestEntityWithTransient.INSTANCE);
        cmd1.set(ID, ID_1);
        cmd1.set(FIELD_1, FIELD_1_VALUE_1);
        cmd1.set(TRANSIENT_1, TRANSIENT_1_VALUE_1);
        cmd1.set(TRANSIENT_2, TRANSIENT_2_VALUE_1);

        final var cmd2 = new CreateEntityCommand<>(TestEntityWithTransient.INSTANCE);
        cmd2.set(ID, ID_2);
        cmd2.set(FIELD_1, FIELD_1_VALUE_2);
        cmd2.set(TRANSIENT_1, TRANSIENT_1_VALUE_2);
        cmd2.set(TRANSIENT_2, TRANSIENT_2_VALUE_2);

        persistenceLayer.create(List.of(cmd1, cmd2), flowConfig);

        assertThat("Incorrect value of transient1 in cmd1:",
                cmd1.get(TRANSIENT_1), is(Optional.of(TRANSIENT_1_VALUE_1)));
        assertThat("Incorrect value of transient2 in cmd1:",
                cmd1.get(TRANSIENT_2), is(Optional.of(TRANSIENT_2_VALUE_1)));
        assertThat("Incorrect value of transient1 in cmd2:",
                cmd2.get(TRANSIENT_1), is(Optional.of(TRANSIENT_1_VALUE_2)));
        assertThat("Incorrect value of transient2 in cmd2:",
                cmd2.get(TRANSIENT_2), is(Optional.of(TRANSIENT_2_VALUE_2)));
    }
}
