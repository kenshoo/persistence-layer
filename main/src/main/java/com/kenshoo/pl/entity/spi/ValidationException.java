package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.ValidationError;

/**
 * Created by yuvalr on 2/14/16.
 */
public class ValidationException extends Exception {

    private final ValidationError validationError;

    public ValidationException(ValidationError validationError) {
        this.validationError = validationError;
    }

    public ValidationError getValidationError() {
        return validationError;
    }
}
