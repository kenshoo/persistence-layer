package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.internal.FalseUpdatesPurger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FalseUpdatesPurgerTest {

    @Mock
    private ChangeContext changeContext;
    @Mock
    private EntityField<TestEntity, Integer> field1;
    @Mock
    private EntityField<TestEntity, String> field2;

    @Mock
    private EntityField<TestEntity, String> field3;

    private FalseUpdatesPurger<TestEntity> onTest;

    @Before
    public void setup() {
        onTest = new FalseUpdatesPurger<>(ChangeEntityCommand::unset, Stream.of(field3));
        when(field1.valuesEqual(anyInt(), anyInt())).thenAnswer(i -> Objects.equals(i.getArguments()[0], i.getArguments()[1]));
        when(field2.valuesEqual(anyString(), anyString())).thenAnswer(i -> Objects.equals(i.getArguments()[0], i.getArguments()[1]));
    }

    @Test
    public void allFalseUpdates() {
        ChangeEntityCommand<TestEntity> command1 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        command1.set(field1, 5);
        command1.set(field2, "value");
        Entity entity1 = mock(Entity.class);
        when(entity1.containsField(field1)).thenReturn(true);
        when(entity1.get(field1)).thenReturn(5);
        when(entity1.containsField(field2)).thenReturn(true);
        when(entity1.get(field2)).thenReturn("value");
        when(changeContext.getEntity(command1)).thenReturn(entity1);
        ChangeEntityCommand<TestEntity> command2 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        command2.set(field1, 10);
        Entity entity2 = mock(Entity.class);
        when(entity2.containsField(field1)).thenReturn(true);
        when(entity2.get(field1)).thenReturn(10);
        when(changeContext.getEntity(command2)).thenReturn(entity2);

        onTest.enrich(Arrays.asList(command1, command2), ChangeOperation.UPDATE, changeContext);

        assertThat(command1.getChanges().count(), is(0L));
        assertThat(command2.getChanges().count(), is(0L));
    }

    @Test
    public void oneFalseUpdate() {
        ChangeEntityCommand<TestEntity> command1 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        command1.set(field1, 5);
        command1.set(field2, "value");
        Entity entity1 = mock(Entity.class);
        when(entity1.containsField(field1)).thenReturn(true);
        when(entity1.get(field1)).thenReturn(5);
        when(entity1.containsField(field2)).thenReturn(true);
        when(entity1.get(field2)).thenReturn("value1");
        when(changeContext.getEntity(command1)).thenReturn(entity1);
        ChangeEntityCommand<TestEntity> command2 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        command2.set(field1, 10);
        Entity entity2 = mock(Entity.class);
        when(entity2.containsField(field1)).thenReturn(true);
        when(entity2.get(field1)).thenReturn(10);
        when(changeContext.getEntity(command2)).thenReturn(entity2);

        onTest.enrich(Arrays.asList(command1, command2), ChangeOperation.UPDATE, changeContext);

        //noinspection unchecked
        assertThat(command1.getChanges().count(), is(1L));
        assertThat(command2.getChanges().count(), is(0L));
    }

    @Test
    public void ignoredIfSetAloneFieldUpdate() {
        ChangeEntityCommand<TestEntity> command1 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        command1.set(field1, 5);
        command1.set(field3, "ignorable");
        Entity entity1 = mock(Entity.class);
        when(entity1.containsField(field1)).thenReturn(true);
        when(entity1.get(field1)).thenReturn(5);
        when(entity1.containsField(field3)).thenReturn(true);
        when(entity1.get(field3)).thenReturn("ignorable1");
        when(changeContext.getEntity(command1)).thenReturn(entity1);
        ChangeEntityCommand<TestEntity> command2 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        command2.set(field1, 5);
        command2.set(field3, "ignorable");
        Entity entity2 = mock(Entity.class);
        when(entity2.containsField(field1)).thenReturn(true);
        when(entity2.get(field1)).thenReturn(10);
        when(entity2.containsField(field3)).thenReturn(true);
        when(entity2.get(field3)).thenReturn("ignorable1");
        when(changeContext.getEntity(command2)).thenReturn(entity2);

        onTest.enrich(Arrays.asList(command1, command2), ChangeOperation.UPDATE, changeContext);

        //noinspection unchecked
        assertThat(command1.getChanges().count(), is(0L));
        assertThat(command2.getChanges().count(), is(2L));
    }
    private static class TestEntity extends AbstractEntityType<TestEntity> {

        static final TestEntity INSTANCE = new TestEntity();

        private TestEntity() {
            super("test");
        }

        @Override
        public DataTable getPrimaryTable() {
            return null;
        }
    }

}