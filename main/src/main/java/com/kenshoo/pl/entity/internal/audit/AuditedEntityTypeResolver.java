package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityType;

import java.util.Optional;

public interface AuditedEntityTypeResolver {

    <E extends EntityType<E>> Optional<AuditedEntityType<E>> resolve(final E entityType);

    AuditedEntityTypeResolver EMPTY = new AuditedEntityTypeResolver() {
        @Override
        public <E extends EntityType<E>> Optional<AuditedEntityType<E>> resolve(E entityType) {
            return Optional.empty();
        }
    };
}
