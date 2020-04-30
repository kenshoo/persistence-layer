package com.kenshoo.pl.entity.matchers.audit;

import com.kenshoo.pl.entity.*;
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

    private static <E extends EntityType<E>> Matcher<AuditRecord<E>> hasFieldRecord(final FieldAuditRecord<E> expectedFieldRecord) {
        return new AuditRecordFieldRecordMatcher<>(expectedFieldRecord);
    }

}
