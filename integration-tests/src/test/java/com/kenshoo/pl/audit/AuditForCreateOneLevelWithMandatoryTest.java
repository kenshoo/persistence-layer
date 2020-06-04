package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.CreateAuditedWithAncestorMandatoryCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.AncestorTable;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorMandatoryType;
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

    private static final EntityField<AuditedWithAncestorMandatoryType, Long> ANCESTOR_FK_FIELD = AuditedWithAncestorMandatoryType.ANCESTOR_ID;
    private static final EntityField<AuditedWithAncestorMandatoryType, String> NAME_FIELD = AuditedWithAncestorMandatoryType.NAME;
    private static final EntityField<AuditedWithAncestorMandatoryType, String> DESC_FIELD = AuditedWithAncestorMandatoryType.DESC;

    private static final EntityField<NotAuditedAncestorType, String> ANCESTOR_NAME_FIELD = NotAuditedAncestorType.NAME;
    private static final EntityField<NotAuditedAncestorType, String> ANCESTOR_DESC_FIELD = NotAuditedAncestorType.DESC;

    private static final List<DataTable> ALL_TABLES = ImmutableList.of(MainWithAncestorTable.INSTANCE,
                                                                       AncestorTable.INSTANCE);

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedWithAncestorMandatoryType> mainWithAncestorMandatoryConfig;

    private PersistenceLayer<AuditedWithAncestorMandatoryType> mainWithAncestorMandatoryPL;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        mainWithAncestorMandatoryConfig = flowConfig(AuditedWithAncestorMandatoryType.INSTANCE);

        mainWithAncestorMandatoryPL = persistenceLayer();

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
    public void oneEntity_WithEntityLevelMandatoryFields_AndFieldsInCmd_ShouldCreateMandatoryFieldsAndFieldRecords() {
        mainWithAncestorMandatoryPL.create(singletonList(createCommand()
                                                             .with(ANCESTOR_FK_FIELD, ANCESTOR_ID)
                                                             .with(NAME_FIELD, NAME)
                                                             .with(DESC_FIELD, DESC)),
                                           mainWithAncestorMandatoryConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithAncestorMandatoryType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasMandatoryFieldValue(ANCESTOR_NAME_FIELD, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(ANCESTOR_DESC_FIELD, ANCESTOR_DESC),
                                      hasCreatedFieldRecord(NAME_FIELD, NAME),
                                      hasCreatedFieldRecord(DESC_FIELD, DESC)));
    }

    private CreateAuditedWithAncestorMandatoryCommand createCommand() {
        return new CreateAuditedWithAncestorMandatoryCommand();
    }

    @Test
    public void oneEntity_WithEntityLevelMandatoryFields_AndNoFieldsInCmd_ShouldCreateMandatoryFieldsOnly() {
        mainWithAncestorMandatoryPL.create(singletonList(createCommand()
                                                             .with(ANCESTOR_FK_FIELD, ANCESTOR_ID)),
                                           mainWithAncestorMandatoryConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithAncestorMandatoryType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasNoFieldRecords()));
    }

    private <E extends EntityType<E>> ChangeFlowConfig<E> flowConfig(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).build();
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> AuditRecord<E> typed(final AuditRecord<?> auditRecord) {
        return (AuditRecord<E>) auditRecord;
    }
}
