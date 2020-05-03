package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.AuditRecord;
import com.kenshoo.pl.entity.EntityType;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

class AuditRecordHasChildRecordMatcher<C extends EntityType<C>> extends TypeSafeMatcher<AuditRecord<?>> {

    private final Matcher<? extends AuditRecord<C>> childRecordMatcher;

    AuditRecordHasChildRecordMatcher(final Matcher<? extends AuditRecord<C>> childRecordMatcher) {
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
