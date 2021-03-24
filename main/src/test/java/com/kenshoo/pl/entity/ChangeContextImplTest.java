package com.kenshoo.pl.entity;

import org.junit.Test;

import static org.junit.Assert.*;

public class ChangeContextImplTest {

    private final ChangeContextImpl changeContext = new ChangeContextImpl(null, null);

    @Test
    public void containsShowStopperErrorTest() {
        EntityChange<TestEntity> entityChange = new CreateEntityCommand<>(TestEntity.INSTANCE);
        changeContext.addValidationError(entityChange, new ValidationError(null, null, null, ValidationError.ShowStopper.Yes));
        assertTrue(changeContext.containsShowStopperErrorNonRecursive(entityChange));
    }

    @Test
    public void doesNotContainShowStopperErrorTest() {
        EntityChange<TestEntity> entityChange = new CreateEntityCommand<>(TestEntity.INSTANCE);
        changeContext.addValidationError(entityChange, new ValidationError(null, null, null));
        assertFalse(changeContext.containsShowStopperErrorNonRecursive(entityChange));
    }
}