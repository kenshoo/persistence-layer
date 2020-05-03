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
                  .values(PARENT_ID_1, "parentName1")
                  .values(PARENT_ID_2, "parentName2")
                  .execute();

        dslContext.insertInto(TestChildEntityTable.INSTANCE)
                  .columns(TestChildEntityTable.INSTANCE.id, TestChildEntityTable.INSTANCE.parent_id, TestChildEntityTable.INSTANCE.name)
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
        final DeleteTestAuditedEntityCommand cmd =
            new DeleteTestAuditedEntityCommand(PARENT_ID_1)
                .with(new DeleteTestAuditedChild1EntityCommand(CHILD_ID_11));

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(DELETE)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildrenSameType_AllExist_ShouldCreateRecordsForBothChildren() {
        final DeleteTestAuditedEntityCommand cmd = new DeleteTestAuditedEntityCommand(PARENT_ID_1)
            .with(TestAuditedEntityType.NAME, "name")
            .with(new DeleteTestAuditedChild1EntityCommand(CHILD_ID_11))
            .with(new DeleteTestAuditedChild1EntityCommand(CHILD_ID_12));

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(DELETE))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildrenDifferentTypes_AllExist_ShouldCreateRecordsForBothChildren() {
        final DeleteTestAuditedEntityCommand parentCmd = new DeleteTestAuditedEntityCommand(PARENT_ID_1)
            .with(new DeleteTestAuditedChild1EntityCommand(CHILD_ID_11))
            .with(new DeleteTestAuditedChild2EntityCommand(CHILD_ID_12));

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild2EntityType.INSTANCE))
                .build();

        auditedEntityPL.delete(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(DELETE))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild2EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_OneNotAuditedChild_AllExist_ShouldCreateRecordForAuditedChildOnly() {
        final DeleteTestAuditedEntityCommand cmd = new DeleteTestAuditedEntityCommand(PARENT_ID_1)
            .with(new DeleteTestAuditedChild1EntityCommand(CHILD_ID_11))
            .with(new DeleteTestEntityCommand(CHILD_ID_12));

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestChildEntityType.INSTANCE))
                .build();

        auditedEntityPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(DELETE))));
        assertThat(auditRecord, not(hasChildRecordThat(hasEntityType(TestChildEntityType.INSTANCE))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_BothExist_WithDeletionOfOthers_ShouldCreateRecordsForBothChildren() {
        final DeleteTestAuditedEntityCommand cmd = new DeleteTestAuditedEntityCommand(PARENT_ID_1)
            .with(new DeleteTestAuditedChild1EntityCommand(CHILD_ID_11))
            .with(new DeletionOfOther<>(TestAuditedChild1EntityType.INSTANCE));

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(DELETE))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_12)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void oneAuditedParentThatExists_OneAuditedChildThatDoesntExist_ShouldReturnEmpty() {
        final DeleteTestAuditedEntityCommand cmd =
            new DeleteTestAuditedEntityCommand(PARENT_ID_1)
                .with(new DeleteTestAuditedChild1EntityCommand(INVALID_ID));

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneNotAuditedParent_OneAuditedChild_BothExist_ShouldReturnEmpty() {
        final DeleteTestEntityCommand cmd =
            new DeleteTestEntityCommand(PARENT_ID_1)
                .with(new DeleteTestAuditedChild1EntityCommand(CHILD_ID_11));

        final ChangeFlowConfig<TestEntityType> flowConfig =
            flowConfigBuilder(TestEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        notAuditedEntityPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneAuditedParentWithoutDataFields_OneAuditedChild_ShouldCreateRecordForParentAndChild() {
        final DeleteTestAuditedEntityWithoutDataFieldsCommand cmd =
            new DeleteTestAuditedEntityWithoutDataFieldsCommand(PARENT_ID_1)
                .with(new DeleteTestAuditedChild1EntityCommand(CHILD_ID_11));

        final ChangeFlowConfig<TestAuditedEntityWithoutDataFieldsType> flowConfig =
            flowConfigBuilder(TestAuditedEntityWithoutDataFieldsType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityWithoutDataFieldsPL.delete(singletonList(cmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityWithoutDataFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityWithoutDataFieldsType.INSTANCE),
                                      hasEntityId(String.valueOf(PARENT_ID_1)),
                                      hasOperator(DELETE)));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(CHILD_ID_11)),
                                                         hasOperator(DELETE))));
    }

    @Test
    public void twoAuditedParents_OneAuditedChildEach_AllExist_ShouldCreateRecordsForBothChildren() {
        final List<DeleteTestAuditedEntityCommand> cmds =
            ImmutableList.of(new DeleteTestAuditedEntityCommand(PARENT_ID_1)
                                 .with(new DeleteTestAuditedChild1EntityCommand(CHILD_ID_11)),
                             new DeleteTestAuditedEntityCommand(PARENT_ID_2)
                                 .with(new DeleteTestAuditedChild2EntityCommand(CHILD_ID_21)));

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild2EntityType.INSTANCE))
                .build();

        auditedEntityPL.delete(cmds, flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord<TestAuditedEntityType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                          hasEntityId(String.valueOf(CHILD_ID_11)),
                                                          hasOperator(DELETE))));

        final AuditRecord<TestAuditedEntityType> auditRecord2 = typed(auditRecords.get(1));
        assertThat(auditRecord2, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild2EntityType.INSTANCE),
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
