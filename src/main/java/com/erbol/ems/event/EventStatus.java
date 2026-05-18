package com.erbol.ems.event;

public enum EventStatus {
    DRAFT,
    PUBLISHED,
    CANCELLED;

    public boolean isPubliclyVisible() {
        return this == PUBLISHED;
    }

    public boolean isFinal() {
        return this == CANCELLED;
    }
}