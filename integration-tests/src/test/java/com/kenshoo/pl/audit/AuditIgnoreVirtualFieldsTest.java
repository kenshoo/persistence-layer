package com.kenshoo.pl.audit;

import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.CreateAuditedWithVirtualCommand;
import com.kenshoo.pl.audit.commands.CreateInclusiveAuditedWithVirtualCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithVirtualType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.InclusiveAuditedWithVirtualType;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.hasFieldRecordFor;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.hasNoFieldRecordFor;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AuditIgnoreVirtualFieldsTest {

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedWithVirtualType> auditedFlowConfig;
    private ChangeFlowConfig<InclusiveAuditedWithVirtualType> inclusiveAuditedFlowConfig;

    private PersistenceLayer<AuditedWithVirtualType> auditedPL;
    private PersistenceLayer<InclusiveAuditedWithVirtualType> inclusiveAuditedPL;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedFlowConfig = flowConfig(AuditedWithVirtualType.INSTANCE);
        inclusiveAuditedFlowConfig = flowConfig(InclusiveAuditedWithVirtualType.INSTANCE);

        auditedPL = persistenceLayer();
        inclusiveAuditedPL = persistenceLayer();

        Stream.of(MainAutoIncIdTable.INSTANCE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));

    }

    @After
    public void tearDown() {
        Stream.of(MainAutoIncIdTable.INSTANCE)
              .forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void auditedEntity_RealAndVirtualFields_ShouldCreateFieldRecordsForRealOnly() {
        auditedPL.create(singletonList(new CreateAuditedWithVirtualCommand()
                                           .with(AuditedWithVirtualType.NAME, "name")
                                           .with(AuditedWithVirtualType.DESC, "desc")
                                           .with(AuditedWithVirtualType.VIRTUAL_DESC_1, "name-desc")
                                           .with(AuditedWithVirtualType.VIRTUAL_DESC_2, "name:desc")),
                         auditedFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasFieldRecordFor(AuditedWithVirtualType.NAME),
                                      hasFieldRecordFor(AuditedWithVirtualType.DESC),
                                      hasNoFieldRecordFor(AuditedWithVirtualType.VIRTUAL_DESC_1),
                                      hasNoFieldRecordFor(AuditedWithVirtualType.VIRTUAL_DESC_2)));
    }

    @Test
    public void inclusiveAuditedEntity_RealAndVirtualFields_ShouldCreateFieldRecordsForRealOnly() {
        inclusiveAuditedPL.create(singletonList(new CreateInclusiveAuditedWithVirtualCommand()
                                                    .with(InclusiveAuditedWithVirtualType.NAME, "name")
                                                    .with(InclusiveAuditedWithVirtualType.DESC, "desc")
                                                    .with(InclusiveAuditedWithVirtualType.VIRTUAL_DESC, "name-desc")),
                                                inclusiveAuditedFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasFieldRecordFor(InclusiveAuditedWithVirtualType.NAME),
                                      hasFieldRecordFor(InclusiveAuditedWithVirtualType.DESC),
                                      hasNoFieldRecordFor(InclusiveAuditedWithVirtualType.VIRTUAL_DESC)));
    }

    private <E extends EntityType<E>> ChangeFlowConfig<E> flowConfig(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).build();
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }
}
