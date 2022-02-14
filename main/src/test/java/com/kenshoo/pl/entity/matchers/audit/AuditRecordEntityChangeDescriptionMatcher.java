package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static java.util.Optional.ofNullable;

class AuditRecordEntityChangeDescriptionMatcher extends TypeSafeMatcher<AuditRecord> {

    private final String expectedEntityChangeDescription;

    AuditRecordEntityChangeDescriptionMatcher(final String expectedEntityChangeDescription) {
        this.expectedEntityChangeDescription = expectedEntityChangeDescription;
    }

    @Override
    protected boolean matchesSafely(final AuditRecord actualRecord) {
        if (actualRecord == null) {
            return false;
        }
        return actualRecord.getEntityChangeDescription().equals(ofNullable(expectedEntityChangeDescription));
    }

    @Override
    public void describeTo(final Description description) {
        description.appendValue("an AuditRecord with entity change description " + expectedEntityChangeDescription);
    }
}
