package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.TestChildEntityTable;
import com.kenshoo.pl.entity.internal.audit.TestEntityTable;
import com.kenshoo.pl.entity.internal.audit.*;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.kenshoo.pl.entity.ChangeOperation.DELETE;
import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AuditForUpdateTwoLevelsTest {

    private static final long PARENT_ID_1 = 1L;
    private static final long PARENT_ID_2 = 2L;
    private static final long CHILD_ID_11 = 11L;
    private static final long CHILD_ID_12 = 12L;
    private static final long CHILD_ID_21 = 21L;
    private static final long CHILD_ID_22 = 22L;

    private static final long INVALID_ID = 999L;

    private static final String PARENT_NAME_1 = "parentName1";
    private static final String NEW_PARENT_NAME_1 = "newParentName1";
    private static final String PARENT_NAME_2 = "parentName2";
    private static final String NEW_PARENT_NAME_2 = "newParentName2";

    private static final String CHILD_NAME_11 = "childName11";
    private static final String NEW_CHILD_NAME_11 = "newChildName11";
    private static final String CHILD_NAME_12 = "childName12";
    private static final String NEW_CHILD_NAME_12 = "newChildName12";
    private static final String CHILD_NAME_21 = "childName21";
    private static final String NEW_CHILD_NAME_21 = "newChildName21";
    private static final String CHILD_NAME_22 = "childName22";

    private static final List<? extends DataTable> ALL_TABLES =
        ImmutableList.of(TestEntityTable.INSTANCE, TestChildEntityTable.INSTANCE);

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private PersistenceLayer<TestAuditedEntityType> auditedEntityPL;
    private PersistenceLayer<TestAuditedEntityWithoutDataFieldsType> auditedEntityWithoutDataFieldsPL;
    private PersistenceLayer<TestEntityType> notAuditedEntityPL;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedEntityPL = persistenceLayer();
        auditedEntityWithoutDataFieldsPL = persistenceLayer();
        notAuditedEntityPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(TestEntityTable.INSTANCE)
                  .columns(TestEntityTable.INSTANCE.id, TestEntityTable.INSTANCE.name)
                  .values(PARENT_ID_1, PARENT_NAME_1)
                  .values(PARENT_ID_2, PARENT_NAME_2)
                  .execute();

        dslContext.insertInto(TestChildEntityTable.INSTANCE)
                  .columns(TestChildEntityTable.INSTANCE.id, TestChildEntityTable.INSTANCE.parent_id, TestChildEntityTable.INSTANCE.name)
                  .values(CHILD_ID_11, PARENT_ID_1, CHILD_NAME_11)
                  .values(CHILD_ID_12, PARENT_ID_1, CHILD_NAME_12)
                  .values(CHILD_ID_21, PARENT_ID_2, CHILD_NAME_21)
                  .values(CHILD_ID_22, PARENT_ID_2, CHILD_NAME_22)
                  .execute();
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_BothChanged_ShouldCreateRecordForParentAndChild() {
        final UpdateTestAuditedChild1EntityCommand childCmd =
            new UpdateTestAuditedChild1EntityCommand(CHILD_ID_11)
                .with(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11);
        final UpdateTestAuditedEntityCommand parentCmd =
            new UpdateTestAuditedEntityCommand(PARENT_ID_1)
                .with(TestAuditedEntityType.NAME, NEW_PARENT_NAME_1)
                .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(TestAuditedEntityType.NAME, PARENT_NAME_1, NEW_PARENT_NAME_1)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(TestAuditedChild1EntityType.NAME,
                                                                               CHILD_NAME_11,
                                                                               NEW_CHILD_NAME_11))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildrenSameType_AllChanged_ShouldCreateRecordsForBothChildren() {
        final UpdateTestAuditedChild1EntityCommand child11Cmd =
            new UpdateTestAuditedChild1EntityCommand(CHILD_ID_11)
                .with(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11);
        final UpdateTestAuditedChild1EntityCommand child12Cmd =
            new UpdateTestAuditedChild1EntityCommand(CHILD_ID_12)
                .with(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_12);

        final UpdateTestAuditedEntityCommand parentCmd = new UpdateTestAuditedEntityCommand(PARENT_ID_1)
            .with(TestAuditedEntityType.NAME, NEW_PARENT_NAME_1)
            .with(child11Cmd)
            .with(child12Cmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(TestAuditedChild1EntityType.NAME,
                                                                               CHILD_NAME_11,
                                                                               NEW_CHILD_NAME_11))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(TestAuditedChild1EntityType.NAME,
                                                                               CHILD_NAME_12,
                                                                               NEW_CHILD_NAME_12))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildrenDifferentTypes_AllChanged_ShouldCreateRecordsForBothChildren() {
        final UpdateTestAuditedChild1EntityCommand child11Cmd =
            new UpdateTestAuditedChild1EntityCommand(CHILD_ID_11)
                .with(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11);
        final UpdateTestAuditedChild2EntityCommand child21Cmd =
            new UpdateTestAuditedChild2EntityCommand(CHILD_ID_12)
                .with(TestAuditedChild2EntityType.NAME, NEW_CHILD_NAME_12);

        final UpdateTestAuditedEntityCommand parentCmd = new UpdateTestAuditedEntityCommand(PARENT_ID_1)
            .with(TestAuditedEntityType.NAME, NEW_PARENT_NAME_1)
            .with(child11Cmd)
            .with(child21Cmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild2EntityType.INSTANCE))
                .build();

        auditedEntityPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(TestAuditedChild1EntityType.NAME,
                                                                               CHILD_NAME_11,
                                                                               NEW_CHILD_NAME_11))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild2EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(TestAuditedChild2EntityType.NAME,
                                                                               CHILD_NAME_12,
                                                                               NEW_CHILD_NAME_12))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_OneNotAuditedChild_AllChanged_ShouldCreateRecordForAuditedChildOnly() {
        final UpdateTestAuditedChild1EntityCommand auditedChildCmd =
            new UpdateTestAuditedChild1EntityCommand(CHILD_ID_11)
                .with(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11);
        final UpdateTestChildEntityCommand notAuditedChildCmd =
            new UpdateTestChildEntityCommand(CHILD_ID_12)
                .with(TestChildEntityType.NAME, NEW_CHILD_NAME_12);

        final UpdateTestAuditedEntityCommand parentCmd = new UpdateTestAuditedEntityCommand(PARENT_ID_1)
            .with(TestAuditedEntityType.NAME, NEW_PARENT_NAME_1)
            .with(auditedChildCmd)
            .with(notAuditedChildCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestChildEntityType.INSTANCE))
                .build();

        auditedEntityPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE))));
        assertThat(auditRecord, not(hasChildRecordThat(hasEntityType(TestChildEntityType.INSTANCE))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_WithDeletionOfOthers_ShouldCreateDeletionRecordForOtherChild() {
        final UpdateTestAuditedChild1EntityCommand childCmd =
            new UpdateTestAuditedChild1EntityCommand(CHILD_ID_11)
                .with(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11);

        final UpdateTestAuditedEntityCommand cmd = new UpdateTestAuditedEntityCommand(PARENT_ID_1)
            .with(childCmd)
            .with(new DeletionOfOther<>(TestAuditedChild1EntityType.INSTANCE));

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.update(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void oneAuditedParentThatExists_OneAuditedChildThatDoesntExist_ShouldReturnEmpty() {
        final UpdateTestAuditedChild1EntityCommand childCmd =
            new UpdateTestAuditedChild1EntityCommand(INVALID_ID)
                .with(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11);

        final UpdateTestAuditedEntityCommand cmd = new UpdateTestAuditedEntityCommand(PARENT_ID_1)
            .with(TestAuditedEntityType.NAME, NEW_PARENT_NAME_1)
            .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.update(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneNotAuditedParent_OneAuditedChild_BothChanged_ShouldReturnEmpty() {
        final UpdateTestAuditedChild1EntityCommand childCmd =
            new UpdateTestAuditedChild1EntityCommand(CHILD_ID_11)
                .with(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11);

        final UpdateTestEntityCommand parentCmd = new UpdateTestEntityCommand(PARENT_ID_1)
            .with(TestEntityType.NAME, NEW_PARENT_NAME_1)
            .with(childCmd);

        final ChangeFlowConfig<TestEntityType> flowConfig =
            flowConfigBuilder(TestEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        notAuditedEntityPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneAuditedParent_OneNotAuditedChild_BothChanged_ShouldCreateRecordForParentOnly() {
        final UpdateTestChildEntityCommand childCmd =
            new UpdateTestChildEntityCommand(CHILD_ID_11)
                .with(TestChildEntityType.NAME, NEW_CHILD_NAME_11);

        final UpdateTestAuditedEntityCommand parentCmd = new UpdateTestAuditedEntityCommand(PARENT_ID_1)
            .with(TestAuditedEntityType.NAME, NEW_PARENT_NAME_1)
            .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestChildEntityType.INSTANCE))
                .build();

        auditedEntityPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE),
                                      not(hasChildRecordThat(hasEntityType(TestChildEntityType.INSTANCE)))));
    }

    @Test
    public void oneAuditedParentWithoutDataFields_OneAuditedChild_ShouldCreateRecordForParentAndChild() {

        final UpdateTestAuditedChild1EntityCommand childCmd =
            new UpdateTestAuditedChild1EntityCommand(CHILD_ID_11)
                .with(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11);
        final UpdateTestAuditedEntityWithoutDataFieldsCommand parentCmd =
            new UpdateTestAuditedEntityWithoutDataFieldsCommand(PARENT_ID_1)
                .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityWithoutDataFieldsType> flowConfig =
            flowConfigBuilder(TestAuditedEntityWithoutDataFieldsType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityWithoutDataFieldsPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityWithoutDataFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityWithoutDataFieldsType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE))));
    }

    @Test
    public void oneAuditedParentUnchanged_OneAuditedChildChanged_ShouldCreateRecordForParentAndChild() {

        final UpdateTestAuditedChild1EntityCommand childCmd =
            new UpdateTestAuditedChild1EntityCommand(CHILD_ID_11)
                .with(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11);
        final UpdateTestAuditedEntityCommand parentCmd =
            new UpdateTestAuditedEntityCommand(PARENT_ID_1)
                .with(TestAuditedEntityType.NAME, PARENT_NAME_1)
                .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE))));
    }

    @Test
    public void oneAuditedParentChanged_OneAuditedChildUnchanged_ShouldCreateRecordForParentOnly() {

        final UpdateTestAuditedChild1EntityCommand childCmd =
            new UpdateTestAuditedChild1EntityCommand(CHILD_ID_11)
                .with(TestAuditedChild1EntityType.NAME, CHILD_NAME_11);
        final UpdateTestAuditedEntityCommand parentCmd =
            new UpdateTestAuditedEntityCommand(PARENT_ID_1)
                .with(TestAuditedEntityType.NAME, NEW_PARENT_NAME_1)
                .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE),
                                      not(hasChildRecordThat(hasEntityType(TestAuditedChild1EntityType.INSTANCE)))));
    }

    @Test
    public void twoAuditedParents_OneAuditedChildEach_AllChanged_ShouldCreateRecordsForBothChildren() {
        final UpdateTestAuditedChild1EntityCommand child1Cmd =
            new UpdateTestAuditedChild1EntityCommand(CHILD_ID_11)
                .with(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11);
        final UpdateTestAuditedChild2EntityCommand child2Cmd =
            new UpdateTestAuditedChild2EntityCommand(CHILD_ID_21)
                .with(TestAuditedChild2EntityType.NAME, NEW_CHILD_NAME_21);

        final List<UpdateTestAuditedEntityCommand> cmds =
            ImmutableList.of(new UpdateTestAuditedEntityCommand(PARENT_ID_1)
                                 .with(TestAuditedEntityType.NAME, NEW_PARENT_NAME_1)
                                 .with(child1Cmd),
                             new UpdateTestAuditedEntityCommand(PARENT_ID_2)
                                 .with(TestAuditedEntityType.NAME, NEW_PARENT_NAME_2)
                                 .with(child2Cmd));

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild2EntityType.INSTANCE))
                .build();

        auditedEntityPL.update(cmds, flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord<TestAuditedEntityType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                          hasEntityId(String.valueOf(CHILD_ID_11)),
                                                          hasOperator(UPDATE))));

        final AuditRecord<TestAuditedEntityType> auditRecord2 = typed(auditRecords.get(1));
        assertThat(auditRecord2, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild2EntityType.INSTANCE),
                                                          hasEntityId(String.valueOf(CHILD_ID_21)),
                                                          hasOperator(UPDATE))));
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
