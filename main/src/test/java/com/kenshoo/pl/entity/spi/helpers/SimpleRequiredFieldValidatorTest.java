package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.CurrentEntityMutableState;
import com.kenshoo.pl.entity.TestEntity;
import com.kenshoo.pl.entity.spi.RequiredFieldValidator;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

public class SimpleRequiredFieldValidatorTest {


    @Test
    public void returnRequiredFieldTest() {
        RequiredFieldValidator<TestEntity, String> validator = new SimpleRequiredFieldValidator<>(TestEntity.FIELD_1, "error", true);
        assertThat(validator.requiredField(), is(TestEntity.FIELD_1));
    }

    @Test
    public void returnErrorCodeTest() {
        RequiredFieldValidator<TestEntity, String> validator = new SimpleRequiredFieldValidator<>(TestEntity.FIELD_1, "error", true);
        assertThat(validator.getErrorCode(), is("error"));
    }

    @Test
    public void alwaysRunTest() {
        RequiredFieldValidator<TestEntity, String> validator = new SimpleRequiredFieldValidator<>(TestEntity.FIELD_1, "error", true);
        assertTrue(validator.requireWhen().test(new CurrentEntityMutableState()));
    }

    @Test
    public void fieldsToFetchTest() {
        RequiredFieldValidator<TestEntity, String> validator = new SimpleRequiredFieldValidator<>(TestEntity.FIELD_1, "error", true);
        assertThat(validator.fetchFields().count(), is(0L));
    }

    @Test
    public void showStopperTest() {
        RequiredFieldValidator<TestEntity, String> validator = new SimpleRequiredFieldValidator<>(TestEntity.FIELD_1, "error", true);
        assertTrue(validator.isShowStopper());
    }
}