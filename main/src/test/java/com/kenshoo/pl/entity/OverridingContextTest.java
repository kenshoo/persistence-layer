package com.kenshoo.pl.entity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OverridingContextTest {

    @Mock
    private ChangeContext original;

    private OverridingContext overridingContext;

    @Before
    public void setUp() {
        overridingContext = new OverridingContext(original);
    }

    @Test
    public void containsShowStopperErrorTest() {
        EntityChange<TestEntity> entityChange = new CreateEntityCommand<>(TestEntity.INSTANCE);
        when(original.containsShowStopperErrorNonRecursive(entityChange)).thenReturn(true);
        assertTrue(overridingContext.containsShowStopperErrorNonRecursive(entityChange));
    }

    @Test
    public void doesNotContainShowStopperErrorTest() {
        EntityChange<TestEntity> entityChange = new CreateEntityCommand<>(TestEntity.INSTANCE);
        when(original.containsShowStopperErrorNonRecursive(entityChange)).thenReturn(false);
        assertFalse(overridingContext.containsShowStopperErrorNonRecursive(entityChange));
    }
}