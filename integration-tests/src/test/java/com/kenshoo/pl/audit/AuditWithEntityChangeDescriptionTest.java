package com.kenshoo.pl.audit;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.UpdateAuditedChild1Command;
import com.kenshoo.pl.audit.commands.UpdateAuditedCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditProperties;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.ChildTable;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild1Type;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.hasChildRecordThat;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.hasEntityChangeDescription;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AuditWithEntityChangeDescriptionTest {

    private static final long PARENT_ID_1 = 1L;
    private static final long PARENT_ID_2 = 2L;

    private static final long CHILD_ID_11 = 11L;

    private static final String PARENT_OLD_NAME_1 = "parentOldName1";
    private static final String PARENT_OLD_NAME_2 = "parentOldName2";
    private static final String PARENT_NEW_NAME_1 = "parentNewName1";
    private static final String PARENT_NEW_NAME_2 = "parentNewName2";

    private static final String CHILD_OLD_NAME_11 = "childOldName11";
    private static final String CHILD_NEW_NAME_11 = "childNewName11";


    private static final String PARENT_ENTITY_CHANGE_DESCRIPTION_1 = "changed because optimization said so.. ";
    private static final String PARENT_ENTITY_CHANGE_DESCRIPTION_2 = "changed because client needed to";
    private static final String CHILD_ENTITY_CHANGE_DESCRIPTION_11 = "changed child because optimization said so.. ";

    private static final List<? extends DataTable> ALL_TABLES = List.of(MainTable.INSTANCE, ChildTable.INSTANCE);

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedType> parentFlowConfig;

    private PersistenceLayer<AuditedType> pl;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
                .withFeaturePredicate(__ -> true)
                .withAuditRecordPublisher(auditRecordPublisher)
                .build();

        parentFlowConfig = flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        pl = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(MainTable.INSTANCE)
                .set(MainTable.INSTANCE.id, PARENT_ID_1)
                .set(MainTable.INSTANCE.name, PARENT_OLD_NAME_1)
                .execute();
        dslContext.insertInto(MainTable.INSTANCE)
                .set(MainTable.INSTANCE.id, PARENT_ID_2)
                .set(MainTable.INSTANCE.name, PARENT_OLD_NAME_2)
                .execute();

        dslContext.insertInto(ChildTable.INSTANCE)
                .columns(ChildTable.INSTANCE.id, ChildTable.INSTANCE.parent_id, ChildTable.INSTANCE.name)
                .values(CHILD_ID_11, PARENT_ID_1, CHILD_OLD_NAME_11)
                .execute();
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedEntityWithEntityChangeDescription() {
        final var updateCmd = new UpdateAuditedCommand(PARENT_ID_1)
                .with(AuditedType.NAME, PARENT_NEW_NAME_1)
                .with(AuditProperties.ENTITY_CHANGE_DESCRIPTION, PARENT_ENTITY_CHANGE_DESCRIPTION_1);
        pl.update(singletonList(updateCmd), parentFlowConfig);

        final var auditRecords = auditRecordPublisher.getAuditRecords().collect(toUnmodifiableList());

        assertThat("Incorrect number of published records",
                auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, hasEntityChangeDescription(PARENT_ENTITY_CHANGE_DESCRIPTION_1));
    }

    @Test
    public void twoAuditedEntitiesWithEntityChangeDescriptions() {
        final var updateCmd1 = new UpdateAuditedCommand(PARENT_ID_1)
                .with(AuditedType.NAME, PARENT_NEW_NAME_1)
                .with(AuditProperties.ENTITY_CHANGE_DESCRIPTION, PARENT_ENTITY_CHANGE_DESCRIPTION_1);
        final var updateCmd2 = new UpdateAuditedCommand(PARENT_ID_2)
                .with(AuditedType.NAME, PARENT_NEW_NAME_2)
                .with(AuditProperties.ENTITY_CHANGE_DESCRIPTION, PARENT_ENTITY_CHANGE_DESCRIPTION_2);

        pl.update(List.of(updateCmd1, updateCmd2), parentFlowConfig);

        final var auditRecords = auditRecordPublisher.getAuditRecords().collect(toUnmodifiableList());

        assertThat("Incorrect number of published records",
                auditRecords, hasSize(2));
        assertThat("Incorrect entity change description in first entity: ",
                auditRecords.get(0), hasEntityChangeDescription(PARENT_ENTITY_CHANGE_DESCRIPTION_1));
        assertThat("Incorrect entity change description in second entity: ",
                auditRecords.get(1), hasEntityChangeDescription(PARENT_ENTITY_CHANGE_DESCRIPTION_2));
    }

    @Test
    public void oneAuditedEntityWithAuditedChildEntityChangeDescription() {
        final var childUpdateCmd = new UpdateAuditedChild1Command(CHILD_ID_11)
                .with(AuditedChild1Type.NAME, CHILD_NEW_NAME_11)
                .with(AuditProperties.ENTITY_CHANGE_DESCRIPTION, CHILD_ENTITY_CHANGE_DESCRIPTION_11);
        final var parentUpdateCmd = new UpdateAuditedCommand(PARENT_ID_1)
                .with(AuditedType.NAME, PARENT_NEW_NAME_1)
                .with(childUpdateCmd);

        pl.update(singletonList(parentUpdateCmd), parentFlowConfig);

        final var auditRecords = auditRecordPublisher.getAuditRecords().collect(toUnmodifiableList());

        assertThat("Incorrect number of published records",
                auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, hasChildRecordThat(hasEntityChangeDescription(CHILD_ENTITY_CHANGE_DESCRIPTION_11)));
    }

    private <E extends EntityType<E>> ChangeFlowConfig.Builder<E> flowConfigBuilder(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType);
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }
}
