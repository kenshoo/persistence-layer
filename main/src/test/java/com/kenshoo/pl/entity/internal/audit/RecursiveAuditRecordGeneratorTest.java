package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RecursiveAuditRecordGeneratorTest {

    private final RecursiveAuditRecordGenerator recursiveAuditRecordGenerator = new RecursiveAuditRecordGenerator();

    @Mock
    private ChangeFlowConfig<AuditedType> flowConfig;
    @Mock
    private ChangeFlowConfig<TestEntity> notAuditedFlowConfig;
    @Mock
    private ChangeFlowConfig<TestChild1EntityType> child1FlowConfig;
    @Mock
    private ChangeFlowConfig<TestChild2EntityType> child2FlowConfig;
    @Mock
    private ChangeFlowConfig<TestGrandchildEntityType> grandChildFlowConfig;

    @Mock
    private ChangeContext changeContext;

    @Mock
    private AuditRecordGeneratorStateConsumerImpl<AuditedType> auditRecordGenerator;
    @Mock
    private AuditRecordGeneratorStateConsumerImpl<TestChild1EntityType> child1AuditRecordGenerator;
    @Mock
    private AuditRecordGeneratorStateConsumerImpl<TestChild2EntityType> child2AuditRecordGenerator;
    @Mock
    private AuditRecordGeneratorStateConsumerImpl<TestGrandchildEntityType> grandchildAuditRecordGenerator;

    @Test
    public void generateMany_OneAuditedEntity_WithChanges_ShouldGenerateRecord() {
        final ChangeEntityCommand<AuditedType> cmd = mockCommand();
        final AuditRecord<AuditedType> auditRecord = mockAuditRecord();
        final CurrentEntityState currentState = mock(CurrentEntityState.class);

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));
        when(changeContext.getEntity(cmd)).thenReturn(currentState);
        doReturn(Optional.of(auditRecord)).when(auditRecordGenerator).generate(cmd, currentState, emptyList());

        final Stream<? extends AuditRecord<AuditedType>> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(auditRecord)));
    }

    @Test
    public void generateMany_OneAuditedEntity_WithoutChanges_ShouldReturnEmpty() {
        final ChangeEntityCommand<AuditedType> cmd = mockCommand();
        final CurrentEntityState currentState = mock(CurrentEntityState.class);

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));
        when(changeContext.getEntity(cmd)).thenReturn(currentState);
        doReturn(Optional.empty()).when(auditRecordGenerator).generate(cmd, currentState, emptyList());

        final Stream<? extends AuditRecord<AuditedType>> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), is(empty()));
    }

    @Test
    public void generateMany_OneNotAuditedEntity_ShouldReturnEmpty() {
        final ChangeEntityCommand<AuditedType> cmd = mockCommand();

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.empty());

        final Stream<? extends AuditRecord<AuditedType>> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), is(empty()));
    }

    @Test
    public void generateMany_TwoAuditedEntities_BothWithChanges_ShouldGenerateTwoRecords() {
        final ChangeEntityCommand<AuditedType> cmd1 = mockCommand();
        final ChangeEntityCommand<AuditedType> cmd2 = mockCommand();

        final CurrentEntityState entity1 = mock(CurrentEntityState.class);
        final CurrentEntityState entity2 = mock(CurrentEntityState.class);

        final AuditRecord<AuditedType> auditRecord1 = mockAuditRecord();
        final AuditRecord<AuditedType> auditRecord2 = mockAuditRecord();

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));

        when(changeContext.getEntity(cmd1)).thenReturn(entity1);
        when(changeContext.getEntity(cmd2)).thenReturn(entity2);

        doReturn(Optional.of(auditRecord1)).when(auditRecordGenerator).generate(cmd1, entity1, emptyList());
        doReturn(Optional.of(auditRecord2)).when(auditRecordGenerator).generate(cmd2, entity2, emptyList());

        final Set<AuditRecord<AuditedType>> expectedAuditRecords =
            ImmutableSet.of(auditRecord1, auditRecord2);

        final Stream<? extends AuditRecord<AuditedType>> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd1, cmd2), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(expectedAuditRecords));
    }

    @Test
    public void generateMany_TwoAuditedEntities_OnlyOneWithChanges_ShouldGenerateOneRecord() {
        final ChangeEntityCommand<AuditedType> cmd1 = mockCommand();
        final ChangeEntityCommand<AuditedType> cmd2 = mockCommand();

        final CurrentEntityState entity1 = mock(CurrentEntityState.class);
        final CurrentEntityState entity2 = mock(CurrentEntityState.class);

        final AuditRecord<AuditedType> auditRecord1 = mockAuditRecord();

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));

        when(changeContext.getEntity(cmd1)).thenReturn(entity1);
        when(changeContext.getEntity(cmd2)).thenReturn(entity2);

        doReturn(Optional.of(auditRecord1)).when(auditRecordGenerator).generate(cmd1, entity1, emptyList());
        doReturn(Optional.empty()).when(auditRecordGenerator).generate(cmd2, entity2, emptyList());

        final Stream<? extends AuditRecord<AuditedType>> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd1, cmd2), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(auditRecord1)));
    }

    @Test
    public void generateMany_AuditedParent_TwoAuditedChildrenSameType_AllChanged_ShouldGenerateParentRecordWithTwoChildren() {
        final ChangeEntityCommand<AuditedType> cmd = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd1A = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd1B = mockCommand();

        final AuditRecord<AuditedType> expectedAuditRecord = mockAuditRecord();
        final AuditRecord<TestChild1EntityType> childAuditRecord1A = mockAuditRecord();
        final AuditRecord<TestChild1EntityType> childAuditRecord1B = mockAuditRecord();
        final List<AuditRecord<TestChild1EntityType>> childAuditRecords = ImmutableList.of(childAuditRecord1A, childAuditRecord1B);

        final CurrentEntityState currentState = mock(CurrentEntityState.class);
        final CurrentEntityState childEntity1A = mock(CurrentEntityState.class);
        final CurrentEntityState childEntity1B = mock(CurrentEntityState.class);

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));
        when(flowConfig.childFlows()).thenReturn(singletonList(child1FlowConfig));

        when(child1FlowConfig.getEntityType()).thenReturn(TestChild1EntityType.INSTANCE);
        when(child1FlowConfig.auditRecordGenerator()).thenReturn(Optional.of(child1AuditRecordGenerator));
        when(child1FlowConfig.childFlows()).thenReturn(emptyList());

        when(cmd.getChildren(TestChild1EntityType.INSTANCE)).thenReturn(Stream.of(childCmd1A, childCmd1B));

        when(changeContext.getEntity(cmd)).thenReturn(currentState);
        when(changeContext.getEntity(childCmd1A)).thenReturn(childEntity1A);
        when(changeContext.getEntity(childCmd1B)).thenReturn(childEntity1B);

        doReturn(Optional.of(childAuditRecord1A)).when(child1AuditRecordGenerator).generate(childCmd1A, childEntity1A, emptyList());
        doReturn(Optional.of(childAuditRecord1B)).when(child1AuditRecordGenerator).generate(childCmd1B, childEntity1B, emptyList());
        doReturn(Optional.of(expectedAuditRecord)).when(auditRecordGenerator).generate(cmd, currentState, childAuditRecords);

        final Stream<? extends AuditRecord<AuditedType>> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(expectedAuditRecord)));
    }


    @Test
    public void generateMany_AuditedParent_TwoAuditedChildrenSameType_OnlyOneChanged_ShouldGenerateParentRecordWithOneChild() {
        final ChangeEntityCommand<AuditedType> cmd = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd1A = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd1B = mockCommand();

        final AuditRecord<AuditedType> expectedAuditRecord = mockAuditRecord();
        final AuditRecord<TestChild1EntityType> childAuditRecord1A = mockAuditRecord();

        final CurrentEntityState currentState = mock(CurrentEntityState.class);
        final CurrentEntityState childEntity1A = mock(CurrentEntityState.class);
        final CurrentEntityState childEntity1B = mock(CurrentEntityState.class);

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));
        when(flowConfig.childFlows()).thenReturn(singletonList(child1FlowConfig));

        when(child1FlowConfig.getEntityType()).thenReturn(TestChild1EntityType.INSTANCE);
        when(child1FlowConfig.auditRecordGenerator()).thenReturn(Optional.of(child1AuditRecordGenerator));
        when(child1FlowConfig.childFlows()).thenReturn(emptyList());

        when(cmd.getChildren(TestChild1EntityType.INSTANCE)).thenReturn(Stream.of(childCmd1A, childCmd1B));

        when(changeContext.getEntity(cmd)).thenReturn(currentState);
        when(changeContext.getEntity(childCmd1A)).thenReturn(childEntity1A);
        when(changeContext.getEntity(childCmd1B)).thenReturn(childEntity1B);

        doReturn(Optional.of(childAuditRecord1A)).when(child1AuditRecordGenerator).generate(childCmd1A, childEntity1A, emptyList());
        doReturn(Optional.empty()).when(child1AuditRecordGenerator).generate(childCmd1B, childEntity1B, emptyList());
        doReturn(Optional.of(expectedAuditRecord)).when(auditRecordGenerator).generate(cmd, currentState, singletonList(childAuditRecord1A));

        final Stream<? extends AuditRecord<AuditedType>> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(expectedAuditRecord)));
    }


    @Test
    public void generateMany_AuditedParent_OneAuditedChildAndOneNot_ShouldGenerateParentRecordWithOneChild() {
        final ChangeEntityCommand<AuditedType> cmd = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> auditedChildCmd = mockCommand();
        final ChangeEntityCommand<TestChild2EntityType> notAuditedChildCmd = mockCommand();

        final AuditRecord<AuditedType> expectedAuditRecord = mockAuditRecord();
        final AuditRecord<TestChild1EntityType> auditedChildRecord = mockAuditRecord();

        final CurrentEntityState currentState = mock(CurrentEntityState.class);
        final CurrentEntityState auditedChildEntity = mock(CurrentEntityState.class);

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));
        when(flowConfig.childFlows()).thenReturn(ImmutableList.of(child1FlowConfig, child2FlowConfig));

        when(child1FlowConfig.getEntityType()).thenReturn(TestChild1EntityType.INSTANCE);
        when(child1FlowConfig.auditRecordGenerator()).thenReturn(Optional.of(child1AuditRecordGenerator));
        when(child1FlowConfig.childFlows()).thenReturn(emptyList());

        when(child2FlowConfig.getEntityType()).thenReturn(TestChild2EntityType.INSTANCE);
        when(child2FlowConfig.auditRecordGenerator()).thenReturn(Optional.empty());

        when(cmd.getChildren(TestChild1EntityType.INSTANCE)).thenReturn(Stream.of(auditedChildCmd));
        when(cmd.getChildren(TestChild2EntityType.INSTANCE)).thenReturn(Stream.of(notAuditedChildCmd));

        when(changeContext.getEntity(cmd)).thenReturn(currentState);
        when(changeContext.getEntity(auditedChildCmd)).thenReturn(auditedChildEntity);

        doReturn(Optional.of(auditedChildRecord)).when(child1AuditRecordGenerator).generate(auditedChildCmd, auditedChildEntity, emptyList());
        doReturn(Optional.of(expectedAuditRecord)).when(auditRecordGenerator).generate(cmd, currentState, singletonList(auditedChildRecord));

        final Stream<? extends AuditRecord<AuditedType>> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(expectedAuditRecord)));
    }

    @Test
    public void generateMany_AuditedParent_TwoAuditedChildrenEachOfTwoTypes_ShouldGenerateParentRecordWithFourChildren() {
        final ChangeEntityCommand<AuditedType> cmd = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd1A = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd1B = mockCommand();
        final ChangeEntityCommand<TestChild2EntityType> childCmd2A = mockCommand();
        final ChangeEntityCommand<TestChild2EntityType> childCmd2B = mockCommand();

        final AuditRecord<AuditedType> expectedAuditRecord = mockAuditRecord();
        final AuditRecord<TestChild1EntityType> childAuditRecord1A = mockAuditRecord();
        final AuditRecord<TestChild1EntityType> childAuditRecord1B = mockAuditRecord();
        final AuditRecord<TestChild2EntityType> childAuditRecord2A = mockAuditRecord();
        final AuditRecord<TestChild2EntityType> childAuditRecord2B = mockAuditRecord();
        final List<AuditRecord<?>> allChildAuditRecords = ImmutableList.of(childAuditRecord1A,
                                                                           childAuditRecord1B,
                                                                           childAuditRecord2A,
                                                                           childAuditRecord2B);

        final CurrentEntityState currentState = mock(CurrentEntityState.class);
        final CurrentEntityState childEntity1A = mock(CurrentEntityState.class);
        final CurrentEntityState childEntity1B = mock(CurrentEntityState.class);
        final CurrentEntityState childEntity2A = mock(CurrentEntityState.class);
        final CurrentEntityState childEntity2B = mock(CurrentEntityState.class);

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));
        when(flowConfig.childFlows()).thenReturn(ImmutableList.of(child1FlowConfig, child2FlowConfig));

        when(child1FlowConfig.getEntityType()).thenReturn(TestChild1EntityType.INSTANCE);
        when(child1FlowConfig.auditRecordGenerator()).thenReturn(Optional.of(child1AuditRecordGenerator));
        when(child1FlowConfig.childFlows()).thenReturn(emptyList());

        when(child2FlowConfig.getEntityType()).thenReturn(TestChild2EntityType.INSTANCE);
        when(child2FlowConfig.auditRecordGenerator()).thenReturn(Optional.of(child2AuditRecordGenerator));
        when(child2FlowConfig.childFlows()).thenReturn(emptyList());

        when(cmd.getChildren(TestChild1EntityType.INSTANCE)).thenReturn(Stream.of(childCmd1A, childCmd1B));
        when(cmd.getChildren(TestChild2EntityType.INSTANCE)).thenReturn(Stream.of(childCmd2A, childCmd2B));

        when(changeContext.getEntity(cmd)).thenReturn(currentState);
        when(changeContext.getEntity(childCmd1A)).thenReturn(childEntity1A);
        when(changeContext.getEntity(childCmd1B)).thenReturn(childEntity1B);
        when(changeContext.getEntity(childCmd2A)).thenReturn(childEntity2A);
        when(changeContext.getEntity(childCmd2B)).thenReturn(childEntity2B);

        doReturn(Optional.of(childAuditRecord1A)).when(child1AuditRecordGenerator).generate(childCmd1A, childEntity1A, emptyList());
        doReturn(Optional.of(childAuditRecord1B)).when(child1AuditRecordGenerator).generate(childCmd1B, childEntity1B, emptyList());
        doReturn(Optional.of(childAuditRecord2A)).when(child2AuditRecordGenerator).generate(childCmd2A, childEntity2A, emptyList());
        doReturn(Optional.of(childAuditRecord2B)).when(child2AuditRecordGenerator).generate(childCmd2B, childEntity2B, emptyList());
        doReturn(Optional.of(expectedAuditRecord)).when(auditRecordGenerator).generate(cmd, currentState, allChildAuditRecords);

        final Stream<? extends AuditRecord<AuditedType>> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(expectedAuditRecord)));
    }


    @Test
    public void generateMany_ThreeAuditedLevels_OneEntityEach_ShouldGenerateThreeLevelRecord() {
        final ChangeEntityCommand<AuditedType> cmd = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd = mockCommand();
        final ChangeEntityCommand<TestGrandchildEntityType> grandchildCmd = mockCommand();

        final AuditRecord<AuditedType> expectedAuditRecord = mockAuditRecord();
        final AuditRecord<TestChild1EntityType> childAuditRecord = mockAuditRecord();
        final AuditRecord<TestGrandchildEntityType> grandchildAuditRecord = mockAuditRecord();

        final CurrentEntityState currentState = mock(CurrentEntityState.class);
        final CurrentEntityState childEntity = mock(CurrentEntityState.class);
        final CurrentEntityState grandchildEntity = mock(CurrentEntityState.class);

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));
        when(flowConfig.childFlows()).thenReturn(singletonList(child1FlowConfig));

        when(child1FlowConfig.getEntityType()).thenReturn(TestChild1EntityType.INSTANCE);
        when(child1FlowConfig.auditRecordGenerator()).thenReturn(Optional.of(child1AuditRecordGenerator));
        when(child1FlowConfig.childFlows()).thenReturn(singletonList(grandChildFlowConfig));

        when(grandChildFlowConfig.getEntityType()).thenReturn(TestGrandchildEntityType.INSTANCE);
        when(grandChildFlowConfig.auditRecordGenerator()).thenReturn(Optional.of(grandchildAuditRecordGenerator));
        when(grandChildFlowConfig.childFlows()).thenReturn(singletonList(grandChildFlowConfig));

        when(cmd.getChildren(TestChild1EntityType.INSTANCE)).thenReturn(Stream.of(childCmd));
        when(childCmd.getChildren(TestGrandchildEntityType.INSTANCE)).thenReturn(Stream.of(grandchildCmd));

        when(changeContext.getEntity(cmd)).thenReturn(currentState);
        when(changeContext.getEntity(childCmd)).thenReturn(childEntity);
        when(changeContext.getEntity(grandchildCmd)).thenReturn(grandchildEntity);

        doReturn(Optional.of(grandchildAuditRecord))
            .when(grandchildAuditRecordGenerator).generate(grandchildCmd, grandchildEntity, emptyList());
        doReturn(Optional.of(childAuditRecord))
            .when(child1AuditRecordGenerator).generate(childCmd, childEntity, singletonList(grandchildAuditRecord));
        doReturn(Optional.of(expectedAuditRecord))
            .when(auditRecordGenerator).generate(cmd, currentState, singletonList(childAuditRecord));

        final Stream<? extends AuditRecord<AuditedType>> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(expectedAuditRecord)));
    }

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> ChangeEntityCommand<E> mockCommand() {
        return mock(ChangeEntityCommand.class);
    }

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> AuditRecord<E> mockAuditRecord() {
        return mock(AuditRecord.class);
    }

    public static class TestChild1EntityType extends AbstractEntityType<TestChild1EntityType> {

        public static final TestChild1EntityType INSTANCE = new TestChild1EntityType();

        @Override
        public DataTable getPrimaryTable() {
            return null;
        }

        private TestChild1EntityType() {
            super("TestChild1Entity");
        }
    }

    public static class TestChild2EntityType extends AbstractEntityType<TestChild2EntityType> {

        public static final TestChild2EntityType INSTANCE = new TestChild2EntityType();

        @Override
        public DataTable getPrimaryTable() {
            return null;
        }

        private TestChild2EntityType() {
            super("TestChild2Entity");
        }
    }

    public static class TestGrandchildEntityType extends AbstractEntityType<TestGrandchildEntityType> {

        public static final TestGrandchildEntityType INSTANCE = new TestGrandchildEntityType();

        @Override
        public DataTable getPrimaryTable() {
            return null;
        }

        private TestGrandchildEntityType() {
            super("TestGrandchildEntity");
        }
    }
}