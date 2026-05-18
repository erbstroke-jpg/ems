package com.erbol.ems.user;

import org.springframework.stereotype.Component;

/**
 * Factory for creating User instances based on UserType.
 *
 * <p>This is an application of the Factory Method pattern. It centralizes
 * the logic of choosing which concrete User subclass to instantiate,
 * keeping the rest of the application decoupled from the concrete types.
 *
 * <p>Administrators cannot be created through this factory — they must
 * be provisioned via the data seeder or by another administrator through
 * the admin panel.
 */
@Component
public class UserFactory {

    /**
     * Create a concrete User instance for the given type.
     *
     * @param type         the desired user type
     * @param fullName     user's full name
     * @param email        user's email (must be unique, checked at service level)
     * @param passwordHash already-hashed password (hashing is service's concern)
     * @return a concrete subclass of User
     * @throws IllegalArgumentException if type is ADMIN
     */
    public User create(UserType type, String fullName, String email, String passwordHash) {
        if (type == null) {
            throw new IllegalArgumentException("UserType must not be null");
        }
        return switch (type) {
            case ORGANIZER -> new Organizer(fullName, email, passwordHash);
            case ATTENDEE -> new Attendee(fullName, email, passwordHash);
            case ADMIN -> throw new IllegalArgumentException(
                    "Administrators cannot be created through public registration"
            );
        };
    }
}