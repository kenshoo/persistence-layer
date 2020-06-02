package com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import com.kenshoo.pl.entity.spi.audit.MandatoryFieldsProvider;

import java.util.stream.Stream;

public class InvalidMandatoryFieldsProvider implements MandatoryFieldsProvider {

    public InvalidMandatoryFieldsProvider(final String ignored) {
        // Dummy non-default ctor. to make the reflection fail when trying to instantiate this class
    }

    @Override
    public Stream<? extends EntityField<?, ?>> getFields() {
        return Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC);
    }
}
