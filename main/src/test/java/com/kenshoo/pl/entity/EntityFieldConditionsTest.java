package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.AbstractDataTable;
import com.kenshoo.jooq.DataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.junit.Test;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EntityFieldConditionsTest {

    @Test
    public void eqForRegularFieldShouldBuildConditionWithField() {
        final PLCondition plCondition = TestEntityType.NAME1.eq("abc");

        assertContainsFields(plCondition, TestEntityType.NAME1);
    }

    @Test
    public void testFieldEqualInPostFetchCondition() {
        final var entity = createEntityWith(TestEntityType.NAME1, "myName");

        assertThat(TestEntityType.NAME1.eq("myName").getPostFetchCondition().test(entity), is(true));
        assertThat(TestEntityType.NAME1.eq("abcd").getPostFetchCondition().test(entity), is(false));
    }

    @Test
    public void testFieldInValuesInPostFetchCondition() {
        final var entity = createEntityWith(TestEntityType.NAME1, "myName");

        assertThat(TestEntityType.NAME1.in("abcd", "myName").getPostFetchCondition().test(entity), is(true));
        assertThat(TestEntityType.NAME1.in("abcd", "aaaa").getPostFetchCondition().test(entity), is(false));
    }

    @Test
    public void eqBetweenTwoRegularFieldShouldBuildConditionWithField() {
        final PLCondition plCondition = TestEntityType.NAME1.eq(TestEntityType.NAME2);

        assertContainsFields(plCondition, TestEntityType.NAME1, TestEntityType.NAME2);
    }

    @Test
    public void testTwoFieldEqualInPostFetchCondition() {
        final var entity = createEntityWith(TestEntityType.NAME1, "myName", TestEntityType.NAME2, "myName");

        assertThat(TestEntityType.NAME1.eq(TestEntityType.NAME2).getPostFetchCondition().test(entity), is(true));
    }

    @Test
    public void testTwoFieldNotEqualInPostFetchCondition() {
        final var entity = createEntityWith(TestEntityType.NAME1, "myName", TestEntityType.NAME2, "anotherName");

        assertThat(TestEntityType.NAME1.eq(TestEntityType.NAME2).getPostFetchCondition().test(entity), is(false));
    }

    @Test
    public void testFieldInValuesInPostFetchConditionWhenFieldValueIsNull() {
        final var entity = createEntityWith(TestEntityType.NAME1, null);

        assertThat(TestEntityType.NAME1.in("abcd").getPostFetchCondition().test(entity), is(false));
    }

    @Test
    public void testFieldIsNullInPostFetchCondition() {
        final var entityWithNullValue = createEntityWith(TestEntityType.NAME1, null);
        final var entity = createEntityWith(TestEntityType.NAME1, "myName");

        assertThat(TestEntityType.NAME1.isNull().getPostFetchCondition().test(entityWithNullValue), is(true));
        assertThat(TestEntityType.NAME1.isNull().getPostFetchCondition().test(entity), is(false));
    }

    @Test
    public void testFieldIsNotNullInPostFetchCondition() {
        final var entityWithNullValue = createEntityWith(TestEntityType.NAME1, null);
        final var entity = createEntityWith(TestEntityType.NAME1, "myName");

        assertThat(TestEntityType.NAME1.isNotNull().getPostFetchCondition().test(entityWithNullValue), is(false));
        assertThat(TestEntityType.NAME1.isNotNull().getPostFetchCondition().test(entity), is(true));
    }

    private FinalEntityState createEntityWith(EntityField<TestEntityType, String> field, String value) {
        return new FinalEntityState(CurrentEntityState.EMPTY, new CreateEntityCommand<>(TestEntityType.INSTANCE) {{
            set(field, value);
        }});
    }

    private FinalEntityState createEntityWith(EntityField<TestEntityType, String> field, String value, EntityField<TestEntityType, String> field2, String value2) {
        return new FinalEntityState(CurrentEntityState.EMPTY, new CreateEntityCommand<>(TestEntityType.INSTANCE) {{
            set(field, value);
            set(field2, value2);
        }});
    }

    @Test(expected = UnsupportedOperationException.class)
    public void eqForVirtualFieldShouldThrowException() {
        TestEntityType.FULL_NAME.eq("abc");
    }

    private void assertContainsFields(final PLCondition plCondition,
                                      final EntityField<?, ?>... fields) {
        assertThat("Incorrect fields in PL condition: ",
                   plCondition.getFields(), equalTo(ImmutableSet.copyOf(fields)));
    }

    private static class TestTable extends AbstractDataTable<TestTable> {
        private static final TestTable INSTANCE = new TestTable("test");

        private final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        private final TableField<Record, String> name1 = createField("name1", SQLDataType.VARCHAR.length(50));
        private final TableField<Record, String> name2 = createField("name2", SQLDataType.VARCHAR.length(50));

        public TestTable(String name) {
            super(name);
        }

        public TestTable(TestTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public TestTable as(String alias) {
            return new TestTable(this, alias);
        }
    }

    public static class TestEntityType extends AbstractEntityType<TestEntityType> {

        public static final TestEntityType INSTANCE = new TestEntityType();

        public static final EntityField<TestEntityType, Integer> ID = INSTANCE.field(TestTable.INSTANCE.id);
        public static final EntityField<TestEntityType, String> NAME1 = INSTANCE.field(TestTable.INSTANCE.name1);
        public static final EntityField<TestEntityType, String> NAME2 = INSTANCE.field(TestTable.INSTANCE.name2);
        public static final EntityField<TestEntityType, String> FULL_NAME = INSTANCE.virtualField(NAME1, NAME2,
                                                                                                  (name1, name2) -> name1 + " " + name2,
                                                                                                  new CommonTypesStringConverter<>(String.class),
                                                                                                  Objects::equals);

        private TestEntityType() {
            super("test");
        }

        @Override
        public DataTable getPrimaryTable() {
            return TestTable.INSTANCE;
        }
    }
}
