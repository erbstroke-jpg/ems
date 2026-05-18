package com.erbol.ems.user;

import com.erbol.ems.user.dto.UserRegisterDto;

/**
 * Service abstraction for user-related operations.
 *
 * <p>Controllers depend on this interface, not the implementation.
 * This is the Dependency Inversion principle: high-level modules
 * (controllers) do not depend on low-level modules (concrete services),
 * both depend on this abstraction.
 */
public interface UserService {

    /**
     * Register a new user (Organizer or Attendee) from the public form.
     *
     * @param dto the registration data, already validated by the controller
     * @return the persisted user (with generated id)
     * @throws com.erbol.ems.common.exception.EmailAlreadyUsedException
     *         if a user with the same email already exists
     * @throws IllegalArgumentException if dto.role is ADMIN
     */
    User register(UserRegisterDto dto);
}