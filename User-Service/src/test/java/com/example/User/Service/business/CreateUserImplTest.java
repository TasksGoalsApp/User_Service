package com.example.User.Service.business;

import com.example.User.Service.business.Impl.CreateUserImpl;
import com.example.User.Service.domain.CreateUserRequest;
import com.example.User.Service.domain.CreateUserResponse;
import com.example.User.Service.repository.Role;
import com.example.User.Service.repository.UserEntity;
import com.example.User.Service.repository.UserRepository;
import com.example.User.Service.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateUserImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CreateUserImpl createUser;

    private CreateUserRequest request;

    @BeforeEach
    void setUp() {
        createUser = new CreateUserImpl(
                userRepository,
                passwordEncoder
        );

        request = CreateUserRequest.builder()
                .name("Hristo Kolev")
                .username("hristo")
                .email("hristo@example.com")
                .password("password123")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .build();
    }

    @Test
    void shouldCreateUserSuccessfully() {
        when(userRepository.findByUsername("hristo"))
                .thenReturn(Optional.empty());

        when(userRepository.existsByEmail("hristo@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        CreateUserResponse response = createUser.createUser(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());

        ArgumentCaptor<UserEntity> captor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(userRepository).save(captor.capture());

        UserEntity savedUser = captor.getValue();

        assertEquals("Hristo Kolev", savedUser.getName());
        assertEquals("hristo", savedUser.getUsername());
        assertEquals("hristo@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(LocalDate.of(2000, 1, 1), savedUser.getDateofbirth());

        assertNotNull(savedUser.getRole());
        assertEquals(Role.CUSTOMER, savedUser.getRole().getRole());
        assertSame(savedUser, savedUser.getRole().getUser());

        verify(passwordEncoder).encode("password123");
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        when(userRepository.findByUsername("hristo"))
                .thenReturn(Optional.of(new UserEntity()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createUser.createUser(request)
        );

        assertEquals(
                "Username 'hristo' is already in use",
                exception.getMessage()
        );

        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.findByUsername("hristo"))
                .thenReturn(Optional.empty());

        when(userRepository.existsByEmail("hristo@example.com"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createUser.createUser(request)
        );

        assertEquals("Email is already in use", exception.getMessage());

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }
}
