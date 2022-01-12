package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.Triptional;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;

import static java.util.Objects.requireNonNull;

public class AuditFieldValueResolver {

    private final AuditFieldValueFormatter fieldValueFormatter;

    public AuditFieldValueResolver(final AuditFieldValueFormatter fieldValueFormatter) {
        this.fieldValueFormatter = requireNonNull(fieldValueFormatter, "fieldValueFormatter is required");
    }

    public <T> Triptional<T> resolve(final AuditedField<?, T> auditedField,
                                     final Entity entity) {
        requireNonNull(entity, "entity is required");
        return entity.safeGet(auditedField.getField());
    }

    public <T> Triptional<String> resolveToString(final AuditedField<?, T> auditedField,
                                                  final Entity entity) {

        return resolve(auditedField, entity)
            .map(value -> fieldValueFormatter.format(auditedField, value));
    }

    @VisibleForTesting
    AuditFieldValueFormatter getFieldValueFormatter() {
        return fieldValueFormatter;
    }
}
