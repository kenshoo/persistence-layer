package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

class AuditRecordEntityIdMatcher extends TypeSafeMatcher<AuditRecord> {

    private final String expectedEntityId;

    AuditRecordEntityIdMatcher(final String expectedEntityId) {
        this.expectedEntityId = requireNonNull(expectedEntityId, "There must be an expected entity id");
    }

    @Override
    protected boolean matchesSafely(final AuditRecord actualRecord) {
        if (actualRecord == null) {
            return false;
        }
        return Objects.equals(actualRecord.getEntityId(), expectedEntityId);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendValue("an AuditRecord with entity id " + expectedEntityId);
    }
}
