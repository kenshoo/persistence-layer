package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;
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
    private ChangeFlowConfig<AuditedAutoIncIdType> flowConfig;
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
    private AuditRecordGenerator<AuditedAutoIncIdType> auditRecordGenerator;
    @Mock
    private AuditRecordGenerator<TestChild1EntityType> child1AuditRecordGenerator;
    @Mock
    private AuditRecordGenerator<TestChild2EntityType> child2AuditRecordGenerator;
    @Mock
    private AuditRecordGenerator<TestGrandchildEntityType> grandchildAuditRecordGenerator;

    @Test
    public void generateMany_OneAuditedEntity_WithChanges_ShouldGenerateRecord() {
        final ChangeEntityCommand<AuditedAutoIncIdType> cmd = mockCommand();
        final AuditRecord auditRecord = mockAuditRecord();

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));
        doReturn(Optional.of(auditRecord)).when(auditRecordGenerator).generate(cmd, changeContext, emptyList());

        final Stream<? extends AuditRecord> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(auditRecord)));
    }

    @Test
    public void generateMany_OneAuditedEntity_WithoutChanges_ShouldReturnEmpty() {
        final ChangeEntityCommand<AuditedAutoIncIdType> cmd = mockCommand();

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));
        doReturn(Optional.empty()).when(auditRecordGenerator).generate(cmd, changeContext, emptyList());

        final Stream<? extends AuditRecord> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), is(empty()));
    }

    @Test
    public void generateMany_OneNotAuditedEntity_ShouldReturnEmpty() {
        final ChangeEntityCommand<AuditedAutoIncIdType> cmd = mockCommand();

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.empty());

        final Stream<? extends AuditRecord> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), is(empty()));
    }

    @Test
    public void generateMany_TwoAuditedEntities_BothWithChanges_ShouldGenerateTwoRecords() {
        final ChangeEntityCommand<AuditedAutoIncIdType> cmd1 = mockCommand();
        final ChangeEntityCommand<AuditedAutoIncIdType> cmd2 = mockCommand();

        final AuditRecord auditRecord1 = mockAuditRecord();
        final AuditRecord auditRecord2 = mockAuditRecord();

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));

        doReturn(Optional.of(auditRecord1)).when(auditRecordGenerator).generate(cmd1, changeContext, emptyList());
        doReturn(Optional.of(auditRecord2)).when(auditRecordGenerator).generate(cmd2, changeContext, emptyList());

        final Set<AuditRecord> expectedAuditRecords =
            ImmutableSet.of(auditRecord1, auditRecord2);

        final Stream<? extends AuditRecord> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd1, cmd2), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(expectedAuditRecords));
    }

    @Test
    public void generateMany_TwoAuditedEntities_OnlyOneWithChanges_ShouldGenerateOneRecord() {
        final ChangeEntityCommand<AuditedAutoIncIdType> cmd1 = mockCommand();
        final ChangeEntityCommand<AuditedAutoIncIdType> cmd2 = mockCommand();

        final AuditRecord auditRecord1 = mockAuditRecord();

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));

        doReturn(Optional.of(auditRecord1)).when(auditRecordGenerator).generate(cmd1, changeContext, emptyList());
        doReturn(Optional.empty()).when(auditRecordGenerator).generate(cmd2, changeContext, emptyList());

        final Stream<? extends AuditRecord> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd1, cmd2), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(auditRecord1)));
    }

    @Test
    public void generateMany_AuditedParent_TwoAuditedChildrenSameType_AllChanged_ShouldGenerateParentRecordWithTwoChildren() {
        final ChangeEntityCommand<AuditedAutoIncIdType> cmd = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd1A = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd1B = mockCommand();

        final AuditRecord expectedAuditRecord = mockAuditRecord();
        final AuditRecord childAuditRecord1A = mockAuditRecord();
        final AuditRecord childAuditRecord1B = mockAuditRecord();
        final List<AuditRecord> childAuditRecords = ImmutableList.of(childAuditRecord1A, childAuditRecord1B);

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));
        when(flowConfig.childFlows()).thenReturn(singletonList(child1FlowConfig));

        when(child1FlowConfig.getEntityType()).thenReturn(TestChild1EntityType.INSTANCE);
        when(child1FlowConfig.auditRecordGenerator()).thenReturn(Optional.of(child1AuditRecordGenerator));
        when(child1FlowConfig.childFlows()).thenReturn(emptyList());

        when(cmd.getChildren(TestChild1EntityType.INSTANCE)).thenReturn(Stream.of(childCmd1A, childCmd1B));

        doReturn(Optional.of(childAuditRecord1A)).when(child1AuditRecordGenerator).generate(childCmd1A, changeContext, emptyList());
        doReturn(Optional.of(childAuditRecord1B)).when(child1AuditRecordGenerator).generate(childCmd1B, changeContext, emptyList());
        doReturn(Optional.of(expectedAuditRecord)).when(auditRecordGenerator).generate(cmd, changeContext, childAuditRecords);

        final Stream<? extends AuditRecord> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(expectedAuditRecord)));
    }


    @Test
    public void generateMany_AuditedParent_TwoAuditedChildrenSameType_OnlyOneChanged_ShouldGenerateParentRecordWithOneChild() {
        final ChangeEntityCommand<AuditedAutoIncIdType> cmd = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd1A = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd1B = mockCommand();

        final AuditRecord expectedAuditRecord = mockAuditRecord();
        final AuditRecord childAuditRecord1A = mockAuditRecord();

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));
        when(flowConfig.childFlows()).thenReturn(singletonList(child1FlowConfig));

        when(child1FlowConfig.getEntityType()).thenReturn(TestChild1EntityType.INSTANCE);
        when(child1FlowConfig.auditRecordGenerator()).thenReturn(Optional.of(child1AuditRecordGenerator));
        when(child1FlowConfig.childFlows()).thenReturn(emptyList());

        when(cmd.getChildren(TestChild1EntityType.INSTANCE)).thenReturn(Stream.of(childCmd1A, childCmd1B));

        doReturn(Optional.of(childAuditRecord1A)).when(child1AuditRecordGenerator).generate(childCmd1A, changeContext, emptyList());
        doReturn(Optional.empty()).when(child1AuditRecordGenerator).generate(childCmd1B, changeContext, emptyList());
        doReturn(Optional.of(expectedAuditRecord)).when(auditRecordGenerator).generate(cmd, changeContext, singletonList(childAuditRecord1A));

        final Stream<? extends AuditRecord> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(expectedAuditRecord)));
    }


    @Test
    public void generateMany_AuditedParent_OneAuditedChildAndOneNot_ShouldGenerateParentRecordWithOneChild() {
        final ChangeEntityCommand<AuditedAutoIncIdType> cmd = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> auditedChildCmd = mockCommand();
        final ChangeEntityCommand<TestChild2EntityType> notAuditedChildCmd = mockCommand();

        final AuditRecord expectedAuditRecord = mockAuditRecord();
        final AuditRecord auditedChildRecord = mockAuditRecord();

        when(flowConfig.auditRecordGenerator()).thenReturn(Optional.of(auditRecordGenerator));
        when(flowConfig.childFlows()).thenReturn(ImmutableList.of(child1FlowConfig, child2FlowConfig));

        when(child1FlowConfig.getEntityType()).thenReturn(TestChild1EntityType.INSTANCE);
        when(child1FlowConfig.auditRecordGenerator()).thenReturn(Optional.of(child1AuditRecordGenerator));
        when(child1FlowConfig.childFlows()).thenReturn(emptyList());

        when(child2FlowConfig.getEntityType()).thenReturn(TestChild2EntityType.INSTANCE);
        when(child2FlowConfig.auditRecordGenerator()).thenReturn(Optional.empty());

        when(cmd.getChildren(TestChild1EntityType.INSTANCE)).thenReturn(Stream.of(auditedChildCmd));
        when(cmd.getChildren(TestChild2EntityType.INSTANCE)).thenReturn(Stream.of(notAuditedChildCmd));

        doReturn(Optional.of(auditedChildRecord)).when(child1AuditRecordGenerator).generate(auditedChildCmd, changeContext, emptyList());
        doReturn(Optional.of(expectedAuditRecord)).when(auditRecordGenerator).generate(cmd, changeContext, singletonList(auditedChildRecord));

        final Stream<? extends AuditRecord> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(expectedAuditRecord)));
    }

    @Test
    public void generateMany_AuditedParent_TwoAuditedChildrenEachOfTwoTypes_ShouldGenerateParentRecordWithFourChildren() {
        final ChangeEntityCommand<AuditedAutoIncIdType> cmd = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd1A = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd1B = mockCommand();
        final ChangeEntityCommand<TestChild2EntityType> childCmd2A = mockCommand();
        final ChangeEntityCommand<TestChild2EntityType> childCmd2B = mockCommand();

        final AuditRecord expectedAuditRecord = mockAuditRecord();
        final AuditRecord childAuditRecord1A = mockAuditRecord();
        final AuditRecord childAuditRecord1B = mockAuditRecord();
        final AuditRecord childAuditRecord2A = mockAuditRecord();
        final AuditRecord childAuditRecord2B = mockAuditRecord();
        final List<AuditRecord> allChildAuditRecords = ImmutableList.of(childAuditRecord1A,
                                                                        childAuditRecord1B,
                                                                        childAuditRecord2A,
                                                                        childAuditRecord2B);

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

        doReturn(Optional.of(childAuditRecord1A)).when(child1AuditRecordGenerator).generate(childCmd1A, changeContext, emptyList());
        doReturn(Optional.of(childAuditRecord1B)).when(child1AuditRecordGenerator).generate(childCmd1B, changeContext, emptyList());
        doReturn(Optional.of(childAuditRecord2A)).when(child2AuditRecordGenerator).generate(childCmd2A, changeContext, emptyList());
        doReturn(Optional.of(childAuditRecord2B)).when(child2AuditRecordGenerator).generate(childCmd2B, changeContext, emptyList());
        doReturn(Optional.of(expectedAuditRecord)).when(auditRecordGenerator).generate(cmd, changeContext, allChildAuditRecords);

        final Stream<? extends AuditRecord> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(expectedAuditRecord)));
    }


    @Test
    public void generateMany_ThreeAuditedLevels_OneEntityEach_ShouldGenerateThreeLevelRecord() {
        final ChangeEntityCommand<AuditedAutoIncIdType> cmd = mockCommand();
        final ChangeEntityCommand<TestChild1EntityType> childCmd = mockCommand();
        final ChangeEntityCommand<TestGrandchildEntityType> grandchildCmd = mockCommand();

        final AuditRecord expectedAuditRecord = mockAuditRecord();
        final AuditRecord childAuditRecord = mockAuditRecord();
        final AuditRecord grandchildAuditRecord = mockAuditRecord();

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

        doReturn(Optional.of(grandchildAuditRecord))
            .when(grandchildAuditRecordGenerator).generate(grandchildCmd, changeContext, emptyList());
        doReturn(Optional.of(childAuditRecord))
            .when(child1AuditRecordGenerator).generate(childCmd, changeContext, singletonList(grandchildAuditRecord));
        doReturn(Optional.of(expectedAuditRecord))
            .when(auditRecordGenerator).generate(cmd, changeContext, singletonList(childAuditRecord));

        final Stream<? extends AuditRecord> actualAuditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig, Stream.of(cmd), changeContext);

        assertThat(actualAuditRecords.collect(toSet()), equalTo(singleton(expectedAuditRecord)));
    }

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> ChangeEntityCommand<E> mockCommand() {
        return mock(ChangeEntityCommand.class);
    }

    private AuditRecord mockAuditRecord() {
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