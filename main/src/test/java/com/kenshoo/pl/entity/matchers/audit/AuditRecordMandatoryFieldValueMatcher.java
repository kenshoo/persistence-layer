package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static java.util.Objects.requireNonNull;

class AuditRecordMandatoryFieldValueMatcher extends TypeSafeMatcher<AuditRecord<?>> {

    private final EntityField<?, ?> expectedField;
    private final Object expectedValue;

    AuditRecordMandatoryFieldValueMatcher(final EntityField<?, ?> expectedField, final Object expectedValue) {
        this.expectedField = requireNonNull(expectedField, "expectedField is required");
        this.expectedValue = requireNonNull(expectedValue, "expectedValue is required");
    }

    @Override
    protected boolean matchesSafely(final AuditRecord<?> actualRecord) {
        if (actualRecord == null) {
            return false;
        }
        return actualRecord.getMandatoryFieldValues().stream()
                           .filter(fieldValue -> expectedField.equals(fieldValue.getField()))
                           .anyMatch(fieldValue -> expectedValue.equals(fieldValue.getValue()));
    }

    @Override
    public void describeTo(final Description description) {
        description.appendValue("an AuditRecord with a mandatory field " + expectedField + " having the value: " + expectedValue);
    }

}
