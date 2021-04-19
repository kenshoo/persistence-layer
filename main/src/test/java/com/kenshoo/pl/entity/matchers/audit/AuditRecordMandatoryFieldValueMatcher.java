package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.FieldValue;
import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static java.util.Objects.requireNonNull;

class AuditRecordMandatoryFieldValueMatcher extends TypeSafeMatcher<AuditRecord> {

    private final FieldValue expectedFieldValue;

    AuditRecordMandatoryFieldValueMatcher(final FieldValue expectedFieldValue) {
        this.expectedFieldValue = requireNonNull(expectedFieldValue, "expectedFieldValue is required");
    }

    @Override
    protected boolean matchesSafely(final AuditRecord actualRecord) {
        if (actualRecord == null) {
            return false;
        }
        return actualRecord.getMandatoryFieldValues().stream()
                           .anyMatch(expectedFieldValue::equals);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendValue("an AuditRecord with a mandatory field and value: " + expectedFieldValue);
    }
}
