package com.kenshoo.pl.entity.annotation.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation which cam be placed on a {@link com.kenshoo.pl.entity.TransientEntityProperty}.<br>
 * It indicates that <i>if</i> all of the following are true:
 * <ul>
 *     <li>The owning entity type is marked {@code @Audited}</li>
 *     <li>There is a command including changes for the entity type</li>
 *     <li>The same command also has a value for the transient property</li>
 * </ul>
 * --&gt; <i>then</i> the transient property will be added to the mandatory fields of the corresponding {@code AuditRecord}
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditDescription {
}
