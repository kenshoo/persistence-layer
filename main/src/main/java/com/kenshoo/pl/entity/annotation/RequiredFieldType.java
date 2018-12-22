package com.kenshoo.pl.entity.annotation;

public enum RequiredFieldType {
    REGULAR,
    /**
     * Indicates a field that is a relation to a parent. This field's value is used to fetch parents data before
     * calling enrichers and validators at create time.
     */
    RELATION
}
