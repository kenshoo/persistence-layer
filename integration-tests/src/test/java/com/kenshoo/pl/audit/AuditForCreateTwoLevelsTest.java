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
import java.util.Optional;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AuditForCreateTwoLevelsTest {

    private static final List<? extends DataTable> ALL_TABLES =
        ImmutableList.of(TestEntityTable.INSTANCE, TestChildEntityTable.INSTANCE);

    private PLContext plContext;
    private DSLContext dslContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private PersistenceLayer<TestAuditedEntityType> auditedEntityPL;
    private PersistenceLayer<TestAuditedEntityWithoutDataFieldsType> auditedEntityWithoutDataFieldsPL;
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
        auditedEntityWithoutDataFieldsPL = persistenceLayer();
        notAuditedEntityPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_ShouldCreateRecordsForParentAndChild() {
        final CreateTestAuditedChild1EntityCommand childCmd = new CreateTestAuditedChild1EntityCommand()
            .with(TestAuditedChild1EntityType.NAME, "childName");
        final CreateTestAuditedEntityCommand parentCmd = new CreateTestAuditedEntityCommand()
            .with(TestAuditedEntityType.NAME, "name")
            .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        final CreateResult<TestAuditedEntityType, Identifier<TestAuditedEntityType>> createResult =
            auditedEntityPL.create(singletonList(parentCmd), flowConfig);

        final CreateEntityCommand<TestAuditedEntityType> outputParentCmd = extractFirstCmdFromResult(createResult);
        final long parentId = extractAuditedParentIdFromCmd(outputParentCmd);
        final long childId = fetchChildIdByName("childName");

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(parentId)),
                                      hasOperator(CREATE),
                                      hasCreatedFieldRecord(TestAuditedEntityType.NAME, "name")));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(childId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, "childName"))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildrenSameType_ShouldCreateRecordsForBothChildren() {
        final CreateTestAuditedChild1EntityCommand child1ACmd = new CreateTestAuditedChild1EntityCommand()
            .with(TestAuditedChild1EntityType.NAME, "child1AName");
        final CreateTestAuditedChild1EntityCommand child1BCmd = new CreateTestAuditedChild1EntityCommand()
            .with(TestAuditedChild1EntityType.NAME, "child1BName");
        final CreateTestAuditedEntityCommand parentCmd = new CreateTestAuditedEntityCommand()
            .with(TestAuditedEntityType.NAME, "name")
            .with(child1ACmd)
            .with(child1BCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityPL.create(singletonList(parentCmd), flowConfig);

        final long child1AId = fetchChildIdByName("child1AName");
        final long child1BId = fetchChildIdByName("child1BName");

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(child1AId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, "child1AName"))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(child1BId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, "child1BName"))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildrenDifferentTypes_ShouldCreateRecordsForBothChildren() {
        final CreateTestAuditedChild1EntityCommand child1Cmd = new CreateTestAuditedChild1EntityCommand()
            .with(TestAuditedChild1EntityType.NAME, "child1Name");
        final CreateTestAuditedChild2EntityCommand child2Cmd = new CreateTestAuditedChild2EntityCommand()
            .with(TestAuditedChild2EntityType.NAME, "child2Name");
        final CreateTestAuditedEntityCommand parentCmd = new CreateTestAuditedEntityCommand()
            .with(TestAuditedEntityType.NAME, "name")
            .with(child1Cmd)
            .with(child2Cmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild2EntityType.INSTANCE))
                .build();

        auditedEntityPL.create(singletonList(parentCmd), flowConfig);

        final long child1Id = fetchChildIdByName("child1Name");
        final long child2Id = fetchChildIdByName("child2Name");

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(child1Id)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, "child1Name"))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild2EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(child2Id)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild2EntityType.NAME, "child2Name"))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_OneNotAuditedChild_ShouldCreateRecordForAuditedChildOnly() {
        final CreateTestAuditedChild1EntityCommand auditedChildCmd = new CreateTestAuditedChild1EntityCommand()
            .with(TestAuditedChild1EntityType.NAME, "auditedChildName");
        final CreateTestChildEntityCommand notAuditedChildCmd = new CreateTestChildEntityCommand()
            .with(TestChildEntityType.NAME, "notAuditedChildName");
        final CreateTestAuditedEntityCommand parentCmd = new CreateTestAuditedEntityCommand()
            .with(TestAuditedEntityType.NAME, "name")
            .with(auditedChildCmd)
            .with(notAuditedChildCmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestChildEntityType.INSTANCE))
                .build();

        auditedEntityPL.create(singletonList(parentCmd), flowConfig);

        final long auditedChildId = fetchChildIdByName("auditedChildName");

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(auditedChildId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, "auditedChildName"))));
        assertThat(auditRecord, not(hasChildRecordThat(hasEntityType(TestChildEntityType.INSTANCE))));
    }

    @Test
    public void oneNotAuditedParent_OneAuditedChild_ShouldReturnEmpty() {
        final CreateTestAuditedChild1EntityCommand auditedChildCmd = new CreateTestAuditedChild1EntityCommand()
            .with(TestAuditedChild1EntityType.NAME, "auditedChildName");
        final CreateTestEntityCommand parentCmd = new CreateTestEntityCommand()
            .with(TestEntityType.NAME, "notAuditedParentName")
            .with(auditedChildCmd);

        final ChangeFlowConfig<TestEntityType> flowConfig =
            flowConfigBuilder(TestEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        notAuditedEntityPL.create(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneAuditedParentWithoutDataFields_OneAuditedChild_ShouldCreateRecordsForParentAndChild() {
        final long parentId = 11L;

        final CreateTestAuditedChild1EntityCommand childCmd = new CreateTestAuditedChild1EntityCommand()
            .with(TestAuditedChild1EntityType.NAME, "childName");
        final CreateTestAuditedEntityWithoutDataFieldsCommand parentCmd = new CreateTestAuditedEntityWithoutDataFieldsCommand(parentId)
            .with(childCmd);

        final ChangeFlowConfig<TestAuditedEntityWithoutDataFieldsType> flowConfig =
            flowConfigBuilder(TestAuditedEntityWithoutDataFieldsType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .build();

        auditedEntityWithoutDataFieldsPL.create(singletonList(parentCmd), flowConfig);

        final long childId = fetchChildIdByName("childName");

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityWithoutDataFieldsType> auditRecord = typed(auditRecords.get(0));

        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityWithoutDataFieldsType.INSTANCE),
                                      hasEntityId(String.valueOf(parentId)),
                                      hasOperator(CREATE)));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                         hasEntityId(String.valueOf(childId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, "childName"))));
    }

    @Test
    public void twoAuditedParents_OneAuditedChildEach_ShouldCreateChildRecordsForBoth() {
        final CreateTestAuditedChild1EntityCommand child1Cmd = new CreateTestAuditedChild1EntityCommand()
            .with(TestAuditedChild1EntityType.NAME, "child1Name");
        final CreateTestAuditedChild2EntityCommand child2Cmd = new CreateTestAuditedChild2EntityCommand()
            .with(TestAuditedChild2EntityType.NAME, "child2Name");

        final CreateTestAuditedEntityCommand parent1Cmd = new CreateTestAuditedEntityCommand()
            .with(TestAuditedEntityType.NAME, "parent1Name")
            .with(child1Cmd);
        final CreateTestAuditedEntityCommand parent2Cmd = new CreateTestAuditedEntityCommand()
            .with(TestAuditedEntityType.NAME, "parent2Name")
            .with(child2Cmd);

        final ChangeFlowConfig<TestAuditedEntityType> flowConfig =
            flowConfigBuilder(TestAuditedEntityType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild1EntityType.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(TestAuditedChild2EntityType.INSTANCE))
                .build();

        auditedEntityPL.create(ImmutableList.of(parent1Cmd, parent2Cmd), flowConfig);

        final long child1Id = fetchChildIdByName("child1Name");
        final long child2Id = fetchChildIdByName("child2Name");

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));
        final AuditRecord<TestAuditedEntityType> auditRecord1 = typed(auditRecords.get(0));
        final AuditRecord<TestAuditedEntityType> auditRecord2 = typed(auditRecords.get(1));

        assertThat(auditRecord1, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild1EntityType.INSTANCE),
                                                          hasEntityId(String.valueOf(child1Id)),
                                                          hasOperator(CREATE),
                                                          hasCreatedFieldRecord(TestAuditedChild1EntityType.NAME, "child1Name"))));
        assertThat(auditRecord2, hasChildRecordThat(allOf(hasEntityType(TestAuditedChild2EntityType.INSTANCE),
                                                          hasEntityId(String.valueOf(child2Id)),
                                                          hasOperator(CREATE),
                                                          hasCreatedFieldRecord(TestAuditedChild2EntityType.NAME, "child2Name"))));
    }

    private <E extends EntityType<E>> ChangeFlowConfig.Builder<E> flowConfigBuilder(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType);
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }

    private <E extends EntityType<E>> CreateEntityCommand<E> extractFirstCmdFromResult(final CreateResult<E, Identifier<E>> createResult) {
        return createResult.getChangeResults().stream()
                           .map(EntityChangeResult::getCommand)
                           .findFirst()
                           .orElseThrow(() -> new IllegalStateException("An empty collection of results was returned by the PL"));
    }

    private long extractAuditedParentIdFromCmd(final ChangeEntityCommand<TestAuditedEntityType> cmd) {
        return Optional.ofNullable(cmd.getIdentifier())
                       .map(identifier -> identifier.get(TestAuditedEntityType.ID))
                       .orElseThrow(() -> new IllegalStateException("Could not find the audited parent id in the creation result"));
    }

    private long fetchChildIdByName(final String childName) {
        return dslContext.select(TestChildEntityTable.INSTANCE.id)
                         .from(TestChildEntityTable.INSTANCE)
                         .where(TestChildEntityTable.INSTANCE.name.eq(childName))
                         .fetchOptional()
                         .map(rec -> rec.get(TestChildEntityTable.INSTANCE.id))
                         .orElseThrow(() -> new IllegalStateException("Could not fetch id by name '" + childName + "'"));
    }

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> AuditRecord<E> typed(final AuditRecord<?> auditRecord) {
        return (AuditRecord<E>) auditRecord;
    }
}
