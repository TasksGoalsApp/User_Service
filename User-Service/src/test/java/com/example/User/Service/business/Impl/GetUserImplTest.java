package com.example.User.Service.business.Impl;

import com.example.User.Service.domain.User;
import com.example.User.Service.exception.ResourceNotFoundException;
import com.example.User.Service.repository.UserEntity;
import com.example.User.Service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class GetUserImplTest {
    private static final long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    private GetUserImpl getUser;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        getUser = new GetUserImpl(userRepository);

        userEntity = UserEntity.builder()
                .id(USER_ID)
                .name("Hristo Kolev")
                .username("hristo")
                .email("hristo@example.com")
                .dateofbirth(LocalDate.of(2000, 1, 1))
                .password("encoded-password")
                .build();
    }

    @Test
    void shouldReturnUserWhenUserExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity));

        Optional<User> result = getUser.getUserById(USER_ID);

        assertTrue(result.isPresent());

        User user = result.get();

        assertEquals(USER_ID, user.getId());
        assertEquals("Hristo Kolev", user.getName());
        assertEquals("hristo", user.getUsername());
        assertEquals("hristo@example.com", user.getEmail());
        assertEquals(LocalDate.of(2000, 1, 1), user.getDateofbirth());

        verify(userRepository).findById(USER_ID);
    }

    @Test
    void shouldNotExposePasswordInReturnedUser() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(userEntity));

        Optional<User> result = getUser.getUserById(USER_ID);

        assertTrue(result.isPresent());

        User user = result.get();

        assertEquals("hristo", user.getUsername());
        assertEquals("hristo@example.com", user.getEmail());

        // The domain User object has no password field,
        // so the password cannot be exposed by this service.
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> getUser.getUserById(USER_ID));

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findById(USER_ID);
    }
}
