package com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders;

import com.kenshoo.pl.entity.audit.ExternalAuditedField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;

import java.util.stream.Stream;

public class AncestorWithFieldNameOverridesAuditExtensions implements AuditExtensions {

    public static final String NAME_FIELD_NAME_OVERRIDE = "nameOverride";
    public static final String DESC_FIELD_NAME_OVERRIDE = "descOverride";

    @Override
    public Stream<? extends ExternalAuditedField<?, ?>> externalMandatoryFields() {
        return Stream.of(new ExternalAuditedField<>(NotAuditedAncestorType.NAME, NAME_FIELD_NAME_OVERRIDE),
                         new ExternalAuditedField<>(NotAuditedAncestorType.DESC, DESC_FIELD_NAME_OVERRIDE));
    }
}
