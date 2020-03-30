package com.kenshoo.pl.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Whenever an entity or field has this annotation, it indicates that any changes to the entity / field
 * will be included in the changes published by the changelog output generator.
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangesLogged {
}
