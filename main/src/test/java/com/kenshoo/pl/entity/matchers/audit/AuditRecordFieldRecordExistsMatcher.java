package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class AuditRecordFieldRecordExistsMatcher extends TypeSafeMatcher<AuditRecord<?>> {

    private final EntityField<?, ?> expectedField;

    AuditRecordFieldRecordExistsMatcher(final EntityField<?, ?> expectedField) {
        this.expectedField = expectedField;
    }

    @Override
    protected boolean matchesSafely(final AuditRecord<?> actualAuditRecord) {
        return hasFieldRecordFor(actualAuditRecord, expectedField);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an AuditRecord with a field record for " + expectedField);
    }


    private boolean hasFieldRecordFor(final AuditRecord<?> auditRecord, final EntityField<?, ?> field) {
        return auditRecord.getFieldRecords().stream()
                          .anyMatch(fieldRecord -> fieldRecord.getField().equals(field));
    }
}
