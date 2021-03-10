package com.kenshoo.pl.entity;

import java.util.Collections;
import java.util.Map;

public class ValidationError {

    private final String errorCode;
    private final EntityField<?, ?> field;
    private final Map<String, String> parameters;
    private final boolean showStopper;

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
        this(errorCode, field, parameters, false);
    }

    public ValidationError(String errorCode, EntityField<?, ?> field, Map<String, String> parameters, boolean showStopper) {
        this.errorCode = errorCode;
        this.field = field;
        this.parameters = parameters;
        this.showStopper = showStopper;
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

    public boolean isShowStopper() {
        return showStopper;
    }

    @Override
    public String toString() {
        return new org.apache.commons.lang3.builder.ToStringBuilder(this)
                .append("errorCode", errorCode)
                .append("errorField", field)
                .append("parameters", parameters)
                .append("showStopper", showStopper)
                .toString();
    }
}
