package com.kenshoo.pl.audit;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.CreateAuditedWithAncestorMandatoryCommand;
import com.kenshoo.pl.audit.commands.UpdateAuditedCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.AncestorTable;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorMandatoryType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.hasChangedFieldRecord;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.hasMandatoryFieldValue;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AuditFieldValuesFormattingTest {

    private static final Set<DataTable> ALL_TABLES = Set.of(AncestorTable.INSTANCE,
                                                            MainWithAncestorTable.INSTANCE,
                                                            MainTable.INSTANCE);

    private static final long MAIN_ID = 11;
    private static final long ANCESTOR_ID = 1;

    private static final double OLD_AMOUNT_VALUE = 11.119999;
    private static final double NEW_AMOUNT_VALUE = 12.129999;

    private static final String DEFAULT_FORMAT_OLD_AMOUNT_VALUE = "11.119999";
    private static final String DEFAULT_FORMAT_NEW_AMOUNT_VALUE = "12.129999";

    private static final String SPECIFIC_FORMAT_OLD_AMOUNT_VALUE = "11.12";
    private static final String SPECIFIC_FORMAT_NEW_AMOUNT_VALUE = "12.13";

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedType> mainFlowConfig;
    private ChangeFlowConfig<AuditedWithAncestorMandatoryType> mainWithAncestorFlowConfig;

    private PersistenceLayer<AuditedType> mainPL;
    private PersistenceLayer<AuditedWithAncestorMandatoryType> mainWithAncestorPL;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        mainFlowConfig = flowConfigBuilder(AuditedType.INSTANCE).build();
        mainWithAncestorFlowConfig = flowConfigBuilder(AuditedWithAncestorMandatoryType.INSTANCE).build();

        mainPL = persistenceLayer();
        mainWithAncestorPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(MainTable.INSTANCE)
                  .set(MainTable.INSTANCE.id, MAIN_ID)
                  .set(MainTable.INSTANCE.amount, OLD_AMOUNT_VALUE)
                  .set(MainTable.INSTANCE.amount2, OLD_AMOUNT_VALUE)
                  .execute();

        dslContext.insertInto(AncestorTable.INSTANCE)
                  .set(AncestorTable.INSTANCE.id, ANCESTOR_ID)
                  .set(AncestorTable.INSTANCE.amount, NEW_AMOUNT_VALUE)
                  .set(AncestorTable.INSTANCE.amount2, NEW_AMOUNT_VALUE)
                  .execute();
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void numericFieldInMainWithDefaultFormatter() {
        mainPL.update(singletonList(mainUpdateCmd()
                                        .with(AuditedType.AMOUNT, NEW_AMOUNT_VALUE)),
                      mainFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        assertThat(auditRecords.get(0), hasChangedFieldRecord(AuditedType.AMOUNT,
                                                              DEFAULT_FORMAT_OLD_AMOUNT_VALUE,
                                                              DEFAULT_FORMAT_NEW_AMOUNT_VALUE));
    }

    @Test
    public void numericFieldInMainWithSpecificFormatter() {
        mainPL.update(singletonList(mainUpdateCmd()
                                        .with(AuditedType.AMOUNT2, NEW_AMOUNT_VALUE)),
                      mainFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        assertThat(auditRecords.get(0), hasChangedFieldRecord(AuditedType.AMOUNT2,
                                                              SPECIFIC_FORMAT_OLD_AMOUNT_VALUE,
                                                              SPECIFIC_FORMAT_NEW_AMOUNT_VALUE));
    }

    @Test
    public void numericFieldInAncestorWithDefaultFormatter() {
        mainWithAncestorPL.create(singletonList(mainWithAncestorCreateCmd()
                                                    .with(AuditedWithAncestorMandatoryType.ANCESTOR_ID, ANCESTOR_ID)
                                                    .with(AuditedWithAncestorMandatoryType.NAME, "someName")),
                                  mainWithAncestorFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        assertThat(auditRecords.get(0), hasMandatoryFieldValue(NotAuditedAncestorType.AMOUNT, DEFAULT_FORMAT_NEW_AMOUNT_VALUE));
    }

    @Test
    public void numericFieldInAncestorWithSpecificFormatter() {
        mainWithAncestorPL.create(singletonList(mainWithAncestorCreateCmd()
                                                    .with(AuditedWithAncestorMandatoryType.ANCESTOR_ID, ANCESTOR_ID)
                                                    .with(AuditedWithAncestorMandatoryType.NAME, "someName")),
                                  mainWithAncestorFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        assertThat(auditRecords.get(0), hasMandatoryFieldValue(NotAuditedAncestorType.AMOUNT2, SPECIFIC_FORMAT_NEW_AMOUNT_VALUE));
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }

    private <E extends EntityType<E>> ChangeFlowConfig.Builder<E> flowConfigBuilder(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType);
    }

    private UpdateAuditedCommand mainUpdateCmd() {
        return new UpdateAuditedCommand(MAIN_ID);
    }

    private CreateAuditedWithAncestorMandatoryCommand mainWithAncestorCreateCmd() {
        return new CreateAuditedWithAncestorMandatoryCommand();
    }
}
