package com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import com.kenshoo.pl.entity.spi.audit.AlwaysAuditedFieldsProvider;

import java.util.stream.Stream;

public class InvalidAuditedFieldsProvider implements AlwaysAuditedFieldsProvider {

    public InvalidAuditedFieldsProvider(final String ignored) {
        // Dummy non-default ctor. to make the reflection fail when trying to instantiate this class
    }

    @Override
    public Stream<? extends EntityField<?, ?>> getFields() {
        return Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC);
    }
}
