package com.kenshoo.pl.entity;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EntityFieldFindFirstTableFieldTest {

    @Test
    public void findWhenExists() {
        assertThat(TestEntity.FIELD_1.findFirstTableField(),
                   is(TestEntityTable.TABLE.field_1));
    }
}
