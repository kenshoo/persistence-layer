package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.getFieldAnnotation;
import static java.util.Objects.requireNonNull;

public class AuditFieldNameResolver {

    static final AuditFieldNameResolver INSTANCE = new AuditFieldNameResolver();

    public <E extends EntityType<E>> String resolve(final EntityField<E, ?> field) {
        requireNonNull(field, "field is required");

        return Optional.ofNullable(getFieldAnnotation(field.getEntityType(), field, Audited.class))
                       .map(Audited::name)
                       .filter(StringUtils::isNotBlank)
                       .orElse(field.toString());
    }

    private AuditFieldNameResolver() {
        // singleton
    }
}
