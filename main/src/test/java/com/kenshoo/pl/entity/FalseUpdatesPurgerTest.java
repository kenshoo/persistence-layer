package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.kenshoo.jooq.AbstractDataTable;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.internal.FalseUpdatesPurger;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class FalseUpdatesPurgerTest {

    @Mock
    private ChangeContext changeContext;
    @Mock
    private EntityField<TestEntity, Integer> field1;
    @Mock
    private EntityField<TestEntity, String> field2;

    @Before
    public void setup() {
        when(field1.valuesEqual(anyInt(), anyInt())).thenAnswer(i -> Objects.equals(i.getArguments()[0], i.getArguments()[1]));
        when(field2.valuesEqual(anyString(), anyString())).thenAnswer(i -> Objects.equals(i.getArguments()[0], i.getArguments()[1]));
    }

    @Test
    public void requiredFieldsForUpdateNewApi() {
        List<EntityField<?, ?>> fields = purger().build().requiredFields(ImmutableList.of(field1,field2), UPDATE).collect(toList());
        assertThat(fields, containsInAnyOrder(field1, field2));
    }

    @Test
    public void allFalseUpdates() {

        TestCommand command1 = new TestCommand()
            .with(field1, 5)
            .with(field2, "value");

        when(changeContext.getEntity(command1)).thenReturn(currentState(ImmutableMap.of(
                field1, 5,
                field2, "value"
        )));

        purger().build().enrich(asList(command1), UPDATE, changeContext);

        assertThat(command1.getChanges().count(), is(0L));
    }

    @Test
    public void oneFalseUpdate() {

        TestCommand command1 = new TestCommand()
                .with(field1, 5)
                .with(field2, "new value");

        when(changeContext.getEntity(command1)).thenReturn(currentState(ImmutableMap.of(
                field1, 5,
                field2, "old value"
        )));

        purger().build().enrich(asList(command1), UPDATE, changeContext);

        assertThat(command1.getChanges().map(FieldChange::getValue).collect(toList()), contains("new value"));
    }

    @Test
    public void removeChangeIfSetAloneFieldUpdate() {

        TestCommand command1 = new TestCommand()
                .with(field1, 5)
                .with(field2, "new value");

        when(changeContext.getEntity(command1)).thenReturn(currentState(ImmutableMap.of(
                field1, 5,
                field2, "old value"
        )));

        purger().setDeleteIfSetAloneFields(field2).build().enrich(asList(command1), UPDATE, changeContext);

        assertThat(command1.getChanges().count(), is(0L));
    }

    @Test
    public void dontRemoveFieldToRetain() {

        TestCommand command1 = new TestCommand()
                .with(field1, 5)
                .with(field2, "value");

        when(changeContext.getEntity(command1)).thenReturn(currentState(ImmutableMap.of(
                field1, 5,
                field2, "value"
        )));

        purger().addFieldsToRetain(field1).build().enrich(asList(command1), UPDATE, changeContext);

        assertThat(command1.getChanges().map(c -> c.getField()).collect(toList()), contains(field1));
    }

    @Test
    public void retain_non_nullable_fields_of_secondary_tables() {
        FalseUpdatesPurger<TestEntity> purger = purger().retainNonNullableFieldsOfSecondaryTables(TestEntity.INSTANCE).build();
        assertThat(purger.getFieldsToRetain(), containsInAnyOrder(TestEntity.NON_NULLABLE_SECONDARY_TABLE_FIELD));
    }

    private FalseUpdatesPurger.Builder<TestEntity> purger() {
        return new FalseUpdatesPurger.Builder<TestEntity>().setFieldUnsetter(ChangeEntityCommand::unset);
    }

    private static class PrimaryTable extends AbstractDataTable<PrimaryTable> {
        public static PrimaryTable INSTANCE = new PrimaryTable();
        public PrimaryTable() { super("primary"); }
        public PrimaryTable as(String alias) { return this; }
    }

    private static class SecondaryTable extends AbstractDataTable<SecondaryTable> {
        public static SecondaryTable INSTANCE = new SecondaryTable();
        public SecondaryTable() { super("secondary"); }
        public SecondaryTable as(String alias) { return this; }
        public TableField nullable_secondary_field = createField("nullable_secondary_field", SQLDataType.INTEGER.nullable(true));
        public TableField non_nullable_secondary_field = createField("no_nullable_secondary_field", SQLDataType.INTEGER.nullable(false));
    }

    private static class TestEntity extends AbstractEntityType<TestEntity> {

        static final TestEntity INSTANCE = new TestEntity();

        private TestEntity() {
            super("test");
        }

        @Override
        public DataTable getPrimaryTable() {
            return PrimaryTable.INSTANCE;
        }

        public static EntityField<TestEntity, Integer> NULLABLE_SECONDARY_TABLE_FIELD = INSTANCE.field(SecondaryTable.INSTANCE.nullable_secondary_field);
        public static EntityField<TestEntity, Integer> NON_NULLABLE_SECONDARY_TABLE_FIELD = INSTANCE.field(SecondaryTable.INSTANCE.non_nullable_secondary_field);
    }

    private static class TestCommand extends CreateEntityCommand<TestEntity> {

        public TestCommand() {
            super(TestEntity.INSTANCE);
        }

        public <VAL> TestCommand with(EntityField<TestEntity, VAL> field, VAL value) {
            set(field, value);
            return this;
        }
    }

    private CurrentEntityState currentState(Map<EntityField<TestEntity, ?>, Object> state) {
        return new CurrentEntityState() {

            @Override
            public boolean containsField(EntityField<?, ?> field) {
                return state.containsKey(field);
            }

            @Override
            public <T> T get(EntityField<?, T> field) {
                return (T)state.get(field);
            }

        };
    }

}