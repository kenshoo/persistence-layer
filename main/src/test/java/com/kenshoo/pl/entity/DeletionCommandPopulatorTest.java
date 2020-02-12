package com.kenshoo.pl.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kenshoo.pl.entity.internal.ChildrenIdFetcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.jooq.lambda.Seq.seq;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by libbyfr on 12/26/2019.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeletionCommandPopulatorTest {

    @Mock
    private ChildrenIdFetcher childrenIdFetcher;

    private final TestChildEntity childEntity = TestChildEntity.INSTANCE;
    private final TestEntity parentEntity = TestEntity.INSTANCE;

    private Identifier parentId = new TestEntity.Key(111);
    private Identifier childId1 = new TestChildEntity.Ordinal(222);
    private Identifier childId2 = new TestChildEntity.Ordinal(333);

    private ChangeFlowConfig flowConfig;
    private DeletionCommandPopulator underTest;

    @Before
    public void setUp() {
        underTest = new DeletionCommandPopulator(childrenIdFetcher);
        flowConfig = new ChangeFlowConfig.Builder(parentEntity)
                .withChildFlowBuilder(new ChangeFlowConfig.Builder(childEntity))
                .build();
    }

    @Test
    public void when_no_found_MissingChildrenSupplier_then_verify_fetch_from_db_is_not_called() {
        ChangeEntityCommand<TestEntity> parentCmd = new UpdateParent(parentId);

        underTest.handleRecursive(singletonList(parentCmd), flowConfig);

        verify(childrenIdFetcher, never()).fetch(any(), any());
    }

    @Test
    public void when_no_return_results_from_db_then_no_call_to_supplyNewCommand_method() {
        ChangeEntityCommand<TestEntity> parentCmd = new UpdateParent(parentId)
                .with(new DeletionOfOther(childEntity));

        setupChildrenInDB(parentCmd, Sets.newHashSet());

        underTest.handleRecursive(Lists.newArrayList(parentCmd), flowConfig);

        assertThat(chilrenOf(parentCmd).size(), is(0));
    }

    @Test
    public void when_no_found_childs_in_parent_cmd_then_call_supplyNewCommand_for_each_child_that_returned_from_db() {
        ChangeEntityCommand<TestEntity> parentCmd = new UpdateParent(parentId)
                .with(new DeletionOfOther(childEntity));

        setupChildrenInDB(parentCmd, Sets.newHashSet(new FullIdentifier(parentId, childId1, childId1)));

        underTest.handleRecursive(Lists.newArrayList(parentCmd), flowConfig);

        ChangeEntityCommand newDeletionCmd = seq(chilrenOf(parentCmd)).filter(c -> c.getChangeOperation() == ChangeOperation.DELETE).findAny().get();
        assertThat(newDeletionCmd.getIdentifier(), is(childId1));
    }

    @Test
    public void when_childCmd_equals_to_childDb_then_no_call_supplyNewCommand_method() {
        ChangeEntityCommand childCmd = new UpdateChild(childId1);
        ChangeEntityCommand<TestEntity> parentCmd = new UpdateParent(parentId)
                .with(new DeletionOfOther(childEntity))
                .with(childCmd);

        setupChildrenInDB(parentCmd, Sets.newHashSet(new FullIdentifier(parentId, childId1, childId1)));

        underTest.handleRecursive(Lists.newArrayList(parentCmd), flowConfig);

        assertThat(chilrenOf(parentCmd), contains(childCmd));
    }

    @Test
    public void add_child_cmd_to_parent_cmd_for_missing_child() {
        ChangeEntityCommand<TestEntity> parentCmd = new UpdateParent(parentId)
                .with(new DeletionOfOther(childEntity))
                .with(new UpdateChild(childId1));

        setupChildrenInDB(parentCmd, Sets.newHashSet(new FullIdentifier(parentId, childId1, childId1), new FullIdentifier(parentId, childId2, childId2)));

        underTest.handleRecursive(Lists.newArrayList(parentCmd), flowConfig);

        ChangeEntityCommand newDeletionCmd = seq(chilrenOf(parentCmd)).filter(c -> c.getChangeOperation() == ChangeOperation.DELETE).findAny().get();
        assertThat(newDeletionCmd.getIdentifier(), is(childId2));
    }

    private void setupChildrenInDB(EntityChange parentCmd, Set<FullIdentifier> fullIdentifiers) {
        when(childrenIdFetcher.fetch(any(), any())).thenReturn(fullIdentifiers.stream());
    }

    private List<ChangeEntityCommand> chilrenOf(EntityChange parentCmd) {
        return (List<ChangeEntityCommand>) parentCmd.getChildren(childEntity)
                .collect(Collectors.toList());
    }
}