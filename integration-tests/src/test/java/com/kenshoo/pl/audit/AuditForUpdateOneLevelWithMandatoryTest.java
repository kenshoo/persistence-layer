package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.UpdateAuditedWithAncestorMandatoryCommand;
import com.kenshoo.pl.audit.commands.UpdateAuditedWithInternalMandatoryCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.AncestorTable;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorMandatoryType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithInternalMandatoryType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.hasChangedFieldRecord;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.hasMandatoryFieldValue;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AuditForUpdateOneLevelWithMandatoryTest {

    private static final long ID = 1L;
    private static final String NAME = "name";
    private static final String DESC = "desc";

    private static final long ANCESTOR_ID = 11L;
    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";

    private static final String NEW_NAME = "newName";
    private static final String NEW_DESC = "newDesc";

    private static final EntityField<AuditedWithAncestorMandatoryType, Long> ANCESTOR_FK_FIELD = AuditedWithAncestorMandatoryType.ANCESTOR_ID;

    private static final EntityField<AuditedWithAncestorMandatoryType, String> AUD_WITH_ANC_NAME_FIELD = AuditedWithAncestorMandatoryType.NAME;
    private static final EntityField<AuditedWithAncestorMandatoryType, String> AUD_WITH_ANC_DESC_FIELD = AuditedWithAncestorMandatoryType.DESC;

    private static final EntityField<AuditedWithInternalMandatoryType, String> AUD_WITH_INTERNAL_NAME_FIELD = AuditedWithInternalMandatoryType.NAME;
    private static final EntityField<AuditedWithInternalMandatoryType, String> AUD_WITH_INTERNAL_DESC_FIELD = AuditedWithInternalMandatoryType.DESC;

    private static final EntityField<NotAuditedAncestorType, String> ANCESTOR_NAME_FIELD = NotAuditedAncestorType.NAME;
    private static final EntityField<NotAuditedAncestorType, String> ANCESTOR_DESC_FIELD = NotAuditedAncestorType.DESC;

    private static final List<DataTable> ALL_TABLES = ImmutableList.of(MainTable.INSTANCE,
                                                                       MainWithAncestorTable.INSTANCE,
                                                                       AncestorTable.INSTANCE);

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedWithAncestorMandatoryType> auditedWithAncestorConfig;
    private ChangeFlowConfig<AuditedWithInternalMandatoryType> auditedWithInternalConfig;

    private PersistenceLayer<AuditedWithAncestorMandatoryType> auditedWithAncestorPL;
    private PersistenceLayer<AuditedWithInternalMandatoryType> auditedWithSelfPL;


    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedWithAncestorConfig = flowConfig(AuditedWithAncestorMandatoryType.INSTANCE);
        auditedWithInternalConfig = flowConfig(AuditedWithInternalMandatoryType.INSTANCE);

        auditedWithAncestorPL = persistenceLayer();
        auditedWithSelfPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(MainTable.INSTANCE)
                  .set(MainTable.INSTANCE.id, ID)
                  .set(MainTable.INSTANCE.name, NAME)
                  .set(MainTable.INSTANCE.desc, DESC)
                  .execute();

        dslContext.insertInto(MainWithAncestorTable.INSTANCE)
                  .set(MainWithAncestorTable.INSTANCE.id, ID)
                  .set(MainWithAncestorTable.INSTANCE.ancestor_id, ANCESTOR_ID)
                  .set(MainWithAncestorTable.INSTANCE.name, NAME)
                  .set(MainWithAncestorTable.INSTANCE.desc, DESC)
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
    public void entityWithExternalMandatory_AndFieldsChanged_ShouldCreateExternalMandatory_AndFieldRecordsForAll() {
        auditedWithAncestorPL.update(singletonList(updateAuditedWithAncestorCommand()
                                                       .with(AUD_WITH_ANC_NAME_FIELD, NEW_NAME)
                                                       .with(AUD_WITH_ANC_DESC_FIELD, NEW_DESC)),
                                     auditedWithAncestorConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithAncestorMandatoryType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasMandatoryFieldValue(ANCESTOR_NAME_FIELD, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(ANCESTOR_DESC_FIELD, ANCESTOR_DESC),
                                      hasChangedFieldRecord(AUD_WITH_ANC_NAME_FIELD, NAME, NEW_NAME),
                                      hasChangedFieldRecord(AUD_WITH_ANC_DESC_FIELD, DESC, NEW_DESC)));
    }

    @Test
    public void entityWithExternalMandatory_AndNoFieldsInCmd_ShouldReturnEmpty() {
        auditedWithAncestorPL.update(singletonList(updateAuditedWithAncestorCommand()
                                                       .with(ANCESTOR_FK_FIELD, ANCESTOR_ID)),
                                     auditedWithAncestorConfig);
        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("There should not be any published audit records",
                   auditRecords, is(empty()));
    }

    @Test
    public void entityWithInternalMandatory_AndOnlyMandatoryInCmd_AndChanged_ShouldCreateInternalMandatory_AndFieldRecordsForSame() {
        auditedWithSelfPL.update(singletonList(updateAuditedWithSelfCommand()
                                                   .with(AUD_WITH_INTERNAL_NAME_FIELD, NEW_NAME)),
                                 auditedWithInternalConfig);
        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithInternalMandatoryType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasMandatoryFieldValue(AUD_WITH_INTERNAL_NAME_FIELD, NEW_NAME),
                                      hasChangedFieldRecord(AUD_WITH_INTERNAL_NAME_FIELD, NAME, NEW_NAME)));
    }

    @Test
    public void entityWithInternalMandatory_AndMandatoryAndOthersInCmd_AndChanged_ShouldCreateInternalMandatory_AndFieldRecordsForAll() {
        auditedWithSelfPL.update(singletonList(updateAuditedWithSelfCommand()
                                                   .with(AUD_WITH_INTERNAL_NAME_FIELD, NEW_NAME)
                                                   .with(AUD_WITH_INTERNAL_DESC_FIELD, NEW_DESC)),
                                 auditedWithInternalConfig);
        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithInternalMandatoryType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasMandatoryFieldValue(AUD_WITH_INTERNAL_NAME_FIELD, NEW_NAME),
                                      hasChangedFieldRecord(AUD_WITH_INTERNAL_NAME_FIELD, NAME, NEW_NAME),
                                      hasChangedFieldRecord(AUD_WITH_INTERNAL_DESC_FIELD, DESC, NEW_DESC)));
    }

    private <E extends EntityType<E>> ChangeFlowConfig<E> flowConfig(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).build();
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }

    private UpdateAuditedWithAncestorMandatoryCommand updateAuditedWithAncestorCommand() {
        return new UpdateAuditedWithAncestorMandatoryCommand(ID);
    }

    private UpdateAuditedWithInternalMandatoryCommand updateAuditedWithSelfCommand() {
        return new UpdateAuditedWithInternalMandatoryCommand(ID);
    }

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> AuditRecord<E> typed(final AuditRecord<?> auditRecord) {
        return (AuditRecord<E>) auditRecord;
    }
}
