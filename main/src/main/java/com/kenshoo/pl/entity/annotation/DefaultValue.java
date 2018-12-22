package com.kenshoo.pl.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Placed on an EntityField that gets a default value at creation time if not specified explicitly by the client.
 * Analogous to the DEFAULT clause in database.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultValue {
    /**
     * @return default value as string, will be parsed using the StringValueConverter of the field
     */
    String value();
}
