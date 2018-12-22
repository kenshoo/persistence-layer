package com.kenshoo.pl.entity.spi;

/**
 * Should be thrown by a supplier if it has reached the decision that the current value should not be changed.
 */
public class NotSuppliedException extends RuntimeException {
    public NotSuppliedException() {
        // Performance optimization - suppress stack creation.
        // This exception is always caught by framework and no stack is needed.
        super("Value is not supplied", null, false, false);
    }
}
