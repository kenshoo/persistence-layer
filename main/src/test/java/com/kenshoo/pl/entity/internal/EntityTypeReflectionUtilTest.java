package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.TestEntityWithTransient;
import com.kenshoo.pl.entity.TestEntityWithTransient.DummyAnnotation;
import com.kenshoo.pl.entity.TestPrivateFieldsEntity;
import com.kenshoo.pl.entity.annotation.Id;
import org.junit.Test;

import static com.kenshoo.pl.entity.TestEntityWithTransient.TRANSIENT_1;
import static com.kenshoo.pl.entity.TestEntityWithTransient.TRANSIENT_2;
import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.isAnnotatedWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class EntityTypeReflectionUtilTest {

    @Test
    public void getFieldAnnotationOfPrivateFieldReturnsNonNull() {
        final var entity = TestPrivateFieldsEntity.INSTANCE;
        final var actualAnnotation = EntityTypeReflectionUtil.getFieldAnnotation(entity, entity.getID(), Id.class);

        assertThat(actualAnnotation, notNullValue());
    }

    @Test
    public void isAnnotatedWithForTransientPropertyShouldReturnTrueIfAnnotated() {
        assertThat(isAnnotatedWith(TestEntityWithTransient.INSTANCE,
                        DummyAnnotation.class,
                        TRANSIENT_1),
                is(true));
    }

    @Test
    public void isAnnotatedWithForTransientPropertyShouldReturnFalseIfNotAnnotated() {
        assertThat(isAnnotatedWith(TestEntityWithTransient.INSTANCE,
                        DummyAnnotation.class,
                        TRANSIENT_2),
                is(false));
    }
}