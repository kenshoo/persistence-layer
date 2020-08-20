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
    public void triggered_exactly_by_field() {
        assertTrue(fieldTrigger.triggeredByFields(List.of(TestEntity.FIELD_1)));
    }

    @Test
    public void triggered_by_list_contains_field() {
        assertTrue(fieldTrigger.triggeredByFields(List.of(TestEntity.FIELD_2, TestEntity.FIELD_1)));
    }

    @Test
    public void do_not_triggered_by_list_not_contains_field() {
        assertFalse(fieldTrigger.triggeredByFields(List.of(TestEntity.FIELD_2)));
    }
}