package com.kenshoo.pl.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates the field that can be used as unique identifier. For a specific entity, only one field can be annotated
 * with <code>@Id</code> and it has to be either <code>Integer</code> or <code>Long</code>. This field is be used
 * by audit log to reference the entity. If no field is annotated with <code>@Id</code>, audit log is not written for this entity.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
    IdGeneration value() default IdGeneration.Manual;
}
