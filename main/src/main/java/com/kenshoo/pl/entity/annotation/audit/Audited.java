package com.kenshoo.pl.entity.annotation.audit;

import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.spi.audit.AlwaysAuditedFieldsProvider;
import com.kenshoo.pl.entity.spi.audit.AlwaysAuditedFieldsProvider.EmptyAlwaysAuditedFieldsProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Whenever an entity or field has this annotation, it indicates that any changes to the entity / field
 * will be published (by the publisher belonging to the PersistenceLayer instance)
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /**
     * @return the provider class of additional fields that should always be added to {@link AuditRecord}s of the annotated entity type,
     * with their current values.<br>
     * This attribute is valid for entity-level annotations only, and will be ignored if appearing on fields.
     * @see AlwaysAuditedFieldsProvider
     */
    Class<? extends AlwaysAuditedFieldsProvider> alwaysAuditedFieldsProvider() default EmptyAlwaysAuditedFieldsProvider.class;
}
