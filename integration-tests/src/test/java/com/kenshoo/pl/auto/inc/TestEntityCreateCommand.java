package com.kenshoo.pl.auto.inc;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;

public class TestEntityCreateCommand extends CreateEntityCommand<TestEntity> implements EntityCommandExt<TestEntity, TestEntityCreateCommand> {

    public TestEntityCreateCommand() {
        super(TestEntity.INSTANCE);
    }
}
