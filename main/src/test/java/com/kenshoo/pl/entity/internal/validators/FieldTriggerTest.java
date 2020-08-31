package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.TestEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FieldTriggerTest {

    private final FieldTrigger fieldTrigger = new FieldTrigger<>(TestEntity.FIELD_1);

    @Test
    public void triggeredExactlyByField() {
        assertTrue(fieldTrigger.triggeredByFields(List.of(TestEntity.FIELD_1)));
    }

    @Test
    public void triggeredByListContainsField() {
        assertTrue(fieldTrigger.triggeredByFields(List.of(TestEntity.FIELD_2, TestEntity.FIELD_1)));
    }

    @Test
    public void isNotTriggeredByListDoesNotContainField() {
        assertFalse(fieldTrigger.triggeredByFields(List.of(TestEntity.FIELD_2)));
    }
}