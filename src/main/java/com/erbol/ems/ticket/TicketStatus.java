package com.erbol.ems.ticket;

public enum TicketStatus {
    ACTIVE,
    CANCELLED,
    USED;

    public boolean isActive() {
        return this == ACTIVE;
    }
}