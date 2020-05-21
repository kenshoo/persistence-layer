package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class AuditRecordFieldRecordExistsMatcher<E extends EntityType<E>> extends TypeSafeMatcher<AuditRecord<E>> {

    private final EntityField<E, ?> expectedField;

    AuditRecordFieldRecordExistsMatcher(final EntityField<E, ?> expectedField) {
        this.expectedField = expectedField;
    }

    @Override
    protected boolean matchesSafely(final AuditRecord<E> actualAuditRecord) {
        return hasFieldRecordFor(actualAuditRecord, expectedField);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an AuditRecord with a field record for " + expectedField);
    }


    private boolean hasFieldRecordFor(final AuditRecord<E> auditRecord, final EntityField<E, ?> field) {
        return auditRecord.getFieldRecords().stream()
                          .anyMatch(fieldRecord -> fieldRecord.getField().equals(field));
    }
}
