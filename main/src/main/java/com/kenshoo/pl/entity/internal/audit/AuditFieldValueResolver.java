package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.Triptional;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;

import static java.util.Objects.requireNonNull;

public class AuditFieldValueResolver {

    public static final AuditFieldValueResolver INSTANCE = new AuditFieldValueResolver();

    private final AuditFieldValueFormatter fieldValueFormatter;

    private AuditFieldValueResolver() {
        this(CompositeAuditFieldValueFormatter.INSTANCE);
    }

    @VisibleForTesting
    AuditFieldValueResolver(final AuditFieldValueFormatter fieldValueFormatter) {
        this.fieldValueFormatter = fieldValueFormatter;
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
}
