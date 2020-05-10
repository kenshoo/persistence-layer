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

import static com.kenshoo.pl.entity.ChangeOperation.*;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AuditForUpsertTwoLevelsTest {

    private static final long PARENT_ID_1 = 1L;
    private static final long PARENT_ID_2 = 2L;
    private static final long CHILD_ID_11 = 11L;
    private static final long CHILD_ID_12 = 12L;
    private static final long CHILD_ID_21 = 21L;
    private static final long CHILD_ID_22 = 22L;

    private static final String PARENT_NAME_1 = "parentName1";
    private static final String NEW_PARENT_NAME_1 = "newParentName1";
    private static final String PARENT_NAME_2 = "parentName2";
    private static final String NEW_PARENT_NAME_2 = "newParentName2";

    private static final String PARENT_DESC_1 = "parentDesc1";
    private static final String NEW_PARENT_DESC_1 = "newParentDesc1";
    private static final String PARENT_DESC_2 = "parentDesc2";
    private static final String NEW_PARENT_DESC_2 = "newParentDesc2";

    private static final String CHILD_NAME_11 = "childName11";
    private static final String NEW_CHILD_NAME_11 = "newChildName11";
    private static final String CHILD_NAME_12 = "childName12";
    private static final String NEW_CHILD_NAME_12 = "newChildName12";
    private static final String CHILD_NAME_21 = "childName21";
    private static final String NEW_CHILD_NAME_21 = "newChildName21";
    private static final String CHILD_NAME_22 = "childName22";

    private static final String CHILD_DESC_11 = "childDesc11";
    private static final String NEW_CHILD_DESC_11 = "newChildDesc11";
    private static final String CHILD_DESC_12 = "childDesc12";
    private static final String NEW_CHILD_DESC_12 = "newChildDesc12";
    private static final String CHILD_DESC_21 = "childDesc21";
    private static final String NEW_CHILD_DESC_21 = "newChildDesc21";
    private static final String CHILD_DESC_22 = "childDesc22";

    private static final TestEntityTable parent_table = TestEntityTable.INSTANCE;
    private static final TestChildEntityTable child_table = TestChildEntityTable.INSTANCE;

    private static final List<? extends DataTable> ALL_TABLES = ImmutableList.of(parent_table, child_table);

    private PLContext plContext;
    private DSLContext dslContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private PersistenceLayer<TestAuditedEntityType> auditedEntityPL;
    private PersistenceLayer<TestEntityType> notAuditedEntityPL;

    @Before
    public void setUp() {
        dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedEntityPL = persistenceLayer();
        notAuditedEntityPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(parent_table)
                  .columns(parent_table.id, parent_table.name, parent_table.desc)
                  .values(PARENT_ID_1, PARENT_NAME_1, PARENT_DESC_1)
                  .values(PARENT_ID_2, PARENT_NAME_2, PARENT_DESC_2)
                  .execute();

        dslContext.insertInto(child_table)
                  .columns(child_table.id, child_table.parent_id, child_table.name, child_table.desc)
                  .values(CHILD_ID_11, PARENT_ID_1, CHILD_NAME_11, CHILD_DESC_11)
                  .values(CHILD_ID_12, PARENT_ID_1, CHILD_NAME_12, CHILD_DESC_12)
                  .values(CHILD_ID_21, PARENT_ID_2, CHILD_NAME_21, CHILD_DESC_21)
                  .values(CHILD_ID_22, PARENT_ID_2, CHILD_NAME_22, CHILD_DESC_22)
                  .execute();
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_BothNew_ShouldGenerateCreateRecordsForBoth() {
        final UpsertTestAuditedChild1EntityCommand childCmd = new UpsertTestAuditedChild1EntityCommand(NEW_CHILD_NAME_11);
        final UpsertTestAuditedEntityCommand parentCmd = new UpsertTestAuditedEntityCommand(NEW_PARENT_NAME_1)
            .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(singletonList(parentCmd), flowConfig);

        final long parentId = fetchNewParent1IdByName();
        final long childId = fetchChildIdByName(NEW_CHILD_NAME_11);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(parentId)),
                                      hasOperator(CREATE),
                                      hasCreatedFieldRecord(TestAuditedEntityType.NAME, NEW_PARENT_NAME_1)));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(childId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_BothChanged_ShouldGenerateUpdateRecordsForBoth() {
        final UpsertTestAuditedChild1EntityCommand childCmd =
            new UpsertTestAuditedChild1EntityCommand(CHILD_NAME_11)
                .with(TestAuditedChild1EntityType.DESC, NEW_CHILD_DESC_11);
        final UpsertTestAuditedEntityCommand parentCmd =
            new UpsertTestAuditedEntityCommand(PARENT_NAME_1)
                .with(TestAuditedEntityType.DESC, NEW_PARENT_DESC_1)
                .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(TestAuditedEntityType.DESC, PARENT_DESC_1, NEW_PARENT_DESC_1)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(TestAuditedChild1EntityType.DESC,
                                                                               CHILD_DESC_11,
                                                                               NEW_CHILD_DESC_11))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildren_AllNew_ShouldGenerateCreateRecordsForBothChildren() {
        final UpsertTestAuditedChild1EntityCommand child11Cmd = new UpsertTestAuditedChild1EntityCommand(NEW_CHILD_NAME_11);
        final UpsertTestAuditedChild1EntityCommand child12Cmd = new UpsertTestAuditedChild1EntityCommand(NEW_CHILD_NAME_12);
        final UpsertTestAuditedEntityCommand parentCmd = new UpsertTestAuditedEntityCommand(NEW_PARENT_NAME_1)
            .with(child11Cmd)
            .with(child12Cmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(singletonList(parentCmd), flowConfig);

        final long child11Id = fetchChildIdByName(NEW_CHILD_NAME_11);
        final long child12Id = fetchChildIdByName(NEW_CHILD_NAME_12);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(child11Id)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(child12Id)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_12))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildren_AllChanged_ShouldGenerateUpdateRecordsForBothChildren() {
        final UpsertTestAuditedChild1EntityCommand child11Cmd =
            new UpsertTestAuditedChild1EntityCommand(CHILD_NAME_11)
                .with(TestAuditedChild1EntityType.DESC, NEW_CHILD_DESC_11);
        final UpsertTestAuditedChild1EntityCommand child12Cmd =
            new UpsertTestAuditedChild1EntityCommand(CHILD_NAME_12)
                .with(TestAuditedChild1EntityType.DESC, NEW_CHILD_DESC_12);

        final UpsertTestAuditedEntityCommand parentCmd = new UpsertTestAuditedEntityCommand(PARENT_NAME_1)
            .with(TestAuditedEntityType.DESC, NEW_PARENT_DESC_1)
            .with(child11Cmd)
            .with(child12Cmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(TestAuditedChild1EntityType.DESC,
                                                                               CHILD_DESC_11,
                                                                               NEW_CHILD_DESC_11))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(TestAuditedChild1EntityType.DESC,
                                                                               CHILD_DESC_12,
                                                                               NEW_CHILD_DESC_12))));
    }

    @Test
    public void oneChangedAuditedParent_OneNewAuditedChild_OneChangedAuditedChild_ShouldGenerateCreateAndUpdateRecordsForChildren() {
        final UpsertTestAuditedChild1EntityCommand child11Cmd =
            new UpsertTestAuditedChild1EntityCommand(NEW_CHILD_NAME_11)
                .with(TestAuditedChild1EntityType.DESC, NEW_CHILD_DESC_11);
        final UpsertTestAuditedChild1EntityCommand child12Cmd =
            new UpsertTestAuditedChild1EntityCommand(CHILD_NAME_12)
                .with(TestAuditedChild1EntityType.DESC, NEW_CHILD_DESC_12);

        final UpsertTestAuditedEntityCommand parentCmd = new UpsertTestAuditedEntityCommand(PARENT_NAME_1)
            .with(TestAuditedEntityType.DESC, NEW_PARENT_DESC_1)
            .with(child11Cmd)
            .with(child12Cmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(singletonList(parentCmd), flowConfig);

        final long newChildId = fetchChildIdByName(NEW_CHILD_NAME_11);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(newChildId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild1EntityType.DESC,
                                                                               NEW_CHILD_DESC_11))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(TestAuditedChild1EntityType.DESC,
                                                                               CHILD_DESC_12,
                                                                               NEW_CHILD_DESC_12))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_OneNotAuditedChild_AllNew_ShouldGenerateCreateRecordForAuditedChildOnly() {
        final UpsertTestAuditedChild1EntityCommand auditedChildCmd = new UpsertTestAuditedChild1EntityCommand(NEW_CHILD_NAME_11);
        final UpsertTestChildEntityCommand notAuditedChildCmd = new UpsertTestChildEntityCommand(NEW_CHILD_NAME_12);
        final UpsertTestAuditedEntityCommand parentCmd = new UpsertTestAuditedEntityCommand(NEW_PARENT_NAME_1)
            .with(auditedChildCmd)
            .with(notAuditedChildCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestChildEntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(singletonList(parentCmd), flowConfig);

        final long auditedChildId = fetchChildIdByName(NEW_CHILD_NAME_11);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(auditedChildId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11))));
        assertThat(auditRecord, not(hasChildRecordThat(hasEntityType(TestChildEntityType.INSTANCE))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_OneNotAuditedChild_AllChanged_ShouldGenerateUpdateRecordForAuditedChildOnly() {
        final UpsertTestAuditedChild1EntityCommand auditedChildCmd =
            new UpsertTestAuditedChild1EntityCommand(CHILD_NAME_11)
                .with(TestAuditedChild1EntityType.DESC, NEW_CHILD_DESC_11);
        final UpsertTestChildEntityCommand notAuditedChildCmd =
            new UpsertTestChildEntityCommand(CHILD_NAME_12)
                .with(TestChildEntityType.DESC, NEW_CHILD_DESC_12);

        final UpsertTestAuditedEntityCommand parentCmd = new UpsertTestAuditedEntityCommand(PARENT_NAME_1)
            .with(TestAuditedEntityType.DESC, NEW_PARENT_DESC_1)
            .with(auditedChildCmd)
            .with(notAuditedChildCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestChildEntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(singletonList(parentCmd), flowConfig);

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
    public void oneAuditedParent_OneAuditedChild_BothChanged_WithDeletionOfOthers_ShouldCreateDeletionRecordForOtherChild() {
        final UpsertTestAuditedChild1EntityCommand childCmd =
            new UpsertTestAuditedChild1EntityCommand(CHILD_NAME_11)
                .with(TestAuditedChild1EntityType.DESC, NEW_CHILD_DESC_11);

        final UpsertTestAuditedEntityCommand cmd = new UpsertTestAuditedEntityCommand(PARENT_NAME_1)
            .with(childCmd)
            .with(new DeletionOfOther<>(TestAuditedChild1EntityType.INSTANCE));

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void oneChangedAuditedParent_OneNewAuditedChild_ShouldGenerateUpdateForParentAndCreateForChild() {
        final UpsertTestAuditedChild1EntityCommand childCmd =
            new UpsertTestAuditedChild1EntityCommand(NEW_CHILD_NAME_11);

        final UpsertTestAuditedEntityCommand cmd = new UpsertTestAuditedEntityCommand(PARENT_NAME_1)
            .with(TestAuditedEntityType.DESC, NEW_PARENT_DESC_1)
            .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(singletonList(cmd), flowConfig);

        final long childId = fetchChildIdByName(NEW_CHILD_NAME_11);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(TestAuditedEntityType.DESC, PARENT_DESC_1, NEW_PARENT_DESC_1)));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(childId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11))));
    }

    @Test
    public void oneNotAuditedParent_OneAuditedChild_AllNew_ShouldReturnEmpty() {
        final UpsertTestAuditedChild1EntityCommand auditedChildCmd = new UpsertTestAuditedChild1EntityCommand(NEW_CHILD_NAME_11);
        final UpsertTestEntityCommand parentCmd = new UpsertTestEntityCommand(NEW_PARENT_NAME_1)
            .with(auditedChildCmd);

        final ChangeFlowConfig<TestEntityType> flowConfig =
            flowConfigBuilder(TestEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        notAuditedEntityPL.upsert(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneNotAuditedParent_OneAuditedChild_BothChanged_ShouldReturnEmpty() {
        final UpsertTestAuditedChild1EntityCommand childCmd =
            new UpsertTestAuditedChild1EntityCommand(CHILD_NAME_11)
                .with(TestAuditedChild1EntityType.DESC, NEW_CHILD_DESC_11);

        final UpsertTestEntityCommand parentCmd = new UpsertTestEntityCommand(PARENT_NAME_1)
            .with(TestEntityType.DESC, NEW_PARENT_DESC_1)
            .with(childCmd);

        final ChangeFlowConfig<TestEntityType> flowConfig =
            flowConfigBuilder(TestEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        notAuditedEntityPL.upsert(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneAuditedParent_OneNotAuditedChild_BothChanged_ShouldGenerateUpdateRecordForParentOnly() {
        final UpsertTestChildEntityCommand childCmd =
            new UpsertTestChildEntityCommand(CHILD_NAME_11)
                .with(TestChildEntityType.DESC, NEW_CHILD_DESC_11);

        final UpsertTestAuditedEntityCommand parentCmd = new UpsertTestAuditedEntityCommand(PARENT_NAME_1)
            .with(TestAuditedEntityType.DESC, NEW_PARENT_DESC_1)
            .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestChildEntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(singletonList(parentCmd), flowConfig);

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
    public void oneAuditedParentUnchanged_OneAuditedChildChanged_ShouldGenerateUpdateRecordForParentAndChild() {

        final UpsertTestAuditedChild1EntityCommand childCmd =
            new UpsertTestAuditedChild1EntityCommand(CHILD_NAME_11)
                .with(TestAuditedChild1EntityType.DESC, NEW_CHILD_DESC_11);
        final UpsertTestAuditedEntityCommand parentCmd =
            new UpsertTestAuditedEntityCommand(PARENT_NAME_1)
                .with(TestAuditedEntityType.DESC, PARENT_DESC_1)
                .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(singletonList(parentCmd), flowConfig);

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
    public void oneAuditedParentChanged_OneAuditedChildUnchanged_ShouldGenerateUpdateRecordForParentOnly() {

        final UpsertTestAuditedChild1EntityCommand childCmd =
            new UpsertTestAuditedChild1EntityCommand(CHILD_NAME_11)
                .with(TestAuditedChild1EntityType.DESC, CHILD_DESC_11);
        final UpsertTestAuditedEntityCommand parentCmd =
            new UpsertTestAuditedEntityCommand(PARENT_NAME_1)
                .with(TestAuditedEntityType.DESC, NEW_PARENT_DESC_1)
                .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(singletonList(parentCmd), flowConfig);

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
    public void twoAuditedParents_OneAuditedChildEach_AllNew_ShouldCreateChildRecordsForBoth() {
        final UpsertTestAuditedChild1EntityCommand child1Cmd = new UpsertTestAuditedChild1EntityCommand(NEW_CHILD_NAME_11);
        final UpsertTestAuditedChild2EntityCommand child2Cmd = new UpsertTestAuditedChild2EntityCommand(NEW_CHILD_NAME_21);

        final UpsertTestAuditedEntityCommand parent1Cmd = new UpsertTestAuditedEntityCommand(NEW_PARENT_NAME_1)
            .with(child1Cmd);
        final UpsertTestAuditedEntityCommand parent2Cmd = new UpsertTestAuditedEntityCommand(NEW_PARENT_NAME_2)
            .with(child2Cmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild2EntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(ImmutableList.of(parent1Cmd, parent2Cmd), flowConfig);

        final long child1Id = fetchChildIdByName(NEW_CHILD_NAME_11);
        final long child2Id = fetchChildIdByName(NEW_CHILD_NAME_21);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));
        final AuditRecord<TestAuditedEntityType> auditRecord1 = typed(auditRecords.get(0));
        final AuditRecord<TestAuditedEntityType> auditRecord2 = typed(auditRecords.get(1));

        assertThat(auditRecord1, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                          hasEntityId(String.valueOf(child1Id)),
                                                          hasOperator(CREATE),
                                                          hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, NEW_CHILD_NAME_11))));
        assertThat(auditRecord2, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild2EntityType.INSTANCE),
                                                          hasEntityId(String.valueOf(child2Id)),
                                                          hasOperator(CREATE),
                                                          hasCreatedFieldRecord(TestAuditedChild2EntityType.NAME, NEW_CHILD_NAME_21))));
    }

    @Test
    public void twoAuditedParents_OneAuditedChildEach_AllChanged_ShouldGenerateUpdateRecordsForBothChildren() {
        final UpsertTestAuditedChild1EntityCommand child1Cmd =
            new UpsertTestAuditedChild1EntityCommand(CHILD_NAME_11)
                .with(TestAuditedChild1EntityType.DESC, NEW_CHILD_DESC_11);
        final UpsertTestAuditedChild2EntityCommand child2Cmd =
            new UpsertTestAuditedChild2EntityCommand(CHILD_NAME_21)
                .with(TestAuditedChild2EntityType.DESC, NEW_CHILD_DESC_21);

        final List<UpsertTestAuditedEntityCommand> cmds =
            ImmutableList.of(new UpsertTestAuditedEntityCommand(PARENT_NAME_1)
                                 .with(TestAuditedEntityType.DESC, NEW_PARENT_DESC_1)
                                 .with(child1Cmd),
                             new UpsertTestAuditedEntityCommand(PARENT_NAME_2)
                                 .with(TestAuditedEntityType.DESC, NEW_PARENT_DESC_2)
                                 .with(child2Cmd));

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild2EntityType.INSTANCE))
                .build();

        auditedEntityPL.upsert(cmds, flowConfig);

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

    private long fetchNewParent1IdByName() {
        return dslContext.select(parent_table.id)
                         .from(parent_table)
                         .where(parent_table.name.eq(NEW_PARENT_NAME_1))
                         .fetchOptional()
                         .map(rec -> rec.get(parent_table.id))
                         .orElseThrow(() -> new IllegalStateException("Could not fetch parent id by name '" + PARENT_NAME_1 + "'"));
    }

    private long fetchChildIdByName(final String childName) {
        return dslContext.select(child_table.id)
                         .from(child_table)
                         .where(child_table.name.eq(childName))
                         .fetchOptional()
                         .map(rec -> rec.get(child_table.id))
                         .orElseThrow(() -> new IllegalStateException("Could not fetch child id by name '" + childName + "'"));
    }

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> AuditRecord<E> typed(final AuditRecord<?> auditRecord) {
        return (AuditRecord<E>) auditRecord;
    }
}
