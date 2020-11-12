package com.kenshoo.pl.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class EntityTest {

    private static final String DUMMY_VALUE = "abc";

    @SuppressWarnings("unchecked")
    @Mock
    final EntityField<TestEntity, String> mockField = mock(EntityField.class);

    @Test
    public void safeGet_WhenNotNull_ShouldReturnIt() {
        final CurrentEntityState currentState = new StubEntity(true, DUMMY_VALUE);

        assertThat(currentState.safeGet(mockField), is(Triptional.of(DUMMY_VALUE)));
    }

    @Test
    public void safeGet_WhenPresentAndNull_ShouldReturnNull() {
        final CurrentEntityState currentState = new StubEntity(true, null);

        assertThat(currentState.safeGet(mockField), is(Triptional.nullInstance()));
    }

    @Test
    public void safeGet_WhenAbsent_ShouldReturnAbsent() {
        final CurrentEntityState currentState = new StubEntity(false, null);

        assertThat(currentState.safeGet(mockField), is(Triptional.absent()));
    }

    private static final class StubEntity implements CurrentEntityState {

        private final boolean fieldPresent;
        private final Object value;

        private StubEntity(final boolean fieldPresent, final Object value) {
            this.fieldPresent = fieldPresent;
            this.value = value;
        }

        @Override
        public boolean containsField(final EntityField<?, ?> field) {
            return fieldPresent;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(final EntityField<?, T> field) {
            return (T)value;
        }
    }
}