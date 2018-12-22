package com.kenshoo.pl.entity;

import java.util.Collections;
import java.util.Map;

public class ValidationError {

    private final String errorCode;
    private final EntityField<?, ?> field;
    private final Map<String, String> parameters;

    public ValidationError(String errorCode) {
        this(errorCode, null, Collections.<String, String>emptyMap());
    }

    public ValidationError(String errorCode, EntityField<?, ?> field) {
        this(errorCode, field, Collections.<String, String>emptyMap());
    }

    public ValidationError(String errorCode, Map<String, String> parameters) {
        this(errorCode, null, parameters);
    }

    public ValidationError(String errorCode, EntityField<?, ?> field, Map<String, String> parameters) {
        this.errorCode = errorCode;
        this.field = field;
        this.parameters = parameters;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public EntityField<?, ?> getField() {
        return field;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return new org.apache.commons.lang3.builder.ToStringBuilder(this)
                .append("errorCode", errorCode)
                .append("errorField", field)
                .append("parameters", parameters)
                .toString();
    }
}
