package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.audit.AuditRecord;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

class AuditRecordOperatorMatcher extends TypeSafeMatcher<AuditRecord<?>> {

    private final ChangeOperation expectedOperator;

    AuditRecordOperatorMatcher(final ChangeOperation expectedOperator) {
        this.expectedOperator = requireNonNull(expectedOperator, "There must be an expected operator");
    }

    @Override
    protected boolean matchesSafely(final AuditRecord<?> actualRecord) {
        if (actualRecord == null) {
            return false;
        }
        return Objects.equals(actualRecord.getOperator(), expectedOperator);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendValue("an AuditRecord with operator " + expectedOperator);
    }
}
