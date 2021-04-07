package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class AuditRecordFieldRecordMatcher extends TypeSafeMatcher<AuditRecord<?>> {

    private final FieldAuditRecord<?> expectedFieldRecord;

    AuditRecordFieldRecordMatcher(final FieldAuditRecord<?> expectedFieldRecord) {
        this.expectedFieldRecord = expectedFieldRecord;
    }

    @Override
    protected boolean matchesSafely(final AuditRecord<?> actualAuditRecord) {
        return actualAuditRecord.getFieldRecords().contains(expectedFieldRecord);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an AuditRecord with the field record " + expectedFieldRecord);
    }
}
