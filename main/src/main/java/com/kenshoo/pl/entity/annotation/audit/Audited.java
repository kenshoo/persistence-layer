package com.kenshoo.pl.entity.annotation.audit;

import com.kenshoo.pl.entity.audit.AuditTrigger;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions.EmptyAuditExtensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_CHANGE;

/**
 * Whenever an entity or field has this annotation, it indicates that any changes to the entity / field
 * will be published (by the publisher belonging to the PersistenceLayer instance)
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /**
     * @return the rule by which to trigger auditing for the annotated entity type or field.<br>
     *     If the entity type is annotated, it sets the default for all fields in that entity type.
     */
    AuditTrigger trigger() default ON_CHANGE;

    /**
     * <b>NOTE</b>: This attribute is valid for entity-level annotations only, and will be ignored if appearing on fields.
     * @return extensions to the basic audit data that will be generated for the annotated entity type.<br>
     * This attribute is valid for entity-level annotations only, and will be ignored if appearing on fields.
     * @see AuditExtensions
     */
    Class<? extends AuditExtensions> extensions() default EmptyAuditExtensions.class;
}
