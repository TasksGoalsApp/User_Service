package com.example.User.Service.business.Impl;

import com.example.User.Service.exception.ResourceNotFoundException;
import com.example.User.Service.repository.UserEntity;
import com.example.User.Service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class DeleteUserImplTest {
    private static final long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    private DeleteUserImpl deleteUser;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        deleteUser = new DeleteUserImpl(userRepository);

        userEntity = UserEntity.builder()
                .id(USER_ID)
                .username("hristo")
                .build();
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity));

        deleteUser.deleteUser(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(userRepository).delete(userEntity);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> deleteUser.deleteUser(USER_ID));

        assertEquals("User not found with id " + USER_ID, exception.getMessage());

        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).delete(any());
        verify(userRepository, never()).deleteById(anyLong());
    }
}
