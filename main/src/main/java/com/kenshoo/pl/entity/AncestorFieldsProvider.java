package com.kenshoo.pl.entity;

import java.util.stream.Stream;

/**
 * Provides additional fields from ancestor entities that should be added to an {@link AuditRecord} of a given entity type.<br>
 * These fields can be used to filter / group the audit records in queries.<br>
 * @see com.kenshoo.pl.entity.annotation.Audited
 */
public interface AncestorFieldsProvider {

    /**
     * @return ancestor fields which should be added to an {@link AuditRecord}
     */
    Stream<? extends EntityField<?, ?>> getFields();


    /**
     * Empty implementation in case no ancestor fields are needed
     */
    final class EmptyAncestorFieldsProvider implements AncestorFieldsProvider {

        @Override
        public Stream<? extends EntityField<?, ?>> getFields() {
            return Stream.empty();
        }
    }
}
