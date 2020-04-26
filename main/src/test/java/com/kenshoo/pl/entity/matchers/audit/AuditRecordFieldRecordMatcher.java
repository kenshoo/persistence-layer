package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.AuditRecord;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldAuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class AuditRecordFieldRecordMatcher<E extends EntityType<E>> extends TypeSafeMatcher<AuditRecord<E>> {

    private final FieldAuditRecord<E> expectedFieldRecord;

    AuditRecordFieldRecordMatcher(final FieldAuditRecord<E> expectedFieldRecord) {
        this.expectedFieldRecord = expectedFieldRecord;
    }

    @Override
    protected boolean matchesSafely(final AuditRecord<E> actualAuditRecord) {
        return actualAuditRecord.hasFieldRecord(expectedFieldRecord);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an AuditRecord with the field record " + expectedFieldRecord);
    }
}
