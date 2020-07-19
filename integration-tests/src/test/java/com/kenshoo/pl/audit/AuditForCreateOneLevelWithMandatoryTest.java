package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.CreateAuditedWithAncestorMandatoryCommand;
import com.kenshoo.pl.audit.commands.CreateAuditedWithSelfAndAncestorMandatoryCommand;
import com.kenshoo.pl.audit.commands.CreateAuditedWithSelfMandatoryCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.AncestorTable;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorMandatoryType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithSelfAndAncestorMandatoryType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithSelfMandatoryType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AuditForCreateOneLevelWithMandatoryTest {

    private static final String NAME = "name";
    private static final String DESC = "desc";
    private static final long ANCESTOR_ID = 11L;
    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";

    private static final EntityField<AuditedWithAncestorMandatoryType, Long> AUD_WITH_ANC_FK_FIELD = AuditedWithAncestorMandatoryType.ANCESTOR_ID;
    private static final EntityField<AuditedWithSelfAndAncestorMandatoryType, Long> AUD_WITH_BOTH_FK_FIELD = AuditedWithSelfAndAncestorMandatoryType.ANCESTOR_ID;

    private static final EntityField<AuditedWithAncestorMandatoryType, String> AUD_WITH_ANC_NAME_FIELD = AuditedWithAncestorMandatoryType.NAME;
    private static final EntityField<AuditedWithAncestorMandatoryType, String> AUD_WITH_ANC_DESC_FIELD = AuditedWithAncestorMandatoryType.DESC;

    private static final EntityField<AuditedWithSelfMandatoryType, String> AUD_WITH_SELF_NAME_FIELD = AuditedWithSelfMandatoryType.NAME;
    private static final EntityField<AuditedWithSelfMandatoryType, String> AUD_WITH_SELF_DESC_FIELD = AuditedWithSelfMandatoryType.DESC;

    private static final EntityField<AuditedWithSelfAndAncestorMandatoryType, String> AUD_WITH_BOTH_NAME_FIELD = AuditedWithSelfAndAncestorMandatoryType.NAME;
    private static final EntityField<AuditedWithSelfAndAncestorMandatoryType, String> AUD_WITH_BOTH_DESC_FIELD = AuditedWithSelfAndAncestorMandatoryType.DESC;

    private static final EntityField<NotAuditedAncestorType, String> ANCESTOR_NAME_FIELD = NotAuditedAncestorType.NAME;
    private static final EntityField<NotAuditedAncestorType, String> ANCESTOR_DESC_FIELD = NotAuditedAncestorType.DESC;

    private static final List<DataTable> ALL_TABLES = ImmutableList.of(MainTable.INSTANCE,
                                                                       MainWithAncestorTable.INSTANCE,
                                                                       AncestorTable.INSTANCE);

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedWithAncestorMandatoryType> auditedWithAncestorConfig;
    private ChangeFlowConfig<AuditedWithSelfMandatoryType> auditedWithSelfConfig;
    private ChangeFlowConfig<AuditedWithSelfAndAncestorMandatoryType> auditedWithBothConfig;

    private PersistenceLayer<AuditedWithAncestorMandatoryType> auditedWithAncestorPL;
    private PersistenceLayer<AuditedWithSelfMandatoryType> auditedWithSelfPL;
    private PersistenceLayer<AuditedWithSelfAndAncestorMandatoryType> auditedWithBothPL;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedWithAncestorConfig = flowConfig(AuditedWithAncestorMandatoryType.INSTANCE);
        auditedWithSelfConfig = flowConfig(AuditedWithSelfMandatoryType.INSTANCE);
        auditedWithBothConfig = flowConfig(AuditedWithSelfAndAncestorMandatoryType.INSTANCE);

        auditedWithAncestorPL = persistenceLayer();
        auditedWithSelfPL = persistenceLayer();
        auditedWithBothPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

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
    public void entityWithExternalMandatory_AndFieldsInCmd_ShouldCreateMandatoryFieldsAndFieldRecords() {
        auditedWithAncestorPL.create(singletonList(createAuditedWithAncestorCommand()
                                                       .with(AUD_WITH_ANC_FK_FIELD, ANCESTOR_ID)
                                                       .with(AUD_WITH_ANC_NAME_FIELD, NAME)
                                                       .with(AUD_WITH_ANC_DESC_FIELD, DESC)),
                                     auditedWithAncestorConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithAncestorMandatoryType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasMandatoryFieldValue(ANCESTOR_NAME_FIELD, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(ANCESTOR_DESC_FIELD, ANCESTOR_DESC),
                                      hasCreatedFieldRecord(AUD_WITH_ANC_NAME_FIELD, NAME),
                                      hasCreatedFieldRecord(AUD_WITH_ANC_DESC_FIELD, DESC)));
    }

    @Test
    public void entityWithSelfMandatory_AndFieldsInCmd_ShouldCreateMandatoryFieldsAndFieldRecords() {
        auditedWithSelfPL.create(singletonList(createAuditedWithSelfCommand()
                                                   .with(AUD_WITH_SELF_NAME_FIELD, NAME)
                                                   .with(AUD_WITH_SELF_DESC_FIELD, DESC)),
                                 auditedWithSelfConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithSelfMandatoryType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasMandatoryFieldValue(AUD_WITH_SELF_NAME_FIELD, NAME),
                                      hasCreatedFieldRecord(AUD_WITH_SELF_NAME_FIELD, NAME),
                                      hasCreatedFieldRecord(AUD_WITH_SELF_DESC_FIELD, DESC)));
    }

    @Test
    public void entityWithSelfAndExternalMandatory_AndFieldsInCmd_ShouldCreateMandatoryFieldsForBothAndFieldRecords() {
        auditedWithBothPL.create(singletonList(createAuditedWithBothCommand()
                                                   .with(AUD_WITH_BOTH_FK_FIELD, ANCESTOR_ID)
                                                   .with(AUD_WITH_BOTH_NAME_FIELD, NAME)
                                                   .with(AUD_WITH_BOTH_DESC_FIELD, DESC)),
                                 auditedWithBothConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithSelfAndAncestorMandatoryType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasMandatoryFieldValue(AUD_WITH_BOTH_NAME_FIELD, NAME),
                                      hasMandatoryFieldValue(ANCESTOR_NAME_FIELD, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(ANCESTOR_DESC_FIELD, ANCESTOR_DESC),
                                      hasCreatedFieldRecord(AUD_WITH_BOTH_NAME_FIELD, NAME),
                                      hasCreatedFieldRecord(AUD_WITH_BOTH_DESC_FIELD, DESC)));
    }

    @Test
    public void entityWithExternalMandatory_AndNoFieldsInCmd_ShouldCreateMandatoryFieldsOnly() {
        auditedWithAncestorPL.create(singletonList(createAuditedWithAncestorCommand()
                                                       .with(AUD_WITH_ANC_FK_FIELD, ANCESTOR_ID)),
                                     auditedWithAncestorConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithAncestorMandatoryType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasNoFieldRecords()));
    }

    @Test
    public void entityWithSelfMandatory_AndOnlyMandatoryInCmd_ShouldCreateMandatoryFieldsAndFieldRecordsForSameFields() {
        auditedWithSelfPL.create(singletonList(createAuditedWithSelfCommand()
                                                   .with(AUD_WITH_SELF_NAME_FIELD, NAME)),
                                 auditedWithSelfConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithSelfMandatoryType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasMandatoryFieldValue(AUD_WITH_SELF_NAME_FIELD, NAME),
                                      hasCreatedFieldRecord(AUD_WITH_SELF_NAME_FIELD, NAME)));
    }

    private <E extends EntityType<E>> ChangeFlowConfig<E> flowConfig(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).build();
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }

    private CreateAuditedWithAncestorMandatoryCommand createAuditedWithAncestorCommand() {
        return new CreateAuditedWithAncestorMandatoryCommand();
    }

    private CreateAuditedWithSelfMandatoryCommand createAuditedWithSelfCommand() {
        return new CreateAuditedWithSelfMandatoryCommand();
    }

    private CreateAuditedWithSelfAndAncestorMandatoryCommand createAuditedWithBothCommand() {
        return new CreateAuditedWithSelfAndAncestorMandatoryCommand();
    }

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> AuditRecord<E> typed(final AuditRecord<?> auditRecord) {
        return (AuditRecord<E>) auditRecord;
    }
}
