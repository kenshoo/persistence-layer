package com.kenshoo.matcher;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityFieldValue;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Collection;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public class EntityHasFieldValuesMatcher extends TypeSafeMatcher<Entity> {

    private final Collection<EntityFieldValue> expectedFieldValues;
    private String mismatchesMessage;

    private EntityHasFieldValuesMatcher(final Collection<EntityFieldValue> expectedFieldValues) {
        this.expectedFieldValues = expectedFieldValues;
        mismatchesMessage = StringUtils.EMPTY;
    }

    @Override
    protected boolean matchesSafely(final Entity actualEntity) {
        mismatchesMessage = expectedFieldValues.stream()
                                               .map(fieldValue -> validateField(actualEntity, fieldValue))
                                               .collect(joining("\n"));

        return StringUtils.isBlank(mismatchesMessage);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an entity with the following field values: " + expectedFieldValues);
    }

    @Override
    protected void describeMismatchSafely(Entity item, Description mismatchDescription) {
        mismatchDescription.appendText("the following mismatches occurred:\n" + mismatchesMessage);
    }

    private String validateField(final Entity actualEntity, final EntityFieldValue fieldValue) {
        if (!actualEntity.containsField(fieldValue.getField())) {
            return "\t\tMissing expected field '" + fieldValue.getFieldName() + "'";
        }
        final Object actualValue = actualEntity.get(fieldValue.getField());
        if (!Objects.equals(actualValue, fieldValue.getValue())) {
            return "\t\tIncorrect value for field " + fieldValue.getFieldName() + ": '" + actualValue + "'";
        }
        return StringUtils.EMPTY;
    }

    public static EntityHasFieldValuesMatcher hasFieldValues(final EntityFieldValue... expectedFieldValues) {
        return new EntityHasFieldValuesMatcher(ImmutableSet.copyOf(expectedFieldValues));
    }

    public static EntityFieldValue fieldValue(final EntityField<?, ?> entityField, final Object value) {
        return new EntityFieldValue(entityField, value);
    }
}
