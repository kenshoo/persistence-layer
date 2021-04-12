package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;
import org.hamcrest.Matcher;

import static java.util.Map.entry;

public class AuditRecordMatchers {

    public static Matcher<AuditRecord> hasEntityType(final String expectedEntityType) {
        return new AuditRecordEntityTypeMatcher(expectedEntityType);
    }

    public static Matcher<AuditRecord> hasEntityId(final String expectedEntityId) {
        return new AuditRecordEntityIdMatcher(expectedEntityId);
    }

    public static Matcher<AuditRecord> hasOperator(final ChangeOperation expectedOperator) {
        return new AuditRecordOperatorMatcher(expectedOperator);
    }

    public static Matcher<AuditRecord> hasMandatoryFieldValue(final EntityField<?, ?> field, final Object value) {
        return new AuditRecordMandatoryFieldValueMatcher(entry(field.toString(), value));
    }

    public static Matcher<AuditRecord> hasNoMandatoryFieldValues() {
        return new AuditRecordNoMandatoryFieldsMatcher();
    }

    public static Matcher<AuditRecord> hasCreatedFieldRecord(final EntityField<?, ?> field, final Object value) {
        return hasFieldRecord(FieldAuditRecord.builder(field)
                                              .newValue(value)
                                              .build());
    }

    public static Matcher<AuditRecord> hasCreatedFieldRecord(final String fieldName, final Object value) {
        return hasFieldRecord(FieldAuditRecord.builder(fieldName)
                                              .newValue(value)
                                              .build());
    }

    public static Matcher<AuditRecord> hasChangedFieldRecord(final EntityField<?, ?> field,
                                                             final String oldValue,
                                                             final String newValue) {
        return hasFieldRecord(FieldAuditRecord.builder(field)
                                              .oldValue(oldValue)
                                              .newValue(newValue)
                                              .build());
    }

    public static  Matcher<AuditRecord> hasDeletedFieldRecord(final EntityField<?, ?> field, final Object value) {
        return hasFieldRecord(FieldAuditRecord.builder(field.toString())
                                              .oldValue(value)
                                              .build());
    }

    public static Matcher<AuditRecord> hasFieldRecordFor(final EntityField<?, ?> expectedField) {
        return new AuditRecordFieldRecordExistsMatcher(expectedField.toString());
    }

    public static Matcher<AuditRecord> hasNoFieldRecords() {
        return new AuditRecordNoFieldRecordsMatcher();
    }

    public static Matcher<AuditRecord> hasSameChildRecord(final AuditRecord expectedChildRecord) {
        return new AuditRecordSameChildRecordMatcher(expectedChildRecord);
    }

    public static Matcher<AuditRecord> hasChildRecordThat(final Matcher<AuditRecord> childRecordMatcher) {
        return new AuditRecordHasChildRecordMatcher(childRecordMatcher);
    }

    public static Matcher<AuditRecord> hasNoChildRecords() {
        return new AuditRecordNoChildRecordsMatcher();
    }

    private static Matcher<AuditRecord> hasFieldRecord(final FieldAuditRecord expectedFieldRecord) {
        return new AuditRecordFieldRecordMatcher(expectedFieldRecord);
    }

}
