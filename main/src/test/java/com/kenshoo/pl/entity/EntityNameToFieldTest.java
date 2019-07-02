package com.kenshoo.pl.entity;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by yuvalr on 7/3/16.
 */
public class EntityNameToFieldTest {

    @Test
    public void idFieldFromName() {
        EntityField<TestEntity, ?> fieldByName = TestEntity.INSTANCE.getFieldByName("ID");

        assertThat(fieldByName, is(TestEntity.ID));
    }

    @Test
    public void prototypeFieldFromName() {
        EntityField<TestEntity, ?> fieldByName = TestEntity.INSTANCE.getFieldByName("FIELD_1");

        assertThat(fieldByName, is(TestEntity.FIELD_1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void noSuchField() throws NoSuchFieldException {
        TestEntity.INSTANCE.getFieldByName("no such field");
    }

    @Test(expected = IllegalArgumentException.class)
    public void notAnEntityField() throws NoSuchFieldException {
        TestEntity.INSTANCE.getFieldByName("INSTANCE");
    }
}
