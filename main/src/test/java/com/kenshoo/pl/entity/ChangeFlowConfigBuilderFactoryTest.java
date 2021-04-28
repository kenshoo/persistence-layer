package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.DontPurge;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.annotation.RequiredFieldType;
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
        assertThat(getPurger(buildFlow(FeatureSet.EMPTY)).getFieldsToRetain(), containsInAnyOrder(MockedEntity.INSTANCE.field1));
    }

    @Test
    public void testFieldsAnnotatedWithRequired() {
        ChangeFlowConfig<MockedEntity> flowConfig = buildFlow(FeatureSet.EMPTY);
        assertThat(flowConfig.getRequiredFields(), containsInAnyOrder(MockedEntity.INSTANCE.field2, MockedEntity.INSTANCE.field3));
        assertThat(flowConfig.getRequiredRelationFields(), containsInAnyOrder(MockedEntity.INSTANCE.field3));
    }


    private ChangeFlowConfig<MockedEntity> buildFlow(FeatureSet featureSet) {
        PLContext plContext = mock(PLContext.class);
        when(plContext.generateFeatureSet()).thenReturn(featureSet);
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, MockedEntity.INSTANCE).build();
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

        @Required
        public static EntityField<MockedEntity, Integer> field2 = INSTANCE.field(integerTableField("field2"));

        @Required(RequiredFieldType.RELATION)
        public static EntityField<MockedEntity, Integer> field3 = INSTANCE.field(integerTableField("field2"));
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
