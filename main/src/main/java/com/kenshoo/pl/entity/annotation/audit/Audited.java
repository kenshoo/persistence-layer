package com.kenshoo.pl.entity.annotation.audit;

import com.kenshoo.pl.entity.spi.audit.AuditExtensions;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions.EmptyAuditExtensions;

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
     * @return extensions to the basic audit data that will be generated for the annotated entity type.<br>
     * This attribute is valid for entity-level annotations only, and will be ignored if appearing on fields.
     * @see AuditExtensions
     */
    Class<? extends AuditExtensions> extensions() default EmptyAuditExtensions.class;
}
