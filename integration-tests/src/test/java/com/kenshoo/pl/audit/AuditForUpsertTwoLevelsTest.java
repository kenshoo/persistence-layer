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

    private static final MainTable parent_table = MainTable.INSTANCE;
    private static final ChildTable child_table = ChildTable.INSTANCE;

    private static final List<? extends DataTable> ALL_TABLES = ImmutableList.of(parent_table, child_table);

    private PLContext plContext;
    private DSLContext dslContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private PersistenceLayer<AuditedType> auditedParentPL;
    private PersistenceLayer<NotAuditedType> notAuditedParentPL;

    @Before
    public void setUp() {
        dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedParentPL = persistenceLayer();
        notAuditedParentPL = persistenceLayer();

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
        final UpsertAuditedChild1Command childCmd = new UpsertAuditedChild1Command(NEW_CHILD_NAME_11);
        final UpsertAuditedCommand parentCmd = new UpsertAuditedCommand(NEW_PARENT_NAME_1)
            .with(childCmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.upsert(singletonList(parentCmd), flowConfig);

        final long parentId = fetchNewParent1IdByName();
        final long childId = fetchChildIdByName(NEW_CHILD_NAME_11);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(String.valueOf(parentId)),
                                      hasOperator(CREATE),
                                      hasCreatedFieldRecord(AuditedType.NAME, NEW_PARENT_NAME_1)));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(childId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild1Type.NAME, NEW_CHILD_NAME_11))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_BothChanged_ShouldGenerateUpdateRecordsForBoth() {
        final UpsertAuditedChild1Command childCmd =
            new UpsertAuditedChild1Command(CHILD_NAME_11)
                .with(AuditedChild1Type.DESC, NEW_CHILD_DESC_11);
        final UpsertAuditedCommand parentCmd =
            new UpsertAuditedCommand(PARENT_NAME_1)
                .with(AuditedType.DESC, NEW_PARENT_DESC_1)
                .with(childCmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.upsert(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedType.DESC, PARENT_DESC_1, NEW_PARENT_DESC_1)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(AuditedChild1Type.DESC,
                                                                               CHILD_DESC_11,
                                                                               NEW_CHILD_DESC_11))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildren_AllNew_ShouldGenerateCreateRecordsForBothChildren() {
        final UpsertAuditedChild1Command child11Cmd = new UpsertAuditedChild1Command(NEW_CHILD_NAME_11);
        final UpsertAuditedChild1Command child12Cmd = new UpsertAuditedChild1Command(NEW_CHILD_NAME_12);
        final UpsertAuditedCommand parentCmd = new UpsertAuditedCommand(NEW_PARENT_NAME_1)
            .with(child11Cmd)
            .with(child12Cmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.upsert(singletonList(parentCmd), flowConfig);

        final long child11Id = fetchChildIdByName(NEW_CHILD_NAME_11);
        final long child12Id = fetchChildIdByName(NEW_CHILD_NAME_12);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(child11Id)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild1Type.NAME, NEW_CHILD_NAME_11))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(child12Id)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild1Type.NAME, NEW_CHILD_NAME_12))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildren_AllChanged_ShouldGenerateUpdateRecordsForBothChildren() {
        final UpsertAuditedChild1Command child11Cmd =
            new UpsertAuditedChild1Command(CHILD_NAME_11)
                .with(AuditedChild1Type.DESC, NEW_CHILD_DESC_11);
        final UpsertAuditedChild1Command child12Cmd =
            new UpsertAuditedChild1Command(CHILD_NAME_12)
                .with(AuditedChild1Type.DESC, NEW_CHILD_DESC_12);

        final UpsertAuditedCommand parentCmd = new UpsertAuditedCommand(PARENT_NAME_1)
            .with(AuditedType.DESC, NEW_PARENT_DESC_1)
            .with(child11Cmd)
            .with(child12Cmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.upsert(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(AuditedChild1Type.DESC,
                                                                               CHILD_DESC_11,
                                                                               NEW_CHILD_DESC_11))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(AuditedChild1Type.DESC,
                                                                               CHILD_DESC_12,
                                                                               NEW_CHILD_DESC_12))));
    }

    @Test
    public void oneChangedAuditedParent_OneNewAuditedChild_OneChangedAuditedChild_ShouldGenerateCreateAndUpdateRecordsForChildren() {
        final UpsertAuditedChild1Command child11Cmd =
            new UpsertAuditedChild1Command(NEW_CHILD_NAME_11)
                .with(AuditedChild1Type.DESC, NEW_CHILD_DESC_11);
        final UpsertAuditedChild1Command child12Cmd =
            new UpsertAuditedChild1Command(CHILD_NAME_12)
                .with(AuditedChild1Type.DESC, NEW_CHILD_DESC_12);

        final UpsertAuditedCommand parentCmd = new UpsertAuditedCommand(PARENT_NAME_1)
            .with(AuditedType.DESC, NEW_PARENT_DESC_1)
            .with(child11Cmd)
            .with(child12Cmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.upsert(singletonList(parentCmd), flowConfig);

        final long newChildId = fetchChildIdByName(NEW_CHILD_NAME_11);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(newChildId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild1Type.DESC,
                                                                               NEW_CHILD_DESC_11))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(AuditedChild1Type.DESC,
                                                                               CHILD_DESC_12,
                                                                               NEW_CHILD_DESC_12))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_OneNotAuditedChild_AllNew_ShouldGenerateCreateRecordForAuditedChildOnly() {
        final UpsertAuditedChild1Command auditedChildCmd = new UpsertAuditedChild1Command(NEW_CHILD_NAME_11);
        final UpsertNotAuditedChildCommand notAuditedChildCmd = new UpsertNotAuditedChildCommand(NEW_CHILD_NAME_12);
        final UpsertAuditedCommand parentCmd = new UpsertAuditedCommand(NEW_PARENT_NAME_1)
            .with(auditedChildCmd)
            .with(notAuditedChildCmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(NotAuditedChildType.INSTANCE))
                .build();

        auditedParentPL.upsert(singletonList(parentCmd), flowConfig);

        final long auditedChildId = fetchChildIdByName(NEW_CHILD_NAME_11);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(auditedChildId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild1Type.NAME, NEW_CHILD_NAME_11))));
        assertThat(auditRecord, not(hasChildRecordThat(hasEntityType(NotAuditedChildType.INSTANCE))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_OneNotAuditedChild_AllChanged_ShouldGenerateUpdateRecordForAuditedChildOnly() {
        final UpsertAuditedChild1Command auditedChildCmd =
            new UpsertAuditedChild1Command(CHILD_NAME_11)
                .with(AuditedChild1Type.DESC, NEW_CHILD_DESC_11);
        final UpsertNotAuditedChildCommand notAuditedChildCmd =
            new UpsertNotAuditedChildCommand(CHILD_NAME_12)
                .with(NotAuditedChildType.DESC, NEW_CHILD_DESC_12);

        final UpsertAuditedCommand parentCmd = new UpsertAuditedCommand(PARENT_NAME_1)
            .with(AuditedType.DESC, NEW_PARENT_DESC_1)
            .with(auditedChildCmd)
            .with(notAuditedChildCmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(NotAuditedChildType.INSTANCE))
                .build();

        auditedParentPL.upsert(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE))));
        assertThat(auditRecord, not(hasChildRecordThat(hasEntityType(NotAuditedChildType.INSTANCE))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_BothChanged_WithDeletionOfOthers_ShouldCreateDeletionRecordForOtherChild() {
        final UpsertAuditedChild1Command childCmd =
            new UpsertAuditedChild1Command(CHILD_NAME_11)
                .with(AuditedChild1Type.DESC, NEW_CHILD_DESC_11);

        final UpsertAuditedCommand cmd = new UpsertAuditedCommand(PARENT_NAME_1)
            .with(childCmd)
            .with(new DeletionOfOther<>(AuditedChild1Type.INSTANCE));

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.upsert(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void oneChangedAuditedParent_OneNewAuditedChild_ShouldGenerateUpdateForParentAndCreateForChild() {
        final UpsertAuditedChild1Command childCmd =
            new UpsertAuditedChild1Command(NEW_CHILD_NAME_11);

        final UpsertAuditedCommand cmd = new UpsertAuditedCommand(PARENT_NAME_1)
            .with(AuditedType.DESC, NEW_PARENT_DESC_1)
            .with(childCmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.upsert(singletonList(cmd), flowConfig);

        final long childId = fetchChildIdByName(NEW_CHILD_NAME_11);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedType.DESC, PARENT_DESC_1, NEW_PARENT_DESC_1)));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(childId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild1Type.NAME, NEW_CHILD_NAME_11))));
    }

    @Test
    public void oneNotAuditedParent_OneAuditedChild_AllNew_ShouldReturnEmpty() {
        final UpsertAuditedChild1Command auditedChildCmd = new UpsertAuditedChild1Command(NEW_CHILD_NAME_11);
        final UpsertNotAuditedCommand parentCmd = new UpsertNotAuditedCommand(NEW_PARENT_NAME_1)
            .with(auditedChildCmd);

        final ChangeFlowConfig<NotAuditedType> flowConfig =
            flowConfigBuilder(NotAuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        notAuditedParentPL.upsert(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneNotAuditedParent_OneAuditedChild_BothChanged_ShouldReturnEmpty() {
        final UpsertAuditedChild1Command childCmd =
            new UpsertAuditedChild1Command(CHILD_NAME_11)
                .with(AuditedChild1Type.DESC, NEW_CHILD_DESC_11);

        final UpsertNotAuditedCommand parentCmd = new UpsertNotAuditedCommand(PARENT_NAME_1)
            .with(NotAuditedType.DESC, NEW_PARENT_DESC_1)
            .with(childCmd);

        final ChangeFlowConfig<NotAuditedType> flowConfig =
            flowConfigBuilder(NotAuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        notAuditedParentPL.upsert(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneAuditedParent_OneNotAuditedChild_BothChanged_ShouldGenerateUpdateRecordForParentOnly() {
        final UpsertNotAuditedChildCommand childCmd =
            new UpsertNotAuditedChildCommand(CHILD_NAME_11)
                .with(NotAuditedChildType.DESC, NEW_CHILD_DESC_11);

        final UpsertAuditedCommand parentCmd = new UpsertAuditedCommand(PARENT_NAME_1)
            .with(AuditedType.DESC, NEW_PARENT_DESC_1)
            .with(childCmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(NotAuditedChildType.INSTANCE))
                .build();

        auditedParentPL.upsert(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE),
                                      not(hasChildRecordThat(hasEntityType(NotAuditedChildType.INSTANCE)))));
    }

    @Test
    public void oneAuditedParentUnchanged_OneAuditedChildChanged_ShouldGenerateUpdateRecordForParentAndChild() {

        final UpsertAuditedChild1Command childCmd =
            new UpsertAuditedChild1Command(CHILD_NAME_11)
                .with(AuditedChild1Type.DESC, NEW_CHILD_DESC_11);
        final UpsertAuditedCommand parentCmd =
            new UpsertAuditedCommand(PARENT_NAME_1)
                .with(AuditedType.DESC, PARENT_DESC_1)
                .with(childCmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.upsert(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE))));
    }

    @Test
    public void oneAuditedParentChanged_OneAuditedChildUnchanged_ShouldGenerateUpdateRecordForParentOnly() {

        final UpsertAuditedChild1Command childCmd =
            new UpsertAuditedChild1Command(CHILD_NAME_11)
                .with(AuditedChild1Type.DESC, CHILD_DESC_11);
        final UpsertAuditedCommand parentCmd =
            new UpsertAuditedCommand(PARENT_NAME_1)
                .with(AuditedType.DESC, NEW_PARENT_DESC_1)
                .with(childCmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.upsert(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE),
                                      not(hasChildRecordThat(hasEntityType(AuditedChild1Type.INSTANCE)))));
    }

    @Test
    public void twoAuditedParents_OneAuditedChildEach_AllNew_ShouldCreateChildRecordsForBoth() {
        final UpsertAuditedChild1Command child1Cmd = new UpsertAuditedChild1Command(NEW_CHILD_NAME_11);
        final UpsertAuditedChild2Command child2Cmd = new UpsertAuditedChild2Command(NEW_CHILD_NAME_21);

        final UpsertAuditedCommand parent1Cmd = new UpsertAuditedCommand(NEW_PARENT_NAME_1)
            .with(child1Cmd);
        final UpsertAuditedCommand parent2Cmd = new UpsertAuditedCommand(NEW_PARENT_NAME_2)
            .with(child2Cmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild2Type.INSTANCE))
                .build();

        auditedParentPL.upsert(ImmutableList.of(parent1Cmd, parent2Cmd), flowConfig);

        final long child1Id = fetchChildIdByName(NEW_CHILD_NAME_11);
        final long child2Id = fetchChildIdByName(NEW_CHILD_NAME_21);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));
        final AuditRecord<AuditedType> auditRecord1 = typed(auditRecords.get(0));
        final AuditRecord<AuditedType> auditRecord2 = typed(auditRecords.get(1));

        assertThat(auditRecord1, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                          hasEntityId(String.valueOf(child1Id)),
                                                          hasOperator(CREATE),
                                                          hasCreatedFieldRecord(AuditedChild1Type.NAME, NEW_CHILD_NAME_11))));
        assertThat(auditRecord2, hasChildRecordThat(allOf(hasEntityType(AuditedChild2Type.INSTANCE),
                                                          hasEntityId(String.valueOf(child2Id)),
                                                          hasOperator(CREATE),
                                                          hasCreatedFieldRecord(AuditedChild2Type.NAME, NEW_CHILD_NAME_21))));
    }

    @Test
    public void twoAuditedParents_OneAuditedChildEach_AllChanged_ShouldGenerateUpdateRecordsForBothChildren() {
        final UpsertAuditedChild1Command child1Cmd =
            new UpsertAuditedChild1Command(CHILD_NAME_11)
                .with(AuditedChild1Type.DESC, NEW_CHILD_DESC_11);
        final UpsertAuditedChild2Command child2Cmd =
            new UpsertAuditedChild2Command(CHILD_NAME_21)
                .with(AuditedChild2Type.DESC, NEW_CHILD_DESC_21);

        final List<UpsertAuditedCommand> cmds =
            ImmutableList.of(new UpsertAuditedCommand(PARENT_NAME_1)
                                 .with(AuditedType.DESC, NEW_PARENT_DESC_1)
                                 .with(child1Cmd),
                             new UpsertAuditedCommand(PARENT_NAME_2)
                                 .with(AuditedType.DESC, NEW_PARENT_DESC_2)
                                 .with(child2Cmd));

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild2Type.INSTANCE))
                .build();

        auditedParentPL.upsert(cmds, flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord<AuditedType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE),
                                                          hasEntityId(String.valueOf(CHILD_ID_11)),
                                                          hasOperator(UPDATE))));

        final AuditRecord<AuditedType> auditRecord2 = typed(auditRecords.get(1));
        assertThat(auditRecord2, hasChildRecordThat(allOf(hasEntityType(AuditedChild2Type.INSTANCE),
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
