package com.kenshoo.pl.entity.spi.audit;

import com.kenshoo.pl.entity.EntityField;

import java.util.stream.Stream;

/**
 * For a given entity type, provides additional fields that should always be added to {@link com.kenshoo.pl.entity.audit.AuditRecord}-s of the given entity.<br>
 * These fields will be added with their <b>current</b> values only, and can be used as additional criteria for filtering / grouping the audit records in queries.<br>
 * @see com.kenshoo.pl.entity.annotation.audit.Audited
 */
public interface MandatoryFieldsProvider {

    /**
     * @return fields which should always be added to an {@link com.kenshoo.pl.entity.audit.AuditRecord} for a given entity type
     */
    Stream<? extends EntityField<?, ?>> getFields();


    /**
     * Empty implementation in case no additional fields are needed
     */
    final class EmptyMandatoryFieldsProvider implements MandatoryFieldsProvider {

        @Override
        public Stream<? extends EntityField<?, ?>> getFields() {
            return Stream.empty();
        }
    }
}
