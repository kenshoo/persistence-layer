package com.kenshoo.pl.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Whenever a field has this annotation, it indicates that any changes to the field
 * will NOT be part of the changes published for the owning entity by the changelog output generator.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangesNotLogged {
}
