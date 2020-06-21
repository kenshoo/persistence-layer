package com.kenshoo.pl.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class EntityTest {

    private static final String DUMMY_FIELD_VALUE = "abc";

    @SuppressWarnings("unchecked")
    @Mock
    final EntityField<TestEntity, String> mockField = mock(EntityField.class);

    @Test
    public void testGetOptional_WhenNotNull_ShouldReturnIt() {
        final Entity entity = new Entity() {
            public boolean containsField(EntityField<?, ?> field) {
                return true;
            }
            @SuppressWarnings("unchecked")
            public <T> T get(EntityField<?, T> field) {
                return (T) DUMMY_FIELD_VALUE;
            }
        };

        assertThat(entity.getOptional(mockField), isPresentAndIs(DUMMY_FIELD_VALUE));
    }

    @Test
    public void testGetOptional_WhenExistsAndNull_ShouldReturnEmpty() {
        final Entity entity = new Entity() {
            public boolean containsField(EntityField<?, ?> field) {
                return true;
            }
            public <T> T get(EntityField<?, T> field) {
                return null;
            }
        };

        assertThat(entity.getOptional(mockField), isEmpty());
    }

    @Test
    public void testGetOptional_WhenDoesntExists_ShouldReturnEmpty() {
        final Entity entity = new Entity() {
            public boolean containsField(EntityField<?, ?> field) {
                return false;
            }
            public <T> T get(EntityField<?, T> field) {
                return null;
            }
        };

        assertThat(entity.getOptional(mockField), isEmpty());
    }
}