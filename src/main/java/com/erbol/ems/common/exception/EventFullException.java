package com.erbol.ems.common.exception;

public class EventFullException extends BusinessRuleException {
    public EventFullException(Long eventId) {
        super("Event " + eventId + " is sold out");
    }
}