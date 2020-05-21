package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class AuditRecordNoChildRecordsMatcher extends TypeSafeMatcher<AuditRecord<?>> {

    @Override
    protected boolean matchesSafely(final AuditRecord<?> actualAuditRecord) {
        return actualAuditRecord.getChildRecords().isEmpty();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an AuditRecord with NO child records");
    }
}
