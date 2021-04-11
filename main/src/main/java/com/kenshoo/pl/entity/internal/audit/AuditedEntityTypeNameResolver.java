package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class AuditedEntityTypeNameResolver {

    static final AuditedEntityTypeNameResolver INSTANCE = new AuditedEntityTypeNameResolver();

    public String resolve(final EntityType<?> entityType) {
        requireNonNull(entityType, "entityType is required");

        return Optional.ofNullable(entityType.getClass().getAnnotation(Audited.class))
                       .map(Audited::name)
                       .filter(StringUtils::isNotBlank)
                       .orElse(entityType.getName());
    }

    private AuditedEntityTypeNameResolver() {
        // singleton
    }
}
