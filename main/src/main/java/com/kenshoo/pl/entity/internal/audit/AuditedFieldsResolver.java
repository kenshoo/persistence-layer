package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityType;

import java.util.Optional;

public interface AuditedFieldsResolver {

    <E extends EntityType<E>> Optional<? extends AuditedFieldSet<E>> resolve(E entityType);
}
