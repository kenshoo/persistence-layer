package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class AuditRecordFieldRecordMissingMatcher extends TypeSafeMatcher<AuditRecord> {

    private final String field;

    AuditRecordFieldRecordMissingMatcher(final String field) {
        this.field = field;
    }

    @Override
    protected boolean matchesSafely(final AuditRecord actualAuditRecord) {
        return hasNoFieldRecordFor(actualAuditRecord, field);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an AuditRecord WITHOUT a field record for " + field);
    }


    private boolean hasNoFieldRecordFor(final AuditRecord auditRecord, final String field) {
        return auditRecord.getFieldRecords().stream()
                          .noneMatch(fieldRecord -> fieldRecord.getField().equals(field));
    }
}
