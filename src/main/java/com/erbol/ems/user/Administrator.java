package com.erbol.ems.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Administrator extends User {

    protected Administrator() {
        super();
    }

    public Administrator(String fullName, String email, String passwordHash) {
        super(fullName, email, passwordHash);
    }

    @Override
    public UserType getUserType() {
        return UserType.ADMIN;
    }
}
