package com.kenshoo.pl.auto.inc;

import com.kenshoo.pl.entity.CreateEntityCommand;

class TestEntityCreateCommand extends CreateEntityCommand<TestEntity> implements EntityCommandExt<TestEntity, TestEntityCreateCommand> {

    public TestEntityCreateCommand() {
        super(TestEntity.INSTANCE);
    }
}
