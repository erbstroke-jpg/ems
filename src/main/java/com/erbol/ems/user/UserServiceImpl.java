package com.erbol.ems.user;

import com.erbol.ems.common.exception.EmailAlreadyUsedException;
import com.erbol.ems.user.dto.UserRegisterDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserFactory userFactory;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           UserFactory userFactory,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userFactory = userFactory;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User register(UserRegisterDto dto) {
        if (dto.getRole() == UserType.ADMIN) {
            throw new IllegalArgumentException(
                    "Administrators cannot be created through public registration");
        }

        String normalizedEmail = dto.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyUsedException(normalizedEmail);
        }

        String passwordHash = passwordEncoder.encode(dto.getPassword());

        User user = userFactory.create(
                dto.getRole(),
                dto.getFullName().trim(),
                normalizedEmail,
                passwordHash
        );

        User saved = userRepository.save(user);
        log.info("Registered new user: id={}, email={}, type={}",
                saved.getId(), saved.getEmail(), saved.getUserType());

        return saved;
    }
}