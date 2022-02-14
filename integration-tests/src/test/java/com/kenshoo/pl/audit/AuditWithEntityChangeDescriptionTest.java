package com.kenshoo.pl.audit;

import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.UpdateAuditedCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditProperties;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.Stream;

import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.hasEntityChangeDescription;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AuditWithEntityChangeDescriptionTest {

    private static final long ID_1 = 1L;

    private static final String OLD_NAME ="old";
    private static final String NEW_NAME ="new";
    private static final String CHANGE_DESCRIPTION = "changed because optimization said so..";

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedType> flowConfig;

    private PersistenceLayer<AuditedType> pl;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        flowConfig = flowConfig(AuditedType.INSTANCE);

        pl = persistenceLayer();

        Stream.of(MainTable.INSTANCE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(MainTable.INSTANCE)
                  .set(MainTable.INSTANCE.id, ID_1)
                  .set(MainTable.INSTANCE.name, OLD_NAME)
                  .execute();
    }

    @After
    public void tearDown() {
        Stream.of(MainTable.INSTANCE)
              .forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedEntityWithEntityChangeDescription() {
        final var updateCmd = new UpdateAuditedCommand(ID_1)
                .with(AuditedType.NAME, NEW_NAME)
                .with(AuditProperties.ENTITY_CHANGE_DESCRIPTION, CHANGE_DESCRIPTION);
        pl.update(singletonList(updateCmd),flowConfig);

        final var auditRecords = auditRecordPublisher.getAuditRecords().collect(toUnmodifiableList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, hasEntityChangeDescription(CHANGE_DESCRIPTION));
    }

    private <E extends EntityType<E>> ChangeFlowConfig<E> flowConfig(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).build();
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }
}
