package com.kenshoo.pl.entity;

public class UpdateChild extends CreateEntityCommand<TestChildEntity> implements EntityCommandExt<TestChildEntity, UpdateChild> {

    UpdateChild(Identifier<TestChildEntity> identifier) {
        super(TestChildEntity.INSTANCE);
        this.setIdentifier(identifier);
    }

}
