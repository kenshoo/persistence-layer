package com.kenshoo.pl.auto.inc;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.UpdateEntityCommand;

public class TestEntityUpdateCommand extends UpdateEntityCommand<TestEntity, TestEntity.Key> implements EntityCommandExt<TestEntity, TestEntityUpdateCommand> {

    public TestEntityUpdateCommand(int id) {
        super(TestEntity.INSTANCE, new TestEntity.Key(id));
    }
}
