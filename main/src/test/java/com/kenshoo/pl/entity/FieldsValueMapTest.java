package com.kenshoo.pl.entity;

import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class FieldsValueMapTest {

    private static final String DUMMY_VALUE = "abc";
    private static final boolean PRESENT = true;
    private static final boolean ABSENT = false;

    @SuppressWarnings("unchecked")
    @Mock
    final EntityField<TestEntity, String> mockField = mock(EntityField.class);

    @Test
    public void safeGet_WhenNotNull_ShouldReturnIt() {

        assertThat(new StubFieldsValueMap<TestEntity>(PRESENT, DUMMY_VALUE).safeGet(mockField),
                   is(Triptional.of(DUMMY_VALUE)));
    }

    @Test
    public void safeGet_WhenPresentAndNull_ShouldReturnNull() {
        assertThat(new StubFieldsValueMap<TestEntity>(PRESENT).safeGet(mockField),
                   is(Triptional.nullInstance()));
    }

    @Test
    public void safeGet_WhenDoesntExist_ShouldReturnAbsent() {
        assertThat(new StubFieldsValueMap<TestEntity>(ABSENT).safeGet(mockField),
                   is(Triptional.absent()));
    }

    private static final class StubFieldsValueMap<E extends EntityType<E>> implements FieldsValueMap<E> {

        private final boolean fieldPresent;
        private final Object value;

        private StubFieldsValueMap(final boolean fieldPresent) {
            this(fieldPresent, null);
        }

        private StubFieldsValueMap(final boolean fieldPresent, final Object value) {
            this.fieldPresent = fieldPresent;
            this.value = value;
        }

        @Override
        public <T> boolean containsField(final EntityField<E, T> field) {
            return fieldPresent;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(final EntityField<E, T> field) {
            return (T)value;
        }
    }
}