package com.kenshoo.pl.entity.annotation.audit;

import com.kenshoo.pl.entity.audit.AuditTrigger;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions.EmptyAuditExtensions;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter.MissingAuditFieldValueFormatter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_CREATE_OR_UPDATE;

/**
 * Whenever an entity or field has this annotation, it indicates that any changes to the entity / field
 * will be published (by the publisher belonging to the PersistenceLayer instance).<br>
 *     When the entity-level is annotated, it implies that all fields should also be annotated unless overriden by {@link NotAudited} on the field.
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /**
     * @return the name to use for the annotated type (entity or field) when audited.<br>
     * If empty or missing, will default to the name already defined for this type, as follows:
     * <ul>
     *     <li>Entity: {@code EntityType.getName()}</li>
     *     <li>Field: The result of {@code toString()} on the field, which retrieves the name by reflection</li>
     * </ul>
     */
    String name() default "";

    /**
     * @return the formatter to use for field values when they are audited.
     * <ul>
     * <li>If defined at the entity level, will apply to all fields in the entity (unless overriden).
     * <li>If defined at the field level, will apply to that field while overriding any definition (if exists) at the entity level
     * <li>If missing at both levels, will default to {@link com.kenshoo.pl.entity.spi.audit.DefaultAuditFieldValueFormatter}
     * </ul>
     */
    Class<? extends AuditFieldValueFormatter> valueFormatter() default MissingAuditFieldValueFormatter.class;

    /**
     * @return the rule by which to trigger auditing for the annotated entity type or field.<br>
     * This attribute is valid for <b>field-level annotations only</b>, and will be ignored if appearing on entities.<br>
     * For the entity-level,{@link AuditTrigger#ON_CREATE_OR_UPDATE} is implied always.
     */
    AuditTrigger trigger() default ON_CREATE_OR_UPDATE;

    /**
     * <b>NOTE</b>: This attribute is valid for entity-level annotations only, and will be ignored if appearing on fields.
     * @return extensions to the basic audit data that will be generated for the annotated entity type.<br>
     * This attribute is valid for <b>entity-level annotations only</b>, and will be ignored if appearing on fields.
     * @see AuditExtensions
     */
    Class<? extends AuditExtensions> extensions() default EmptyAuditExtensions.class;
}
