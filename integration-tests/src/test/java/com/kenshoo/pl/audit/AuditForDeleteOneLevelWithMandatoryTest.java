package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.DeleteAuditedWithAncestorMandatoryCommand;
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

import static com.kenshoo.pl.entity.ChangeOperation.DELETE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AuditForDeleteOneLevelWithMandatoryTest {

    private static final long ID = 1L;
    private static final String NAME = "name";
    private static final String DESC = "desc";
    private static final String DESC2 = "desc2";

    private static final long ANCESTOR_ID = 11L;
    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";

    private static final List<DataTable> ALL_TABLES = ImmutableList.of(MainWithAncestorTable.INSTANCE,
                                                                       AncestorTable.INSTANCE);
    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedWithAncestorMandatoryType> auditedWithAncestorMandatoryConfig;

    private PersistenceLayer<AuditedWithAncestorMandatoryType> auditedWithAncestorMandatoryPL;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedWithAncestorMandatoryConfig = flowConfig(AuditedWithAncestorMandatoryType.INSTANCE);

        auditedWithAncestorMandatoryPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(MainWithAncestorTable.INSTANCE)
                  .set(MainWithAncestorTable.INSTANCE.id, ID)
                  .set(MainWithAncestorTable.INSTANCE.ancestor_id, ANCESTOR_ID)
                  .set(MainWithAncestorTable.INSTANCE.name, NAME)
                  .set(MainWithAncestorTable.INSTANCE.desc, DESC)
                  .set(MainWithAncestorTable.INSTANCE.desc2, DESC2)
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
    public void oneEntity_WithEntityLevelMandatoryFields_ShouldCreateRecordWithMandatoryFields() {
        auditedWithAncestorMandatoryPL.delete(singletonList(new DeleteAuditedWithAncestorMandatoryCommand(ID)),
                                              auditedWithAncestorMandatoryConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithAncestorMandatoryType> auditRecord = typed(auditRecords.get(0));
        //noinspection unchecked
        assertThat(auditRecord, allOf(hasEntityType(AuditedWithAncestorMandatoryType.INSTANCE),
                                      hasEntityId(String.valueOf(ID)),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasOperator(DELETE)));
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
