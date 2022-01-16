package com.kenshoo.pl.audit;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.CreateAuditedWithAncestorValueFormattersCommand;
import com.kenshoo.pl.audit.commands.UpdateAuditedWithEntityLevelValueFormatterCommand;
import com.kenshoo.pl.audit.commands.UpdateAuditedWithFieldLevelOnlyValueFormatterCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.AncestorTable;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorValueFormattersType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithEntityLevelValueFormatterType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithFieldLevelOnlyValueFormatterType;
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
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AuditWithCustomFieldValueFormatterTest {

    private static final Set<DataTable> ALL_TABLES = Set.of(AncestorTable.INSTANCE,
                                                            MainWithAncestorTable.INSTANCE,
                                                            MainTable.INSTANCE);

    private static final long MAIN_ID = 11;
    private static final long ANCESTOR_ID = 1;

    private static final String OLD_NAME = "oldName";
    private static final String NEW_NAME = "newName";

    private static final String OLD_DESC = "oldDesc";
    private static final String NEW_DESC = "newDesc";

    private static final String OLD_DESC2 = "oldDesc2";
    private static final String NEW_DESC2 = "newDesc2";

    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";

    private static final String CUSTOM1_SUFFIX = " custom1";
    private static final String CUSTOM2_SUFFIX = " custom2";


    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedWithEntityLevelValueFormatterType> entityLevelFormatterFlowConfig;
    private ChangeFlowConfig<AuditedWithFieldLevelOnlyValueFormatterType> fieldLevelOnlyFormatterFlowConfig;
    private ChangeFlowConfig<AuditedWithAncestorValueFormattersType> ancestorFormattersFlowConfig;

    private PersistenceLayer<AuditedWithEntityLevelValueFormatterType> entityLevelFormatterPL;
    private PersistenceLayer<AuditedWithFieldLevelOnlyValueFormatterType> fieldLevelOnlyFormatterPL;
    private PersistenceLayer<AuditedWithAncestorValueFormattersType> ancestorFormattersPL;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        entityLevelFormatterFlowConfig = flowConfigBuilder(AuditedWithEntityLevelValueFormatterType.INSTANCE).build();
        fieldLevelOnlyFormatterFlowConfig = flowConfigBuilder(AuditedWithFieldLevelOnlyValueFormatterType.INSTANCE).build();
        ancestorFormattersFlowConfig = flowConfigBuilder(AuditedWithAncestorValueFormattersType.INSTANCE).build();

        entityLevelFormatterPL = persistenceLayer();
        fieldLevelOnlyFormatterPL = persistenceLayer();
        ancestorFormattersPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(MainTable.INSTANCE)
                  .set(MainTable.INSTANCE.id, MAIN_ID)
                  .set(MainTable.INSTANCE.name, OLD_NAME)
                  .set(MainTable.INSTANCE.desc, OLD_DESC)
                  .set(MainTable.INSTANCE.desc2, OLD_DESC2)
                  .execute();

        dslContext.insertInto(AncestorTable.INSTANCE)
                  .set(AncestorTable.INSTANCE.id, ANCESTOR_ID)
                  .set(AncestorTable.INSTANCE.name, ANCESTOR_NAME)
                  .set(AncestorTable.INSTANCE.desc, ANCESTOR_DESC)
                  .execute();
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void entityLevelFormatterAndNoAnnotationsOnField() {
        entityLevelFormatterPL.update(singletonList(entityLevelFormatterUpdateCmd()
                                        .with(AuditedWithEntityLevelValueFormatterType.NAME, NEW_NAME)),
                                      entityLevelFormatterFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        assertThat(auditRecords.get(0), hasChangedFieldRecord(AuditedWithEntityLevelValueFormatterType.NAME,
                                                              OLD_NAME + CUSTOM1_SUFFIX,
                                                              NEW_NAME + CUSTOM1_SUFFIX));
    }

    @Test
    public void entityLevelFormatterAndPlainAuditedAnnotationOnField() {
        entityLevelFormatterPL.update(singletonList(entityLevelFormatterUpdateCmd()
                                                        .with(AuditedWithEntityLevelValueFormatterType.DESC2, NEW_DESC2)),
                                      entityLevelFormatterFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        assertThat(auditRecords.get(0), hasChangedFieldRecord(AuditedWithEntityLevelValueFormatterType.DESC2,
                                                              OLD_DESC2 + CUSTOM1_SUFFIX,
                                                              NEW_DESC2 + CUSTOM1_SUFFIX));
    }

    @Test
    public void entityLevelFormatterAndFieldLevelFormatter() {
        entityLevelFormatterPL.update(singletonList(entityLevelFormatterUpdateCmd()
                                                        .with(AuditedWithEntityLevelValueFormatterType.DESC, NEW_DESC)),
                                      entityLevelFormatterFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        assertThat(auditRecords.get(0), hasChangedFieldRecord(AuditedWithEntityLevelValueFormatterType.DESC,
                                                              OLD_DESC + CUSTOM2_SUFFIX,
                                                              NEW_DESC + CUSTOM2_SUFFIX));
    }

    @Test
    public void fieldLevelOnlyFormatter() {
        fieldLevelOnlyFormatterPL.update(singletonList(fieldLevelOnlyFormatterUpdateCmd()
                                                        .with(AuditedWithFieldLevelOnlyValueFormatterType.NAME, NEW_NAME)),
                                      fieldLevelOnlyFormatterFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        assertThat(auditRecords.get(0), hasChangedFieldRecord(AuditedWithFieldLevelOnlyValueFormatterType.NAME,
                                                              OLD_NAME + CUSTOM1_SUFFIX,
                                                              NEW_NAME + CUSTOM1_SUFFIX));
    }

    @Test
    public void ancestorFormatters() {
        ancestorFormattersPL.create(singletonList(ancestorFormattersCreateCmd()
                                                      .with(AuditedWithAncestorValueFormattersType.ANCESTOR_ID, ANCESTOR_ID)
                                                      .with(AuditedWithAncestorValueFormattersType.NAME, "blabla")),
                                         ancestorFormattersFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        assertThat(auditRecords.get(0), allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME + CUSTOM1_SUFFIX),
                                              hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC + CUSTOM2_SUFFIX)));
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }

    private <E extends EntityType<E>> ChangeFlowConfig.Builder<E> flowConfigBuilder(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType);
    }

    private UpdateAuditedWithEntityLevelValueFormatterCommand entityLevelFormatterUpdateCmd() {
        return new UpdateAuditedWithEntityLevelValueFormatterCommand(MAIN_ID);
    }

    private UpdateAuditedWithFieldLevelOnlyValueFormatterCommand fieldLevelOnlyFormatterUpdateCmd() {
        return new UpdateAuditedWithFieldLevelOnlyValueFormatterCommand(MAIN_ID);
    }

    private CreateAuditedWithAncestorValueFormattersCommand ancestorFormattersCreateCmd() {
        return new CreateAuditedWithAncestorValueFormattersCommand();
    }
}
