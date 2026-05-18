package com.erbol.ems.common.exception;

/**
 * Thrown when a domain rule is violated — for example, an invalid
 * state transition or a precondition that the caller failed to satisfy.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}