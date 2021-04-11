package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.*;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.ChildTable;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.kenshoo.pl.entity.ChangeOperation.DELETE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AuditForDeleteTwoLevelsTest {

    private static final long PARENT_ID_1 = 1L;
    private static final long PARENT_ID_2 = 2L;
    private static final long CHILD_ID_11 = 11L;
    private static final long CHILD_ID_12 = 12L;
    private static final long CHILD_ID_21 = 21L;
    private static final long CHILD_ID_22 = 22L;

    private static final long INVALID_ID = 999L;

    private static final List<? extends DataTable> ALL_TABLES =
        ImmutableList.of(MainTable.INSTANCE, ChildTable.INSTANCE);

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private PersistenceLayer<AuditedType> auditedParentPL;
    private PersistenceLayer<AuditedWithoutDataFieldsType> auditedParentWithoutDataFieldsPL;
    private PersistenceLayer<NotAuditedType> notAuditedParentPL;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedParentPL = persistenceLayer();
        auditedParentWithoutDataFieldsPL = persistenceLayer();
        notAuditedParentPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(MainTable.INSTANCE)
                  .columns(MainTable.INSTANCE.id, MainTable.INSTANCE.name)
                  .values(PARENT_ID_1, "parentName1")
                  .values(PARENT_ID_2, "parentName2")
                  .execute();

        dslContext.insertInto(ChildTable.INSTANCE)
                  .columns(ChildTable.INSTANCE.id, ChildTable.INSTANCE.parent_id, ChildTable.INSTANCE.name)
                  .values(CHILD_ID_11, PARENT_ID_1, "childName11")
                  .values(CHILD_ID_12, PARENT_ID_1, "childName12")
                  .values(CHILD_ID_21, PARENT_ID_2, "childName21")
                  .values(CHILD_ID_22, PARENT_ID_2, "childName22")
                  .execute();
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_BothExist_ShouldCreateRecordForParentAndChild() {
        final DeleteAuditedCommand cmd =
            new DeleteAuditedCommand(PARENT_ID_1)
                .with(new DeleteAuditedChild1Command(CHILD_ID_11));

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(DELETE)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildrenSameType_AllExist_ShouldCreateRecordsForBothChildren() {
        final DeleteAuditedCommand cmd = new DeleteAuditedCommand(PARENT_ID_1)
            .with(AuditedType.NAME, "name")
            .with(new DeleteAuditedChild1Command(CHILD_ID_11))
            .with(new DeleteAuditedChild1Command(CHILD_ID_12));

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(DELETE))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildrenDifferentTypes_AllExist_ShouldCreateRecordsForBothChildren() {
        final DeleteAuditedCommand parentCmd = new DeleteAuditedCommand(PARENT_ID_1)
            .with(new DeleteAuditedChild1Command(CHILD_ID_11))
            .with(new DeleteAuditedChild2Command(CHILD_ID_12));

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild2Type.INSTANCE))
                .build();

        auditedParentPL.delete(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(DELETE))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild2Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_OneNotAuditedChild_AllExist_ShouldCreateRecordForAuditedChildOnly() {
        final DeleteAuditedCommand cmd = new DeleteAuditedCommand(PARENT_ID_1)
            .with(new DeleteAuditedChild1Command(CHILD_ID_11))
            .with(new DeleteNotAuditedChildCommand(CHILD_ID_12));

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(NotAuditedChildType.INSTANCE))
                .build();

        auditedParentPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(DELETE))));
        assertThat(auditRecord, not(hasChildRecordThat(hasEntityType(NotAuditedChildType.INSTANCE.getName()))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_BothExist_WithDeletionOfOthers_ShouldCreateRecordsForBothChildren() {
        final DeleteAuditedCommand cmd = new DeleteAuditedCommand(PARENT_ID_1)
            .with(new DeleteAuditedChild1Command(CHILD_ID_11))
            .with(new DeletionOfOther<>(AuditedChild1Type.INSTANCE));

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(DELETE))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void oneAuditedParentThatExists_OneAuditedChildThatDoesntExist_ShouldReturnEmpty() {
        final DeleteAuditedCommand cmd =
            new DeleteAuditedCommand(PARENT_ID_1)
                .with(new DeleteAuditedChild1Command(INVALID_ID));

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneNotAuditedParent_OneAuditedChild_BothExist_ShouldReturnEmpty() {
        final DeleteNotAuditedCommand cmd =
            new DeleteNotAuditedCommand(PARENT_ID_1)
                .with(new DeleteAuditedChild1Command(CHILD_ID_11));

        final ChangeFlowConfig<NotAuditedType> flowConfig =
            flowConfigBuilder(NotAuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        notAuditedParentPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneAuditedParentWithoutDataFields_OneAuditedChild_ShouldCreateRecordForParentAndChild() {
        final DeleteAuditedWithoutDataFieldsCommand cmd =
            new DeleteAuditedWithoutDataFieldsCommand(PARENT_ID_1)
                .with(new DeleteAuditedChild1Command(CHILD_ID_11));

        final ChangeFlowConfig<AuditedWithoutDataFieldsType> flowConfig =
            flowConfigBuilder(AuditedWithoutDataFieldsType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentWithoutDataFieldsPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithoutDataFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(AuditedWithoutDataFieldsType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(DELETE)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void twoAuditedParents_OneAuditedChildEach_AllExist_ShouldCreateRecordsForBothChildren() {
        final List<DeleteAuditedCommand> cmds =
            ImmutableList.of(new DeleteAuditedCommand(PARENT_ID_1)
                                 .with(new DeleteAuditedChild1Command(CHILD_ID_11)),
                             new DeleteAuditedCommand(PARENT_ID_2)
                                 .with(new DeleteAuditedChild2Command(CHILD_ID_21)));

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild2Type.INSTANCE))
                .build();

        auditedParentPL.delete(cmds, flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord<AuditedType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                          hasEntityId(String.valueOf(CHILD_ID_11)),
                                                          hasOperator(DELETE))));

        final AuditRecord<AuditedType> auditRecord2 = typed(auditRecords.get(1));
        assertThat(auditRecord2, hasChildRecordThat(allOf(hasEntityType(AuditedChild2Type.INSTANCE.getName()),
                                                          hasEntityId(String.valueOf(CHILD_ID_21)),
                                                          hasOperator(DELETE))));
    }

    private <E extends EntityType<E>> ChangeFlowConfig.Builder<E> flowConfigBuilder(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType);
    }


    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> AuditRecord<E> typed(final AuditRecord<?> auditRecord) {
        return (AuditRecord<E>) auditRecord;
    }
}
