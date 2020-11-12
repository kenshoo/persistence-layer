package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.spi.MutableCommand;

public class UpdateParent extends CreateEntityCommand<TestEntity> implements EntityCommandExt<TestEntity, UpdateParent> {

    UpdateParent(Identifier<TestEntity> identifier) {
        super(TestEntity.INSTANCE);
        this.setIdentifier(identifier);
    }

}
