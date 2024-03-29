package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.*;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.ChildAutoIncIdTable;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
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
        ImmutableList.of(MainAutoIncIdTable.INSTANCE, ChildAutoIncIdTable.INSTANCE);

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private PersistenceLayer<AuditedAutoIncIdType> auditedParentPL;
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

        dslContext.insertInto(MainAutoIncIdTable.INSTANCE)
                  .columns(MainAutoIncIdTable.INSTANCE.id, MainAutoIncIdTable.INSTANCE.name)
                  .values(PARENT_ID_1, PARENT_NAME_1)
                  .values(PARENT_ID_2, PARENT_NAME_2)
                  .execute();

        dslContext.insertInto(ChildAutoIncIdTable.INSTANCE)
                  .columns(ChildAutoIncIdTable.INSTANCE.id, ChildAutoIncIdTable.INSTANCE.parent_id, ChildAutoIncIdTable.INSTANCE.name)
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
        final UpdateAuditedChild1Command childCmd =
            new UpdateAuditedChild1Command(CHILD_ID_11)
                .with(AuditedAutoIncIdChild1Type.NAME, NEW_CHILD_NAME_11);
        final UpdateAuditedCommand parentCmd =
            new UpdateAuditedCommand(PARENT_ID_1)
                .with(AuditedAutoIncIdType.NAME, NEW_PARENT_NAME_1)
                .with(childCmd);

        final ChangeFlowConfig<AuditedAutoIncIdType> flowConfig =
            flowConfigBuilder(AuditedAutoIncIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild1Type.INSTANCE))
                .build();

        auditedParentPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.NAME, PARENT_NAME_1, NEW_PARENT_NAME_1)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedAutoIncIdChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(AuditedAutoIncIdChild1Type.NAME,
                                                                               CHILD_NAME_11,
                                                                               NEW_CHILD_NAME_11))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildrenSameType_AllChanged_ShouldCreateRecordsForBothChildren() {
        final UpdateAuditedChild1Command child11Cmd =
            new UpdateAuditedChild1Command(CHILD_ID_11)
                .with(AuditedAutoIncIdChild1Type.NAME, NEW_CHILD_NAME_11);
        final UpdateAuditedChild1Command child12Cmd =
            new UpdateAuditedChild1Command(CHILD_ID_12)
                .with(AuditedAutoIncIdChild1Type.NAME, NEW_CHILD_NAME_12);

        final UpdateAuditedCommand parentCmd = new UpdateAuditedCommand(PARENT_ID_1)
            .with(AuditedAutoIncIdType.NAME, NEW_PARENT_NAME_1)
            .with(child11Cmd)
            .with(child12Cmd);

        final ChangeFlowConfig<AuditedAutoIncIdType> flowConfig =
            flowConfigBuilder(AuditedAutoIncIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild1Type.INSTANCE))
                .build();

        auditedParentPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedAutoIncIdChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(AuditedAutoIncIdChild1Type.NAME,
                                                                               CHILD_NAME_11,
                                                                               NEW_CHILD_NAME_11))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedAutoIncIdChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(AuditedAutoIncIdChild1Type.NAME,
                                                                               CHILD_NAME_12,
                                                                               NEW_CHILD_NAME_12))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildrenDifferentTypes_AllChanged_ShouldCreateRecordsForBothChildren() {
        final UpdateAuditedChild1Command child11Cmd =
            new UpdateAuditedChild1Command(CHILD_ID_11)
                .with(AuditedAutoIncIdChild1Type.NAME, NEW_CHILD_NAME_11);
        final UpdateAuditedChild2Command child21Cmd =
            new UpdateAuditedChild2Command(CHILD_ID_12)
                .with(AuditedAutoIncIdChild2Type.NAME, NEW_CHILD_NAME_12);

        final UpdateAuditedCommand parentCmd = new UpdateAuditedCommand(PARENT_ID_1)
            .with(AuditedAutoIncIdType.NAME, NEW_PARENT_NAME_1)
            .with(child11Cmd)
            .with(child21Cmd);

        final ChangeFlowConfig<AuditedAutoIncIdType> flowConfig =
            flowConfigBuilder(AuditedAutoIncIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild2Type.INSTANCE))
                .build();

        auditedParentPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedAutoIncIdChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(AuditedAutoIncIdChild1Type.NAME,
                                                                               CHILD_NAME_11,
                                                                               NEW_CHILD_NAME_11))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedAutoIncIdChild2Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(UPDATE),
                                                         hasChangedFieldRecord(AuditedAutoIncIdChild2Type.NAME,
                                                                               CHILD_NAME_12,
                                                                               NEW_CHILD_NAME_12))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_OneNotAuditedChild_AllChanged_ShouldCreateRecordForAuditedChildOnly() {
        final UpdateAuditedChild1Command auditedChildCmd =
            new UpdateAuditedChild1Command(CHILD_ID_11)
                .with(AuditedAutoIncIdChild1Type.NAME, NEW_CHILD_NAME_11);
        final UpdateNotAuditedChildCommand notAuditedChildCmd =
            new UpdateNotAuditedChildCommand(CHILD_ID_12)
                .with(NotAuditedChildType.NAME, NEW_CHILD_NAME_12);

        final UpdateAuditedCommand parentCmd = new UpdateAuditedCommand(PARENT_ID_1)
            .with(AuditedAutoIncIdType.NAME, NEW_PARENT_NAME_1)
            .with(auditedChildCmd)
            .with(notAuditedChildCmd);

        final ChangeFlowConfig<AuditedAutoIncIdType> flowConfig =
            flowConfigBuilder(AuditedAutoIncIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(NotAuditedChildType.INSTANCE))
                .build();

        auditedParentPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedAutoIncIdChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE))));
        assertThat(auditRecord, not(hasChildRecordThat(hasEntityType(NotAuditedChildType.INSTANCE.getName()))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_WithDeletionOfOthers_ShouldCreateDeletionRecordForOtherChild() {
        final UpdateAuditedChild1Command childCmd =
            new UpdateAuditedChild1Command(CHILD_ID_11)
                .with(AuditedAutoIncIdChild1Type.NAME, NEW_CHILD_NAME_11);

        final UpdateAuditedCommand cmd = new UpdateAuditedCommand(PARENT_ID_1)
            .with(childCmd)
            .with(new DeletionOfOther<>(AuditedAutoIncIdChild1Type.INSTANCE));

        final ChangeFlowConfig<AuditedAutoIncIdType> flowConfig =
            flowConfigBuilder(AuditedAutoIncIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild1Type.INSTANCE))
                .build();

        auditedParentPL.update(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedAutoIncIdChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void oneAuditedParentThatExists_OneAuditedChildThatDoesntExist_ShouldReturnEmpty() {
        final UpdateAuditedChild1Command childCmd =
            new UpdateAuditedChild1Command(INVALID_ID)
                .with(AuditedAutoIncIdChild1Type.NAME, NEW_CHILD_NAME_11);

        final UpdateAuditedCommand cmd = new UpdateAuditedCommand(PARENT_ID_1)
            .with(AuditedAutoIncIdType.NAME, NEW_PARENT_NAME_1)
            .with(childCmd);

        final ChangeFlowConfig<AuditedAutoIncIdType> flowConfig =
            flowConfigBuilder(AuditedAutoIncIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild1Type.INSTANCE))
                .build();

        auditedParentPL.update(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneNotAuditedParent_OneAuditedChild_BothChanged_ShouldReturnEmpty() {
        final UpdateAuditedChild1Command childCmd =
            new UpdateAuditedChild1Command(CHILD_ID_11)
                .with(AuditedAutoIncIdChild1Type.NAME, NEW_CHILD_NAME_11);

        final UpdateNotAuditedCommand parentCmd = new UpdateNotAuditedCommand(PARENT_ID_1)
            .with(NotAuditedType.NAME, NEW_PARENT_NAME_1)
            .with(childCmd);

        final ChangeFlowConfig<NotAuditedType> flowConfig =
            flowConfigBuilder(NotAuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild1Type.INSTANCE))
                .build();

        notAuditedParentPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneAuditedParent_OneNotAuditedChild_BothChanged_ShouldCreateRecordForParentOnly() {
        final UpdateNotAuditedChildCommand childCmd =
            new UpdateNotAuditedChildCommand(CHILD_ID_11)
                .with(NotAuditedChildType.NAME, NEW_CHILD_NAME_11);

        final UpdateAuditedCommand parentCmd = new UpdateAuditedCommand(PARENT_ID_1)
            .with(AuditedAutoIncIdType.NAME, NEW_PARENT_NAME_1)
            .with(childCmd);

        final ChangeFlowConfig<AuditedAutoIncIdType> flowConfig =
            flowConfigBuilder(AuditedAutoIncIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(NotAuditedChildType.INSTANCE))
                .build();

        auditedParentPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE),
                                      not(hasChildRecordThat(hasEntityType(NotAuditedChildType.INSTANCE.getName())))));
    }

    @Test
    public void oneAuditedParentWithoutDataFields_OneAuditedChild_ShouldCreateRecordForParentAndChild() {

        final UpdateAuditedChild1Command childCmd =
            new UpdateAuditedChild1Command(CHILD_ID_11)
                .with(AuditedAutoIncIdChild1Type.NAME, NEW_CHILD_NAME_11);
        final UpdateAuditedWithoutDataFieldsCommand parentCmd =
            new UpdateAuditedWithoutDataFieldsCommand(PARENT_ID_1)
                .with(childCmd);

        final ChangeFlowConfig<AuditedWithoutDataFieldsType> flowConfig =
            flowConfigBuilder(AuditedWithoutDataFieldsType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild1Type.INSTANCE))
                .build();

        auditedParentWithoutDataFieldsPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(AuditedWithoutDataFieldsType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedAutoIncIdChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE))));
    }

    @Test
    public void oneAuditedParentUnchanged_OneAuditedChildChanged_ShouldCreateRecordForParentAndChild() {

        final UpdateAuditedChild1Command childCmd =
            new UpdateAuditedChild1Command(CHILD_ID_11)
                .with(AuditedAutoIncIdChild1Type.NAME, NEW_CHILD_NAME_11);
        final UpdateAuditedCommand parentCmd =
            new UpdateAuditedCommand(PARENT_ID_1)
                .with(AuditedAutoIncIdType.NAME, PARENT_NAME_1)
                .with(childCmd);

        final ChangeFlowConfig<AuditedAutoIncIdType> flowConfig =
            flowConfigBuilder(AuditedAutoIncIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild1Type.INSTANCE))
                .build();

        auditedParentPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedAutoIncIdChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(UPDATE))));
    }

    @Test
    public void oneAuditedParentChanged_OneAuditedChildUnchanged_ShouldCreateRecordForParentOnly() {

        final UpdateAuditedChild1Command childCmd =
            new UpdateAuditedChild1Command(CHILD_ID_11)
                .with(AuditedAutoIncIdChild1Type.NAME, CHILD_NAME_11);
        final UpdateAuditedCommand parentCmd =
            new UpdateAuditedCommand(PARENT_ID_1)
                .with(AuditedAutoIncIdType.NAME, NEW_PARENT_NAME_1)
                .with(childCmd);

        final ChangeFlowConfig<AuditedAutoIncIdType> flowConfig =
            flowConfigBuilder(AuditedAutoIncIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild1Type.INSTANCE))
                .build();

        auditedParentPL.update(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(UPDATE),
                                      not(hasChildRecordThat(hasEntityType(AuditedAutoIncIdChild1Type.INSTANCE.getName())))));
    }

    @Test
    public void twoAuditedParents_OneAuditedChildEach_AllChanged_ShouldCreateRecordsForBothChildren() {
        final UpdateAuditedChild1Command child1Cmd =
            new UpdateAuditedChild1Command(CHILD_ID_11)
                .with(AuditedAutoIncIdChild1Type.NAME, NEW_CHILD_NAME_11);
        final UpdateAuditedChild2Command child2Cmd =
            new UpdateAuditedChild2Command(CHILD_ID_21)
                .with(AuditedAutoIncIdChild2Type.NAME, NEW_CHILD_NAME_21);

        final List<UpdateAuditedCommand> cmds =
            ImmutableList.of(new UpdateAuditedCommand(PARENT_ID_1)
                                 .with(AuditedAutoIncIdType.NAME, NEW_PARENT_NAME_1)
                                 .with(child1Cmd),
                             new UpdateAuditedCommand(PARENT_ID_2)
                                 .with(AuditedAutoIncIdType.NAME, NEW_PARENT_NAME_2)
                                 .with(child2Cmd));

        final ChangeFlowConfig<AuditedAutoIncIdType> flowConfig =
            flowConfigBuilder(AuditedAutoIncIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(AuditedAutoIncIdChild2Type.INSTANCE))
                .build();

        auditedParentPL.update(cmds, flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord auditRecord1 = auditRecords.get(0);
        assertThat(auditRecord1, hasChildRecordThat(allOf(hasEntityType(AuditedAutoIncIdChild1Type.INSTANCE.getName()),
                                                          hasEntityId(String.valueOf(CHILD_ID_11)),
                                                          hasOperator(UPDATE))));

        final AuditRecord auditRecord2 = auditRecords.get(1);
        assertThat(auditRecord2, hasChildRecordThat(allOf(hasEntityType(AuditedAutoIncIdChild2Type.INSTANCE.getName()),
                                                          hasEntityId(String.valueOf(CHILD_ID_21)),
                                                          hasOperator(UPDATE))));
    }

    private <E extends EntityType<E>> ChangeFlowConfig.Builder<E> flowConfigBuilder(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType);
    }


    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }
}
