package com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders;

import com.kenshoo.pl.entity.audit.ExternalAuditedField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import com.kenshoo.pl.entity.internal.audit.formatters.CustomAuditFieldValueFormatter1;
import com.kenshoo.pl.entity.internal.audit.formatters.CustomAuditFieldValueFormatter2;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;

import java.util.stream.Stream;

public class AncestorWithFieldValueFormatterOverridesAuditExtensions implements AuditExtensions {

    @Override
    public Stream<? extends ExternalAuditedField<?, ?>> externalMandatoryFields() {
        return Stream.of(new ExternalAuditedField.Builder<>(NotAuditedAncestorType.NAME)
                             .withValueFormatter(new CustomAuditFieldValueFormatter1())
                             .build(),
                         new ExternalAuditedField.Builder<>(NotAuditedAncestorType.DESC)
                             .withValueFormatter(new CustomAuditFieldValueFormatter2())
                             .build());
    }
}
