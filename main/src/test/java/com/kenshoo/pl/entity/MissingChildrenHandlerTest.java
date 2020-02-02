package com.kenshoo.pl.entity;

import com.google.common.collect.Lists;
import com.kenshoo.pl.entity.internal.ChildrenIdFetcher;
import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;
import org.jooq.lambda.Seq;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Stream.empty;

import static org.jooq.lambda.Seq.seq;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by libbyfr on 12/26/2019.
 */
@RunWith(MockitoJUnitRunner.class)
public class MissingChildrenHandlerTest {

    @Mock
    private ChildrenIdFetcher childrenIdFetcher;

    private final TestChildEntity childEntity = TestChildEntity.INSTANCE;
    private final TestEntity parentEntity = TestEntity.INSTANCE;

    private Identifier parentId = new TestEntity.Key(111);
    private Identifier childParentId = new TestChildEntity.ParentId(111);
    private Identifier childId1 = new TestChildEntity.Ordinal(222);
    private Identifier childId2 = new TestChildEntity.Ordinal(333);

    private ChangeFlowConfig config;
    private MissingChildrenHandler underTest;

    @Before
    public void setUp() {
        underTest = new MissingChildrenHandler(childrenIdFetcher);
        config = new ChangeFlowConfig.Builder(parentEntity)
                .withChildFlowBuilder(new ChangeFlowConfig.Builder(childEntity))
                .build();
    }

    @Test
    public void when_no_found_MissingChildrenSupplier_then_verify_fetch_from_db_is_not_called() {
        EntityChange parentCmd = new UpdateParent(parentId);

        underTest.handle(Lists.newArrayList(parentCmd), config);

        verify(childrenIdFetcher, never()).fetch(Lists.newArrayList(parentCmd), childEntity);
    }

    @Test
    public void when_no_return_results_from_db_then_no_call_to_supplyNewCommand_method() {
        EntityChange parentCmd = new UpdateParent(parentId)
                .with(new DeletionOfOther(childEntity));

        setupChildrenInDB(parentCmd, Stream.empty());

        underTest.handle(Lists.newArrayList(parentCmd), config);

        assertThat(getChildCmds(parentCmd).size(), is(0));
    }

    @Test
    public void when_no_found_childs_in_parent_cmd_then_call_supplyNewCommand_for_each_child_that_returned_from_db() {
        EntityChange parentCmd = new UpdateParent(parentId)
                .with(new DeletionOfOther(childEntity));

        setupChildrenInDB(parentCmd, Seq.of(new FullIdentifier(childParentId, childId1)));

        underTest.handle(Lists.newArrayList(parentCmd), config);

        List<ChangeEntityCommand> newChildCmds = getChildCmds(parentCmd);
        assertThat(newChildCmds.get(0).getIdentifier(), is(childId1));
    }

    @Test
    public void when_childCmd_equals_to_childDb_then_no_call_supplyNewCommand_method() {
        EntityChange parentCmd = new UpdateParent(parentId)
                .with(new DeletionOfOther(childEntity))
                .with(new UpdateChild(childId1));

        setupChildrenInDB(parentCmd, Seq.of(new FullIdentifier(childParentId, childId1)));

        underTest.handle(Lists.newArrayList(parentCmd), config);

        List<ChangeEntityCommand> newChildCmds = getChildCmds(parentCmd);
        assertThat(newChildCmds.size(), is(1));
        assertThat(newChildCmds.get(0).getIdentifier(), is(childId1));
    }

    @Test
    public void add_child_cmd_to_parent_cmd_for_missing_child() {
        EntityChange parentCmd = new UpdateParent(parentId)
                .with(new DeletionOfOther(childEntity))
                .with(new UpdateChild(childId1));

        setupChildrenInDB(parentCmd, Seq.of(new FullIdentifier(childParentId, childId1), new FullIdentifier(childParentId, childId2)));

        underTest.handle(Lists.newArrayList(parentCmd), config);

        List<ChangeEntityCommand> newChildCmds = getChildCmds(parentCmd);
        assertThat(newChildCmds.size(), is(2));
        assertThat(newChildCmds.get(0).getIdentifier(), is(childId1));
        assertThat(newChildCmds.get(1).getIdentifier(), is(childId2));
    }

    private void setupChildrenInDB(EntityChange parentCmd, Stream<FullIdentifier> fullIdentifiers) {
        when(childrenIdFetcher.fetch(Lists.newArrayList(parentCmd), childEntity)).thenReturn(fullIdentifiers);
    }

    private List<ChangeEntityCommand> getChildCmds(EntityChange parentCmd) {
        return (List<ChangeEntityCommand>) parentCmd.getChildren(childEntity)
                .collect(Collectors.toList());
    }
}