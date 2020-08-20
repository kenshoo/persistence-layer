package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.TestEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AnyFieldsTriggerTest {

    private final AnyFieldsTrigger anyFieldTrigger = new AnyFieldsTrigger<>(Stream.of(TestEntity.FIELD_1, TestEntity.FIELD_2));

    @Test
    public void triggered_by_one_of_fields() {
        assertTrue(anyFieldTrigger.triggeredByFields(List.of(TestEntity.FIELD_1)));
    }

    @Test
    public void triggered_by_list_contains_field() {
        assertTrue(anyFieldTrigger.triggeredByFields(List.of(TestEntity.ID, TestEntity.FIELD_1)));
    }

    @Test
    public void do_not_triggered_by_list_not_contains_field() {
        assertFalse(anyFieldTrigger.triggeredByFields(List.of(TestEntity.ID)));
    }
}
