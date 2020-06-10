package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityFieldValue;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;
import org.hamcrest.Matcher;

public class AuditRecordMatchers {

    public static <E extends EntityType<E>> Matcher<AuditRecord<E>> hasEntityType(final E expectedEntityType) {
        return new AuditRecordEntityTypeMatcher<>(expectedEntityType);
    }

    public static Matcher<AuditRecord<?>> hasEntityId(final String expectedEntityId) {
        return new AuditRecordEntityIdMatcher(expectedEntityId);
    }

    public static Matcher<AuditRecord<?>> hasOperator(final ChangeOperation expectedOperator) {
        return new AuditRecordOperatorMatcher(expectedOperator);
    }

    public static Matcher<AuditRecord<?>> hasMandatoryFieldValue(final EntityField<?, ?> field, final Object value) {
        return new AuditRecordMandatoryFieldValueMatcher(new EntityFieldValue(field, value));
    }

    public static Matcher<AuditRecord<?>> hasNoMandatoryFieldValues() {
        return new AuditRecordNoMandatoryFieldsMatcher();
    }

    public static <E extends EntityType<E>> Matcher<AuditRecord<E>> hasCreatedFieldRecord(final EntityField<E, ?> field, final Object value) {
        return hasFieldRecord(new FieldAuditRecord<>(field, null, value));
    }

    public static <E extends EntityType<E>> Matcher<AuditRecord<E>> hasChangedFieldRecord(final EntityField<E, ?> field,
                                                                                          final String oldValue,
                                                                                          final String newValue) {
        return hasFieldRecord(new FieldAuditRecord<>(field, oldValue, newValue));
    }

    public static <E extends EntityType<E>> Matcher<AuditRecord<E>> hasDeletedFieldRecord(final EntityField<E, ?> field, final Object value) {
        return hasFieldRecord(new FieldAuditRecord<>(field, value, null));
    }

    public static <E extends EntityType<E>> Matcher<AuditRecord<E>> hasFieldRecordFor(final EntityField<E, ?> expectedField) {
        return new AuditRecordFieldRecordExistsMatcher<>(expectedField);
    }

    public static Matcher<AuditRecord<?>> hasNoFieldRecords() {
        return new AuditRecordNoFieldRecordsMatcher();
    }

    public static Matcher<AuditRecord<?>> hasSameChildRecord(final AuditRecord<?> expectedChildRecord) {
        return new AuditRecordSameChildRecordMatcher(expectedChildRecord);
    }

    public static <C extends EntityType<C>> Matcher<AuditRecord<?>> hasChildRecordThat(final Matcher<AuditRecord<C>> childRecordMatcher) {
        return new AuditRecordHasChildRecordMatcher<>(childRecordMatcher);
    }

    public static Matcher<AuditRecord<?>> hasNoChildRecords() {
        return new AuditRecordNoChildRecordsMatcher();
    }

    private static <E extends EntityType<E>> Matcher<AuditRecord<E>> hasFieldRecord(final FieldAuditRecord<E> expectedFieldRecord) {
        return new AuditRecordFieldRecordMatcher<>(expectedFieldRecord);
    }

}
