package com.example.User.Service.business;

import com.example.User.Service.business.Impl.UpdateUserInfoImpl;
import com.example.User.Service.domain.UpdateUserInfoRequest;
import com.example.User.Service.domain.UpdateUserInfoResponse;
import com.example.User.Service.exception.ResourceNotFoundException;
import com.example.User.Service.repository.UserEntity;
import com.example.User.Service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class UpdateUserInfoImplTest {
    private static final long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    private UpdateUserInfoImpl updateUserInfo;

    private UpdateUserInfoRequest request;
    private UserEntity existingUser;

    @BeforeEach
    void setUp() {
        updateUserInfo = new UpdateUserInfoImpl(userRepository);

        request = UpdateUserInfoRequest.builder()
                .name("Hristo Updated")
                .username("hristo.updated")
                .email("updated@example.com")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .build();

        existingUser = UserEntity.builder()
                .id(USER_ID)
                .name("Hristo Kolev")
                .username("hristo")
                .email("hristo@example.com")
                .dateofbirth(LocalDate.of(2000, 2, 2))
                .password("encoded-password")
                .build();
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        when(userRepository.findByUsername("hristo.updated")).thenReturn(Optional.empty());

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserInfoResponse response = updateUserInfo.updateUserInfo(request, USER_ID);

        assertNotNull(response);
        assertEquals(USER_ID, response.getId());
        assertEquals("Hristo Updated", response.getName());
        assertEquals("hristo.updated", response.getUsername());
        assertEquals("updated@example.com", response.getEmail());
        assertEquals(LocalDate.of(2000, 1, 1), response.getDateOfBirth());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        verify(userRepository).save(userCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();

        assertEquals(USER_ID, savedUser.getId());
        assertEquals("Hristo Updated", savedUser.getName());
        assertEquals("hristo.updated", savedUser.getUsername());
        assertEquals("updated@example.com", savedUser.getEmail());
        assertEquals(LocalDate.of(2000, 1, 1), savedUser.getDateofbirth());

        // Fields that are not editable must remain unchanged.
        assertEquals("encoded-password", savedUser.getPassword());
    }

    @Test
    void shouldAllowUserToKeepCurrentUsername() {
        request.setUsername("hristo");

        when(userRepository.findByUsername("hristo")).thenReturn(Optional.of(existingUser));

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserInfoResponse response = updateUserInfo.updateUserInfo(request, USER_ID);

        assertNotNull(response);
        assertEquals("hristo", response.getUsername());

        verify(userRepository).save(existingUser);
    }

    @Test
    void shouldThrowExceptionWhenUsernameBelongsToAnotherUser() {
        UserEntity anotherUser = UserEntity.builder()
                .id(2L)
                .username("hristo.updated")
                .build();

        when(userRepository.findByUsername("hristo.updated")).thenReturn(Optional.of(anotherUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> updateUserInfo.updateUserInfo(request, USER_ID));

        assertEquals("Username 'hristo.updated' is already in use", exception.getMessage());

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findByUsername("hristo.updated")).thenReturn(Optional.empty());

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> updateUserInfo.updateUserInfo(request, USER_ID));

        assertEquals("User not found with id " + USER_ID, exception.getMessage());

        verify(userRepository, never()).save(any());
    }


    @Test
    void shouldThrowExceptionWhenEmailBelongsToAnotherUser() {
        UserEntity anotherUser = UserEntity.builder()
                .id(2L)
                .email("updated@example.com")
                .build();

        when(userRepository.findByUsername("hristo.updated"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("updated@example.com"))
                .thenReturn(Optional.of(anotherUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateUserInfo.updateUserInfo(request, USER_ID)
        );

        assertEquals("Email is already in use", exception.getMessage());

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any());
    }
}
