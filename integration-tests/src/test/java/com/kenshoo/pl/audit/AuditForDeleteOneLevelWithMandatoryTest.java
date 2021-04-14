package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.DeleteAuditedWithAncestorMandatoryCommand;
import com.kenshoo.pl.audit.commands.DeleteAuditedWithInternalMandatoryCommand;
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

import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.hasMandatoryFieldValue;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AuditForDeleteOneLevelWithMandatoryTest {

    private static final long ID = 1L;
    private static final String NAME = "name";
    private static final String DESC = "desc";

    private static final long ANCESTOR_ID = 11L;
    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";

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
    private PersistenceLayer<AuditedWithInternalMandatoryType> auditedWithInternalPL;

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
        auditedWithInternalPL = persistenceLayer();

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
    public void entityWithExternalMandatory_ShouldCreateRecordWithExternalMandatory() {
        auditedWithAncestorPL.delete(singletonList(deleteWithAncestorCommand()),
                                     auditedWithAncestorConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasMandatoryFieldValue(ANCESTOR_NAME_FIELD, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(ANCESTOR_DESC_FIELD, ANCESTOR_DESC)));
    }

    @Test
    public void entityWithInternalMandatory_ShouldCreateRecordWithInternalMandatory() {
        auditedWithInternalPL.delete(singletonList(deleteWithInternalCommand()),
                                 auditedWithInternalConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, hasMandatoryFieldValue(AuditedWithInternalMandatoryType.NAME, NAME));
    }

    private DeleteAuditedWithAncestorMandatoryCommand deleteWithAncestorCommand() {
        return new DeleteAuditedWithAncestorMandatoryCommand(ID);
    }

    private DeleteAuditedWithInternalMandatoryCommand deleteWithInternalCommand() {
        return new DeleteAuditedWithInternalMandatoryCommand(ID);
    }

    private <E extends EntityType<E>> ChangeFlowConfig<E> flowConfig(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).build();
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }
}
