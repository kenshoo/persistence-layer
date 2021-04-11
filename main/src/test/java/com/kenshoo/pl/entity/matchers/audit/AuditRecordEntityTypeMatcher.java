package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

class AuditRecordEntityTypeMatcher extends TypeSafeMatcher<AuditRecord<?>> {

    private final String expectedEntityType;

    AuditRecordEntityTypeMatcher(final String expectedEntityType) {
        this.expectedEntityType = requireNonNull(expectedEntityType, "There must be an expected entity type");
    }

    @Override
    protected boolean matchesSafely(final AuditRecord<?> actualRecord) {
        if (actualRecord == null) {
            return false;
        }
        return Objects.equals(actualRecord.getEntityType(), expectedEntityType);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendValue("an AuditRecord with entity type " + expectedEntityType);
    }
}
