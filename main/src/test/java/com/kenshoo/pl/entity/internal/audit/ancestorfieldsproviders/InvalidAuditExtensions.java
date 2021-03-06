package com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders;

import com.kenshoo.pl.entity.audit.ExternalAuditedField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;

import java.util.stream.Stream;

public class InvalidAuditExtensions implements AuditExtensions {

    public InvalidAuditExtensions(final String ignored) {
        // Dummy non-default ctor. to make the reflection fail when trying to instantiate this class
    }

    @Override
    public Stream<? extends ExternalAuditedField<?, ?>> externalMandatoryFields() {
        return Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                     .map(ExternalAuditedField::new);
    }
}
