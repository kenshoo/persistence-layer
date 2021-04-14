package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class AuditRecordFieldRecordExistsMatcher extends TypeSafeMatcher<AuditRecord> {

    private final String expectedField;

    AuditRecordFieldRecordExistsMatcher(final String expectedField) {
        this.expectedField = expectedField;
    }

    @Override
    protected boolean matchesSafely(final AuditRecord actualAuditRecord) {
        return hasFieldRecordFor(actualAuditRecord, expectedField);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an AuditRecord with a field record for " + expectedField);
    }


    private boolean hasFieldRecordFor(final AuditRecord auditRecord, final String field) {
        return auditRecord.getFieldRecords().stream()
                          .anyMatch(fieldRecord -> fieldRecord.getField().equals(field));
    }
}
