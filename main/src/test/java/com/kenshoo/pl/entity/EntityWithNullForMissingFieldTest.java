package com.kenshoo.pl.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.kenshoo.pl.entity.TestEntity.FIELD_1;
import static com.kenshoo.pl.entity.TestEntity.FIELD_2;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EntityWithNullForMissingFieldTest {

    @Mock
    private Entity entity;

    @Test
    public void testContainsField() {
        when(entity.containsField(FIELD_1)).thenReturn(true);
        when(entity.containsField(FIELD_2)).thenReturn(false);

        final var entityWithNullForMissingField = new EntityWithNullForMissingField(entity);

        assertThat(entityWithNullForMissingField.containsField(FIELD_1), is(true));
        assertThat(entityWithNullForMissingField.containsField(FIELD_2), is(false));
    }

    @Test
    public void testGetReturnsEntityFieldValue() {
        when(entity.containsField(FIELD_1)).thenReturn(true);
        when(entity.get(FIELD_1)).thenReturn("value of field1");

        final var entityWithNullForMissingField = new EntityWithNullForMissingField(entity);

        assertThat(entityWithNullForMissingField.get(FIELD_1), is("value of field1"));
    }

    @Test
    public void testGetReturnsNullWhenFieldIsMissing() {
        when(entity.containsField(FIELD_1)).thenReturn(false);

        final var entityWithNullForMissingField = new EntityWithNullForMissingField(entity);

        assertThat(entityWithNullForMissingField.get(FIELD_1), is(nullValue()));
    }

}