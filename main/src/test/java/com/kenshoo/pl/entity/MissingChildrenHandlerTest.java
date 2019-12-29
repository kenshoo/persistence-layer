package com.kenshoo.pl.entity;

import com.google.common.collect.Lists;
import com.kenshoo.pl.entity.internal.ChildrenIdFetcher;
import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;
import org.jooq.DSLContext;
import org.jooq.TableField;
import org.jooq.lambda.Seq;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Stream.empty;

import static org.jooq.lambda.Seq.seq;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by libbyfr on 12/26/2019.
 */
@RunWith(MockitoJUnitRunner.class)
public class MissingChildrenHandlerTest {

    @Mock
    private DSLContext jooq;
    @Mock
    private ChangeEntityCommand parentCmd;
    @Mock
    private ChangeEntityCommand childCmd;
    @Mock
    private ChangeFlowConfig changeFlowConfig;
    @Mock
    private ChangeFlowConfig childChangeFlowConfig;
    @Mock
    private ChildrenIdFetcher childrenIdFetcher;
    @Mock
    private EntityType childType;
    @Mock
    private EntityType parentType;
    @Mock
    private MissingChildrenSupplier missingChildrenSupplier;
    @Mock
    private ChangeEntityCommand deleteCommand;
    @Mock
    private Identifier childIdentifier;
    @Mock
    private Identifier parentIdentifier;
    @Mock
    private EntityFieldDbAdapter entityFieldDbAdapter;
    @Mock
    private EntityField parentId;
    @Mock
    private EntityField childParentId;

    private final Integer parentIdValue = 10;

    private UniqueKeyValue childParentIdentifier;
    private MissingChildrenHandler underTest = new MissingChildrenHandler(jooq);

    @Before
    public void setUp() {
        underTest.setChildrenIdFetcher(childrenIdFetcher);
        mockChildParentIdentifier();
    }

    @Test
    public void when_no_found_MissingChildrenSupplier_then_verify_fetch_from_db_is_not_called() throws Exception {
        mockChildFlows();

        when(parentCmd.getMissingChildrenSupplier(childType)).thenReturn(Optional.empty());

        underTest.handle(Lists.newArrayList(parentCmd), changeFlowConfig);

        verify(childrenIdFetcher, never()).fetch(Lists.newArrayList(parentCmd), childType);
    }

    @Test
    public void when_no_return_results_from_db_then_no_call_to_supplyNewCommand_method() throws Exception {
        mockChildFlows();
        mockParentCmd();
        mockForeignKey();
        mockMissingChildSupplier();

        when(childrenIdFetcher.fetch(Lists.newArrayList(parentCmd), childType)).thenReturn(Stream.empty());

        underTest.handle(Lists.newArrayList(parentCmd), changeFlowConfig);

        verify(missingChildrenSupplier, never()).supplyNewCommand(any(Identifier.class));

    }

    @Test
    public void when_no_found_childs_in_parent_cmd_then_call_supplyNewCommand_for_each_child_that_returned_from_db() throws Exception {
        mockChildFlows();
        mockParentCmd();
        mockForeignKey();
        mockMissingChildSupplier();

        mockChildrenIdFetcher();
        when(parentCmd.getChildren(childType)).thenReturn(empty());

        underTest.handle(Lists.newArrayList(parentCmd), changeFlowConfig);

        verify(missingChildrenSupplier).supplyNewCommand(childIdentifier);
    }

    private void mockChildrenIdFetcher() {
        when(childrenIdFetcher.fetch(Lists.newArrayList(parentCmd), childType)).thenReturn(Seq.of(new FullIdentifier(childParentIdentifier, childIdentifier)));
    }

    @Test
    public void when_childCmd_equals_to_childDb_then_no_call_supplyNewCommand_method() throws Exception {
        mockChildFlows();
        mockParentCmd();
        mockForeignKey();
        mockMissingChildSupplier();
        mockChildrenIdFetcher();

        when(parentCmd.getChildren(childType)).thenReturn(Seq.of(childCmd));
        when(childCmd.getParent()).thenReturn(parentCmd);
        when(childCmd.getIdentifier()).thenReturn(childIdentifier);


        underTest.handle(Lists.newArrayList(parentCmd), changeFlowConfig);

        verify(missingChildrenSupplier, never()).supplyNewCommand(childIdentifier);
    }

    @Test
    public void add_child_cmd_to_parent_cmd_for_missing_child() throws Exception {
        mockChildFlows();
        mockParentCmd();
        mockForeignKey();
        mockMissingChildSupplier();
        mockChildrenIdFetcher();

        when(parentCmd.getChildren(childType)).thenReturn(empty());
        when(missingChildrenSupplier.supplyNewCommand(childIdentifier)).thenReturn(Optional.of(deleteCommand));

        underTest.handle(Lists.newArrayList(parentCmd), changeFlowConfig);

        verify(missingChildrenSupplier).supplyNewCommand(childIdentifier);
        verify(parentCmd).addChild(deleteCommand);
    }

    private void mockChildFlows() {
        when(changeFlowConfig.childFlows()).thenReturn(Lists.newArrayList(childChangeFlowConfig));
        when(childChangeFlowConfig.getEntityType()).thenReturn(childType);
    }

    private void mockParentCmd() {
        when(parentCmd.getEntityType()).thenReturn(parentType);
        when(parentCmd.getIdentifier()).thenReturn(parentIdentifier);
        when(parentIdentifier.get(parentId)).thenReturn(parentIdValue);
    }

    private void mockMissingChildSupplier() {
        when(parentCmd.getMissingChildrenSupplier(childType)).thenReturn(Optional.of(missingChildrenSupplier));
    }

    private void mockForeignKey() {
        when(childType.getKeyTo(parentType)).thenReturn(new EntityType.ForeignKey(Lists.newArrayList(childParentId), Lists.newArrayList(parentId)));
    }

    private void mockChildParentIdentifier() {
        when(childParentId.getDbAdapter()).thenReturn(entityFieldDbAdapter);
        when(entityFieldDbAdapter.getTableFields()).thenReturn(empty(), empty(), empty());
        final UniqueKey uniqueKey = new UniqueKey(Lists.newArrayList(childParentId));
        childParentIdentifier = new UniqueKeyValue(uniqueKey, seq(IntStream.of(parentIdValue)).toArray());
    }

}