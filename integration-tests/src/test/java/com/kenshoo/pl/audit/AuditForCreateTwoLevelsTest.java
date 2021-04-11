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
        ImmutableList.of(MainTable.INSTANCE, ChildTable.INSTANCE);

    private PLContext plContext;
    private DSLContext dslContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private PersistenceLayer<AuditedType> auditedParentPL;
    private PersistenceLayer<AuditedWithoutDataFieldsType> auditedParentWithoutDataFieldsPL;
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
        auditedParentWithoutDataFieldsPL = persistenceLayer();
        notAuditedParentPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_ShouldCreateRecordsForParentAndChild() {
        final CreateAuditedChild1Command childCmd = new CreateAuditedChild1Command()
            .with(AuditedChild1Type.NAME, "childName");
        final CreateAuditedCommand parentCmd = new CreateAuditedCommand()
            .with(AuditedType.NAME, "name")
            .with(childCmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        final CreateResult<AuditedType, Identifier<AuditedType>> createResult =
            auditedParentPL.create(singletonList(parentCmd), flowConfig);

        final CreateEntityCommand<AuditedType> outputParentCmd = extractFirstCmdFromResult(createResult);
        final long parentId = extractAuditedParentIdFromCmd(outputParentCmd);
        final long childId = fetchChildIdByName("childName");

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(parentId)),
                                      hasOperator(CREATE),
                                      hasCreatedFieldRecord(AuditedType.NAME, "name")));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(childId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild1Type.NAME, "childName"))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildrenSameType_ShouldCreateRecordsForBothChildren() {
        final CreateAuditedChild1Command child1ACmd = new CreateAuditedChild1Command()
            .with(AuditedChild1Type.NAME, "child1AName");
        final CreateAuditedChild1Command child1BCmd = new CreateAuditedChild1Command()
            .with(AuditedChild1Type.NAME, "child1BName");
        final CreateAuditedCommand parentCmd = new CreateAuditedCommand()
            .with(AuditedType.NAME, "name")
            .with(child1ACmd)
            .with(child1BCmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentPL.create(singletonList(parentCmd), flowConfig);

        final long child1AId = fetchChildIdByName("child1AName");
        final long child1BId = fetchChildIdByName("child1BName");

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(child1AId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild1Type.NAME, "child1AName"))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(child1BId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild1Type.NAME, "child1BName"))));
    }

    @Test
    public void oneAuditedParent_TwoAuditedChildrenDifferentTypes_ShouldCreateRecordsForBothChildren() {
        final CreateAuditedChild1Command child1Cmd = new CreateAuditedChild1Command()
            .with(AuditedChild1Type.NAME, "child1Name");
        final CreateAuditedChild2Command child2Cmd = new CreateAuditedChild2Command()
            .with(AuditedChild2Type.NAME, "child2Name");
        final CreateAuditedCommand parentCmd = new CreateAuditedCommand()
            .with(AuditedType.NAME, "name")
            .with(child1Cmd)
            .with(child2Cmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild2Type.INSTANCE))
                .build();

        auditedParentPL.create(singletonList(parentCmd), flowConfig);

        final long child1Id = fetchChildIdByName("child1Name");
        final long child2Id = fetchChildIdByName("child2Name");

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(child1Id)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild1Type.NAME, "child1Name"))));
        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild2Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(child2Id)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild2Type.NAME, "child2Name"))));
    }

    @Test
    public void oneAuditedParent_OneAuditedChild_OneNotAuditedChild_ShouldCreateRecordForAuditedChildOnly() {
        final CreateAuditedChild1Command auditedChildCmd = new CreateAuditedChild1Command()
            .with(AuditedChild1Type.NAME, "auditedChildName");
        final CreateNotAuditedChildCommand notAuditedChildCmd = new CreateNotAuditedChildCommand()
            .with(NotAuditedChildType.NAME, "notAuditedChildName");
        final CreateAuditedCommand parentCmd = new CreateAuditedCommand()
            .with(AuditedType.NAME, "name")
            .with(auditedChildCmd)
            .with(notAuditedChildCmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(NotAuditedChildType.INSTANCE))
                .build();

        auditedParentPL.create(singletonList(parentCmd), flowConfig);

        final long auditedChildId = fetchChildIdByName("auditedChildName");

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(auditedChildId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild1Type.NAME, "auditedChildName"))));
        assertThat(auditRecord, not(hasChildRecordThat(hasEntityType(NotAuditedChildType.INSTANCE.getName()))));
    }

    @Test
    public void oneNotAuditedParent_OneAuditedChild_ShouldReturnEmpty() {
        final CreateAuditedChild1Command auditedChildCmd = new CreateAuditedChild1Command()
            .with(AuditedChild1Type.NAME, "auditedChildName");
        final CreateNotAuditedCommand parentCmd = new CreateNotAuditedCommand()
            .with(NotAuditedType.NAME, "notAuditedParentName")
            .with(auditedChildCmd);

        final ChangeFlowConfig<NotAuditedType> flowConfig =
            flowConfigBuilder(NotAuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        notAuditedParentPL.create(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneAuditedParentWithoutDataFields_OneAuditedChild_ShouldCreateRecordsForParentAndChild() {
        final long parentId = 11L;

        final CreateAuditedChild1Command childCmd = new CreateAuditedChild1Command()
            .with(AuditedChild1Type.NAME, "childName");
        final CreateAuditedWithoutDataFieldsCommand parentCmd = new CreateAuditedWithoutDataFieldsCommand(parentId)
            .with(childCmd);

        final ChangeFlowConfig<AuditedWithoutDataFieldsType> flowConfig =
            flowConfigBuilder(AuditedWithoutDataFieldsType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .build();

        auditedParentWithoutDataFieldsPL.create(singletonList(parentCmd), flowConfig);

        final long childId = fetchChildIdByName("childName");

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, allOf(hasEntityType(AuditedWithoutDataFieldsType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(parentId)),
                                      hasOperator(CREATE)));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                         hasEntityId(String.valueOf(childId)),
                                                         hasOperator(CREATE),
                                                         hasCreatedFieldRecord(AuditedChild1Type.NAME, "childName"))));
    }

    @Test
    public void twoAuditedParents_OneAuditedChildEach_ShouldCreateChildRecordsForBoth() {
        final CreateAuditedChild1Command child1Cmd = new CreateAuditedChild1Command()
            .with(AuditedChild1Type.NAME, "child1Name");
        final CreateAuditedChild2Command child2Cmd = new CreateAuditedChild2Command()
            .with(AuditedChild2Type.NAME, "child2Name");

        final CreateAuditedCommand parent1Cmd = new CreateAuditedCommand()
            .with(AuditedType.NAME, "parent1Name")
            .with(child1Cmd);
        final CreateAuditedCommand parent2Cmd = new CreateAuditedCommand()
            .with(AuditedType.NAME, "parent2Name")
            .with(child2Cmd);

        final ChangeFlowConfig<AuditedType> flowConfig =
            flowConfigBuilder(AuditedType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild1Type.INSTANCE))
                .withChildFlowBuilder(flowConfigBuilder(AuditedChild2Type.INSTANCE))
                .build();

        auditedParentPL.create(ImmutableList.of(parent1Cmd, parent2Cmd), flowConfig);

        final long child1Id = fetchChildIdByName("child1Name");
        final long child2Id = fetchChildIdByName("child2Name");

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));
        final AuditRecord auditRecord1 = auditRecords.get(0);
        final AuditRecord auditRecord2 = auditRecords.get(1);

        assertThat(auditRecord1, hasChildRecordThat(allOf(hasEntityType(AuditedChild1Type.INSTANCE.getName()),
                                                          hasEntityId(String.valueOf(child1Id)),
                                                          hasOperator(CREATE),
                                                          hasCreatedFieldRecord(AuditedChild1Type.NAME, "child1Name"))));
        assertThat(auditRecord2, hasChildRecordThat(allOf(hasEntityType(AuditedChild2Type.INSTANCE.getName()),
                                                          hasEntityId(String.valueOf(child2Id)),
                                                          hasOperator(CREATE),
                                                          hasCreatedFieldRecord(AuditedChild2Type.NAME, "child2Name"))));
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

    private long extractAuditedParentIdFromCmd(final ChangeEntityCommand<AuditedType> cmd) {
        return Optional.ofNullable(cmd.getIdentifier())
                       .map(identifier -> identifier.get(AuditedType.ID))
                       .orElseThrow(() -> new IllegalStateException("Could not find the audited parent id in the creation result"));
    }

    private long fetchChildIdByName(final String childName) {
        return dslContext.select(ChildTable.INSTANCE.id)
                         .from(ChildTable.INSTANCE)
                         .where(ChildTable.INSTANCE.name.eq(childName))
                         .fetchOptional()
                         .map(rec -> rec.get(ChildTable.INSTANCE.id))
                         .orElseThrow(() -> new IllegalStateException("Could not fetch id by name '" + childName + "'"));
    }
}
