package com.erbol.ems.user;

public enum UserType {
    ADMIN,
    ORGANIZER,
    ATTENDEE;

    public String authorityName() {
        return "ROLE_" + name();
    }
}