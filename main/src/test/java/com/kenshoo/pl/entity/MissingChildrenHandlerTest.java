package com.kenshoo.pl.entity;

import com.google.common.collect.Lists;
import com.kenshoo.jooq.AbstractDataTable;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.internal.ChildrenIdFetcher;
import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
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
    private ChildrenIdFetcher childrenIdFetcher;
    @Mock
    private MissingChildrenSupplier missingChildrenSupplier;
    @Mock
    private ChangeEntityCommand deleteCommand;
    @Mock
    private Identifier childIdentifier;


    private final Integer parentIdValue = 10;
    private final ChildEntity childEntity = ChildEntity.INSTANCE;
    private final ParentEntity parentEntity = ParentEntity.INSTANCE;

    private UniqueKeyValue childParentIdentifier;
    private MissingChildrenHandler underTest;
    private ChangeFlowConfig config;
    private ChangeEntityCommand parentCmd;

    @Before
    public void setUp() {
        underTest = new MissingChildrenHandler(childrenIdFetcher);
        config = new ChangeFlowConfig.Builder(parentEntity).withChildFlowBuilder(new ChangeFlowConfig.Builder(childEntity)).build();

        childParentIdentifier = setupChildParentIdentifier();
        parentCmd = setupParentCmd();
    }

    @Test
    public void when_no_found_MissingChildrenSupplier_then_verify_fetch_from_db_is_not_called() {
        when(parentCmd.getMissingChildrenSupplier(childEntity)).thenReturn(Optional.empty());

        underTest.handle(Lists.newArrayList(parentCmd), config);

        verify(childrenIdFetcher, never()).fetch(Lists.newArrayList(parentCmd), childEntity);
    }

    @Test
    public void when_no_return_results_from_db_then_no_call_to_supplyNewCommand_method() {
        when(childrenIdFetcher.fetch(Lists.newArrayList(parentCmd), childEntity)).thenReturn(Stream.empty());

        underTest.handle(Lists.newArrayList(parentCmd), config);

        verify(missingChildrenSupplier, never()).supplyNewCommand(any(Identifier.class));

    }

    @Test
    public void when_no_found_childs_in_parent_cmd_then_call_supplyNewCommand_for_each_child_that_returned_from_db() {
        when(childrenIdFetcher.fetch(Lists.newArrayList(parentCmd), childEntity)).thenReturn(Seq.of(setUpChildrenIdFetcher()));
        when(parentCmd.getChildren(childEntity)).thenReturn(empty());

        underTest.handle(Lists.newArrayList(parentCmd), config);

        verify(missingChildrenSupplier).supplyNewCommand(childIdentifier);
    }

    @Test
    public void when_childCmd_equals_to_childDb_then_no_call_supplyNewCommand_method() {
        when(childrenIdFetcher.fetch(Lists.newArrayList(parentCmd), childEntity)).thenReturn(Seq.of(setUpChildrenIdFetcher()));

        ChangeEntityCommand childCmd = mock(ChangeEntityCommand.class);
        when(parentCmd.getChildren(childEntity)).thenReturn(Seq.of(childCmd));
        when(childCmd.getParent()).thenReturn(parentCmd);
        when(childCmd.getIdentifier()).thenReturn(childIdentifier);


        underTest.handle(Lists.newArrayList(parentCmd), config);

        verify(missingChildrenSupplier, never()).supplyNewCommand(childIdentifier);
    }

    @Test
    public void add_child_cmd_to_parent_cmd_for_missing_child() {
        when(childrenIdFetcher.fetch(Lists.newArrayList(parentCmd), childEntity)).thenReturn(Seq.of(setUpChildrenIdFetcher()));

        when(parentCmd.getChildren(childEntity)).thenReturn(empty());
        when(missingChildrenSupplier.supplyNewCommand(childIdentifier)).thenReturn(Optional.of(deleteCommand));

        underTest.handle(Lists.newArrayList(parentCmd), config);

        verify(missingChildrenSupplier).supplyNewCommand(childIdentifier);
        verify(parentCmd).addChild(deleteCommand);
    }

    private ChangeEntityCommand setupParentCmd() {
       ChangeEntityCommand parentCmd = mock(ChangeEntityCommand.class);
        Identifier parentIdentifier = mock(Identifier.class);
        when(parentCmd.getEntityType()).thenReturn(parentEntity);
        when(parentCmd.getMissingChildrenSupplier(childEntity)).thenReturn(Optional.of(missingChildrenSupplier));
        when(parentCmd.getIdentifier()).thenReturn(parentIdentifier);
        when(parentIdentifier.get(parentEntity.ID)).thenReturn(parentIdValue);
        return parentCmd;
    }

    private UniqueKeyValue setupChildParentIdentifier() {
        final UniqueKey uniqueKey = new UniqueKey(Lists.newArrayList(childEntity.PARENT_ID));
        return new UniqueKeyValue(uniqueKey, seq(IntStream.of(parentIdValue)).toArray());
    }

    private FullIdentifier setUpChildrenIdFetcher() {
        return new FullIdentifier(childParentIdentifier, childIdentifier);
    }


    static class ParentTable extends AbstractDataTable<ParentTable> {

        static final ParentTable INSTANCE = new ParentTable();

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);

        public ParentTable() {
            super("ParentTable");
        }

        public ParentTable(ParentTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public ParentTable as(String alias) {
            return new ParentTable(this, alias);
        }
    }


    static class ChildTable extends AbstractDataTable<ChildTable> {

        static final ChildTable INSTANCE = new ChildTable();

        final TableField<Record, Integer> parent_id = createPKAndFKField("parent_id", SQLDataType.INTEGER, ParentTable.INSTANCE.id);
        final TableField<Record, Integer> child_id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(64));

        public ChildTable() {
            super("ChildTable");
        }

        public ChildTable(ChildTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public ChildTable as(String alias) {
            return new ChildTable(this, alias);
        }
    }


    static class ParentEntity extends AbstractEntityType<ParentEntity> {

        static final ParentEntity INSTANCE = new ParentEntity();

        @Id
        static final EntityField<ParentEntity, Integer> ID = INSTANCE.field(ParentTable.INSTANCE.id);

        private ParentEntity() {
            super("parent");
        }

        @Override
        public DataTable getPrimaryTable() {
            return ParentTable.INSTANCE;
        }

        @Override
        public SupportedChangeOperation getSupportedOperation()  {
            return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
        }
    }


    static class ChildEntity extends AbstractEntityType<ChildEntity> {

        static final ChildEntity INSTANCE = new ChildEntity();


        static final EntityField<ChildEntity, Integer> CHILD_ID = INSTANCE.field(ChildTable.INSTANCE.child_id);

        static final EntityField<ChildEntity, String> NAME = INSTANCE.field(ChildTable.INSTANCE.name);

        static final EntityField<ChildEntity, Integer> PARENT_ID = INSTANCE.field(ChildTable.INSTANCE.parent_id);

        private ChildEntity() {
            super("child");
        }

        @Override
        public DataTable getPrimaryTable() {
            return ChildTable.INSTANCE;
        }

        @Override
        public SupportedChangeOperation getSupportedOperation()  {
            return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
        }
    }

}