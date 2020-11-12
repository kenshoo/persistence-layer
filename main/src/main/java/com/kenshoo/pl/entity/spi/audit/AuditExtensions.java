package com.kenshoo.pl.entity.spi.audit;

import com.kenshoo.pl.entity.EntityField;

import java.util.stream.Stream;

/**
 * For a given entity type, provides various extensions to the basic audit data generated for that type.<br>
 * @see com.kenshoo.pl.entity.annotation.audit.Audited
 */
public interface AuditExtensions {

    /**
     * @return fields from different entity types, which must always be added to {@link com.kenshoo.pl.entity.audit.AuditRecord}-s of the current type.<br>
     *     These fields can be used (for example) to filter / group audit records in queries later on.
     */
    Stream<? extends EntityField<?, ?>> externalMandatoryFields();


    /**
     * Empty implementation in case no extensions are needed
     */
    final class EmptyAuditExtensions implements AuditExtensions {

        @Override
        public Stream<? extends EntityField<?, ?>> externalMandatoryFields() {
            return Stream.empty();
        }
    }
}
