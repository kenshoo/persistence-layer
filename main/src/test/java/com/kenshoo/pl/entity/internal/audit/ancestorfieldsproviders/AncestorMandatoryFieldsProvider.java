package com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import com.kenshoo.pl.entity.spi.audit.MandatoryFieldsProvider;

import java.util.stream.Stream;

public class AncestorMandatoryFieldsProvider implements MandatoryFieldsProvider {

    @Override
    public Stream<? extends EntityField<?, ?>> getFields() {
        return Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC);
    }
}
