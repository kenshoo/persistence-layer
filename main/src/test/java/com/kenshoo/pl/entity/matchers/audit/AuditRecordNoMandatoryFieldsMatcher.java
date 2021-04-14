package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class AuditRecordNoMandatoryFieldsMatcher extends TypeSafeMatcher<AuditRecord> {

    @Override
    protected boolean matchesSafely(AuditRecord actualAuditRecord) {
        return actualAuditRecord.getMandatoryFieldValues().isEmpty();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an AuditRecord with NO mandatory fields");
    }
}
