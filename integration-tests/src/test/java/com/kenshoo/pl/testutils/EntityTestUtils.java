package com.kenshoo.pl.testutils;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Identifier;

import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public final class EntityTestUtils {

    public static <E extends EntityType<E>> void assertFetchedEntity(
        final Map<Identifier<E>, Entity> actualKeyToEntity,
        final Identifier<E> expectedKey,
        final Collection<? extends EntityField<?, ?>> expectedFields) {

        assertThat("There should be an entity with key '" + expectedKey + " but none found",
                   actualKeyToEntity.containsKey(expectedKey), notNullValue());
        final Entity actualEntity = actualKeyToEntity.get(expectedKey);

        expectedFields.forEach(field -> assertThat("The fetched entity should contain the field " + field,
                                                   actualEntity.containsField(field), is(true)));
    }

    private EntityTestUtils() {
        // utility class
    }
}
