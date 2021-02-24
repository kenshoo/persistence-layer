package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.TestPrivateFieldsEntity;
import com.kenshoo.pl.entity.annotation.Id;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class EntityTypeReflectionUtilTest {

    @Test
    public void getFieldAnnotationOfPrivateFieldReturnsNonNull() {
        final var entity = TestPrivateFieldsEntity.INSTANCE;
        final var actualAnnotation = EntityTypeReflectionUtil.getFieldAnnotation(entity, entity.getID(), Id.class);

        assertThat(actualAnnotation, notNullValue());
    }
}