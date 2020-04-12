package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.DontPurge;
import com.kenshoo.pl.entity.internal.FalseUpdatesPurger;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.jooq.lambda.Seq.seq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangeFlowConfigBuilderFactoryTest {


    @Test
    public void testFieldsAnnotatedWithDontPurgeArePassedToTheFalseUpdatePurger() {
        assertThat(getPurger(buildFlow()).getFieldsToRetain(), containsInAnyOrder(MockedEntity.INSTANCE.field1));
    }

    private ChangeFlowConfig<MockedEntity> buildFlow() {
        return ChangeFlowConfigBuilderFactory.newInstance(mock(PLContext.class), MockedEntity.INSTANCE).build();
    }

    private FalseUpdatesPurger<MockedEntity> getPurger(ChangeFlowConfig<MockedEntity> flow) {
        return (FalseUpdatesPurger) seq(flow.getPostFetchCommandEnrichers()).findFirst(e -> e.getClass().equals(FalseUpdatesPurger.class)).get();
    }

    public static class MockedEntity extends AbstractEntityType<MockedEntity> {

        static MockedEntity INSTANCE = new MockedEntity();

        protected MockedEntity() {
            super("xxx");
        }

        @Override public DataTable getPrimaryTable() {
            return mock(DataTable.class);
        }

        @DontPurge
        public static EntityField<MockedEntity, Integer> field1 = INSTANCE.field(integerTableField("field1"));

        public static EntityField<MockedEntity, Integer> field2 = INSTANCE.field(integerTableField("field2"));
    }

    private static <R extends Record> TableField<R, Integer> integerTableField(String name) {
        TableField<R, Integer> mocked = mock(TableField.class);
        Table table = mock(DataTable.class);
        when(mocked.getTable()).thenReturn(table);
        when(mocked.getName()).thenReturn(name);
        when(mocked.getType()).thenReturn(Integer.class);
        when(mocked.getDataType()).thenReturn(SQLDataType.INTEGER);
        return mocked;
    }
}
