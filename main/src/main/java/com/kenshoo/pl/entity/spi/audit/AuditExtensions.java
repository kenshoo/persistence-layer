package com.kenshoo.pl.entity.spi.audit;

import com.kenshoo.pl.entity.audit.ExternalAuditedField;

import java.util.stream.Stream;

/**
 * For a given entity type, provides various extensions to the basic audit data generated for that type.<br>
 *
 * @see com.kenshoo.pl.entity.annotation.audit.Audited
 */
public interface AuditExtensions {

    /**
     * @return fields from different entity types, which must always be added to {@link com.kenshoo.pl.entity.audit.AuditRecord}-s of the current type.<br>
     * These fields can be used (for example) to filter / group audit records in queries later on.
     */
    default Stream<? extends ExternalAuditedField<?, ?>> externalMandatoryFields() { return Stream.empty(); }

    /**
     * @return a custom formatter for the field values that will be included in the audit record.<br>
     * Defaults to {@link DefaultAuditFieldValueFormatter}.
     */
    default AuditFieldValueFormatter fieldValueFormatter() { return DefaultAuditFieldValueFormatter.INSTANCE; }


    /**
     * Empty implementation in case no extensions are needed
     */
    final class EmptyAuditExtensions implements AuditExtensions {}
}
