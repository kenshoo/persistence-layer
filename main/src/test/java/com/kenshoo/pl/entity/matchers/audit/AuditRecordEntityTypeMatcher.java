package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.AuditRecord;
import com.kenshoo.pl.entity.EntityType;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

class AuditRecordEntityTypeMatcher<E extends EntityType<E>> extends TypeSafeMatcher<AuditRecord<E>> {

    private final E expectedEntityType;

    AuditRecordEntityTypeMatcher(final E expectedEntityType) {
        this.expectedEntityType = requireNonNull(expectedEntityType, "There must be an expected entity type");
    }

    @Override
    protected boolean matchesSafely(final AuditRecord<E> actualRecord) {
        if (actualRecord == null) {
            return false;
        }
        return Objects.equals(actualRecord.getEntityType(), expectedEntityType);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendValue("an AuditRecord with entity type " + expectedEntityType.getName());
    }
}
