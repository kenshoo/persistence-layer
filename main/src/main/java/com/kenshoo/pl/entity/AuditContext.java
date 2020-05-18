package com.kenshoo.pl.entity;

import java.util.stream.Stream;

/**
 * A context which provides additional data that should be added to an {@link AuditRecord}.<br>
 * This data could be used as a way of tagging/grouping the audit records for queries later on.<br>
 * @see com.kenshoo.pl.entity.annotation.Audited
 */
public interface AuditContext {

    /**
     * @return additional fields (not necessarily from the same entity type) which should be added to an {@link AuditRecord},
     * to be used for tagging / grouping the records
     */
    Stream<? extends EntityField<?, ?>> getTagFields();


    /**
     * Empty implementation in case no additional data is needed
     */
    final class EmptyAuditContext implements AuditContext {

        @Override
        public Stream<? extends EntityField<?, ?>> getTagFields() {
            return Stream.empty();
        }
    }
}
