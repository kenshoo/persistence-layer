package com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;

import java.util.stream.Stream;

public class AncestorAuditExtensions implements AuditExtensions {

    @Override
    public Stream<? extends EntityField<?, ?>> externalMandatoryFields() {
        return Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC);
    }
}
