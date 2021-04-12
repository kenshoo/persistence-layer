package com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders;

import com.kenshoo.pl.entity.audit.ExternalAuditedField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;

import java.util.stream.Stream;

public class AncestorAuditExtensions implements AuditExtensions {

    @Override
    public Stream<? extends ExternalAuditedField<?, ?>> externalMandatoryFields() {
        return Stream.of(NotAuditedAncestorType.NAME,
                         NotAuditedAncestorType.DESC,
                         NotAuditedAncestorType.AMOUNT,
                         NotAuditedAncestorType.AMOUNT2)
                     .map(ExternalAuditedField::new);
    }
}
