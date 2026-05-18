package com.erbol.ems;

import com.erbol.ems.common.exception.EmailAlreadyUsedException;
import com.erbol.ems.user.*;
import com.erbol.ems.user.dto.UserRegisterDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    private UserFactory userFactory;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        userService = new UserServiceImpl(userRepository, userFactory, passwordEncoder);
    }

    @Test
    @DisplayName("registers a new ATTENDEE and saves it")
    void registersNewAttendee() {
        UserRegisterDto dto = UserRegisterDto.builder()
                .fullName("Jane Tester")
                .email("Jane@Example.COM")
                .password("Password1")
                .role(UserType.ATTENDEE)
                .build();

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register(dto);

        assertThat(saved).isInstanceOf(Attendee.class);
        assertThat(saved.getEmail()).isEqualTo("jane@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("rejects registration if email already in use")
    void rejectsDuplicateEmail() {
        UserRegisterDto dto = UserRegisterDto.builder()
                .fullName("Jane")
                .email("jane@example.com")
                .password("Password1")
                .role(UserType.ATTENDEE)
                .build();

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("forbids creating an admin through public registration")
    void forbidsAdminRegistration() {
        UserRegisterDto dto = UserRegisterDto.builder()
                .fullName("Eve")
                .email("eve@example.com")
                .password("Password1")
                .role(UserType.ADMIN)
                .build();

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any(User.class));
    }
}