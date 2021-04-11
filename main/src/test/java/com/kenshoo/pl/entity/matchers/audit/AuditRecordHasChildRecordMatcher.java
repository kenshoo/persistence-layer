package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

class AuditRecordHasChildRecordMatcher extends TypeSafeMatcher<AuditRecord<?>> {

    private final Matcher<AuditRecord<?>> childRecordMatcher;

    AuditRecordHasChildRecordMatcher(final Matcher<AuditRecord<?>> childRecordMatcher) {
        this.childRecordMatcher = childRecordMatcher;
    }

    @Override
    protected boolean matchesSafely(final AuditRecord<?> actualAuditRecord) {
        return actualAuditRecord.getChildRecords().stream()
                                .anyMatch(childRecordMatcher::matches);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an AuditRecord with a child record that: " + childRecordMatcher);
    }
}
