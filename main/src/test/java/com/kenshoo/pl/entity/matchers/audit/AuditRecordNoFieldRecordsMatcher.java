package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class AuditRecordNoFieldRecordsMatcher extends TypeSafeMatcher<AuditRecord<?>> {

    @Override
    protected boolean matchesSafely(final AuditRecord<?> actualAuditRecord) {
        return actualAuditRecord.getFieldRecords().isEmpty();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an AuditRecord with NO field records");
    }
}
