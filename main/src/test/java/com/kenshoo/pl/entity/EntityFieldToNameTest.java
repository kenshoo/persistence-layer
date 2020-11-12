package com.kenshoo.pl.entity;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by yuvalr on 7/3/16.
 */
public class EntityFieldToNameTest {

    @Test
    public void idFieldToString() {
        String fieldName = TestEntity.INSTANCE.toFieldName(TestEntity.ID);

        assertThat(fieldName, is("ID"));
    }

    @Test
    public void badInput() {
        String fieldName = TestEntity.INSTANCE.toFieldName(null);

        assertThat(fieldName, nullValue());
    }

    @Test
    public void prototypeFieldToName() {
        String fieldName = TestEntity.INSTANCE.toFieldName(TestEntity.FIELD_1);

        assertThat(fieldName, is("FIELD_1"));
    }
}
