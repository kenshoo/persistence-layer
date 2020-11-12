package com.kenshoo.pl.entity;

import org.junit.Test;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class InsertOnDuplicateUpdateCommandTest {

    @Test
    public void whenOperatorBecomes_CREATE_thenSetChildrenTo_CREATE_recursively() {

        var parent = newCommand();
        var child = newCommand();
        var grand = newCommand();

        parent.addChild(child);
        child.addChild(grand);

        parent.updateOperator(CREATE);

        assertThat(child.getChangeOperation(), is(CREATE));
        assertThat(grand.getChangeOperation(), is(CREATE));
    }

    private InsertOnDuplicateUpdateCommand<TestEntity, TestEntity.Key> newCommand() {
        return new InsertOnDuplicateUpdateCommand<>(TestEntity.INSTANCE, new TestEntity.Key(1));
    }

}