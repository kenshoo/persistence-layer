package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.internal.audit.AuditedField;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static java.util.Objects.requireNonNull;

public class AuditedFieldMatcher extends TypeSafeMatcher<AuditedField<?, ?>> {

    private final AuditedField<?, ?> expectedAuditedField;
    
    AuditedFieldMatcher(final AuditedField<?, ?> expectedAuditedField) {
        this.expectedAuditedField = requireNonNull(expectedAuditedField, "There must be an expected audited field");
    }

    @Override
    protected boolean matchesSafely(final AuditedField<?, ?> actualAuditedField) {
        if (actualAuditedField == null) {
            return false;
        }
        return new EqualsBuilder()
            .append(actualAuditedField.getField(), expectedAuditedField.getField())
            .append(actualAuditedField.getName(), expectedAuditedField.getName())
            .append(actualAuditedField.getValueFormatter().getClass(), expectedAuditedField.getValueFormatter().getClass())
            .isEquals();
    }

    @Override
    public void describeTo(final Description description) {
        description.appendValue("audited field: " + expectedAuditedField);
    }
    
    public static Matcher<AuditedField<?, ?>> eqAuditedField(final AuditedField<?, ?> auditedField) {
        return new AuditedFieldMatcher(auditedField);
    }
}
