package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class AuditRecordSameChildRecordMatcher extends TypeSafeMatcher<AuditRecord<?>> {

    private final AuditRecord<?> expectedChildRecord;

    AuditRecordSameChildRecordMatcher(final AuditRecord<?> expectedChildRecord) {
        this.expectedChildRecord = expectedChildRecord;
    }

    @Override
    protected boolean matchesSafely(final AuditRecord<?> actualAuditRecord) {
        return actualAuditRecord.getChildRecords().contains(expectedChildRecord);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an AuditRecord with the child record (by reference): " + expectedChildRecord);
    }
}
