package com.erbol.ems.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ORGANIZER")
public class Organizer extends User {

    // We will add @OneToMany List<Event> events here
    // when the Event entity is introduced.

    protected Organizer() {
        super();
    }

    public Organizer(String fullName, String email, String passwordHash) {
        super(fullName, email, passwordHash);
    }

    @Override
    public UserType getUserType() {
        return UserType.ORGANIZER;
    }
}
