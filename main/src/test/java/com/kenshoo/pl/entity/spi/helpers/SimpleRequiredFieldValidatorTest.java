package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.CurrentEntityMutableState;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.TestEntity;
import com.kenshoo.pl.entity.spi.RequiredFieldValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SimpleRequiredFieldValidatorTest {

    @Mock
    private EntityChange<TestEntity> entityChange;

    @Test
    public void returnRequiredFieldTest() {
        RequiredFieldValidator<TestEntity, String> validator = new SimpleRequiredFieldValidator<>(TestEntity.FIELD_1, "error");
        assertThat(validator.requiredField(), is(TestEntity.FIELD_1));
    }

    @Test
    public void returnErrorCodeTest() {
        RequiredFieldValidator<TestEntity, String> validator = new SimpleRequiredFieldValidator<>(TestEntity.FIELD_1, "error");
        assertThat(validator.getErrorCode(), is("error"));
    }

    @Test
    public void alwaysRunTest() {
        RequiredFieldValidator<TestEntity, String> validator = new SimpleRequiredFieldValidator<>(TestEntity.FIELD_1, "error");
        assertTrue(validator.requireWhen().test(new CurrentEntityMutableState(), entityChange));
    }

    @Test
    public void fieldsToFetchTest() {
        RequiredFieldValidator<TestEntity, String> validator = new SimpleRequiredFieldValidator<>(TestEntity.FIELD_1, "error");
        assertThat(validator.fetchFields().count(), is(0L));
    }
}