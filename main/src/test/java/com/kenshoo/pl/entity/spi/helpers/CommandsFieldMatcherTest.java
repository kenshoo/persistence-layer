package com.kenshoo.pl.entity.spi.helpers;

import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.TestEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class CommandsFieldMatcherTest {

    @Test
    public void command_do_not_contain_field() {
        CreateEntityCommand<TestEntity> cmd1 = new CreateEntityCommand<>(TestEntity.INSTANCE);

        assertFalse(CommandsFieldMatcher.isAnyFieldContainedInAnyCommand(ImmutableList.of(cmd1), TestEntity.FIELD_1));
        assertTrue(CommandsFieldMatcher.isAnyFieldMissingInAnyCommand(ImmutableList.of(cmd1), TestEntity.FIELD_1));
    }

    @Test
    public void command_contain_field() {
        CreateEntityCommand<TestEntity> cmd1 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd1.set(TestEntity.FIELD_1, "test");

        assertTrue(CommandsFieldMatcher.isAnyFieldContainedInAnyCommand(ImmutableList.of(cmd1), TestEntity.FIELD_1));
        assertFalse(CommandsFieldMatcher.isAnyFieldMissingInAnyCommand(ImmutableList.of(cmd1), TestEntity.FIELD_1));
    }

    @Test
    public void at_least_one_command_contain_field() {
        CreateEntityCommand<TestEntity> cmd1 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        CreateEntityCommand<TestEntity> cmd2 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd2.set(TestEntity.FIELD_1, "test");

        assertTrue(CommandsFieldMatcher.isAnyFieldContainedInAnyCommand(ImmutableList.of(cmd1, cmd2), TestEntity.FIELD_1));
    }


    @Test
    public void at_least_one_command_do_not_contain_field() {
        CreateEntityCommand<TestEntity> cmd1 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        CreateEntityCommand<TestEntity> cmd2 = new CreateEntityCommand<>(TestEntity.INSTANCE);
        cmd2.set(TestEntity.FIELD_1, "test");

        assertTrue(CommandsFieldMatcher.isAnyFieldMissingInAnyCommand(ImmutableList.of(cmd1, cmd2), TestEntity.FIELD_1));
    }
}