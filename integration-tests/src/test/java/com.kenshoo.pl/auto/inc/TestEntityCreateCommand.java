package com.kenshoo.pl.auto.inc;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityField;

class TestEntityCreateCommand extends CreateEntityCommand<TestEntity> {

    public TestEntityCreateCommand() {
        super(TestEntity.INSTANCE);
    }

    public <T> TestEntityCreateCommand with(EntityField<TestEntity, T> field, T newValue) {
        set(field, newValue);
        return this;
    }

}
