package com.erbol.ems.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ATTENDEE")
public class Attendee extends User {

    // We will add @OneToMany List<Ticket> tickets here
    // when the Ticket entity is introduced.

    protected Attendee() {
        super();
    }

    public Attendee(String fullName, String email, String passwordHash) {
        super(fullName, email, passwordHash);
    }

    @Override
    public UserType getUserType() {
        return UserType.ATTENDEE;
    }
}
