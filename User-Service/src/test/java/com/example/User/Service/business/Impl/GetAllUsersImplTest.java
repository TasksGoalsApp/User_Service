package com.example.User.Service.business.Impl;

import com.example.User.Service.domain.GetAllUsersResponse;
import com.example.User.Service.domain.User;
import com.example.User.Service.repository.UserEntity;
import com.example.User.Service.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class GetAllUsersImplTest {
    @Mock
    private UserRepository userRepository;

    private GetAllUsersImpl getAllUsers;

    @BeforeEach
    void setUp() {
        getAllUsers = new GetAllUsersImpl(userRepository);
    }

    @Test
    void shouldReturnAllUsers() {
        UserEntity firstUser = UserEntity.builder()
                .id(1L)
                .name("Hristo Kolev")
                .username("hristo")
                .email("hristo@example.com")
                .dateofbirth(LocalDate.of(2000, 1, 1))
                .password("encoded-password-1")
                .build();

        UserEntity secondUser = UserEntity.builder()
                .id(2L)
                .name("John Smith")
                .username("john")
                .email("john@example.com")
                .dateofbirth(LocalDate.of(1998, 5, 20))
                .password("encoded-password-2")
                .build();

        when(userRepository.findAll())
                .thenReturn(List.of(firstUser, secondUser));

        GetAllUsersResponse response = getAllUsers.getAllUsers();

        assertNotNull(response);
        assertNotNull(response.getUsers());
        assertEquals(2, response.getUsers().size());

        User firstResult = response.getUsers().get(0);

        assertEquals(1L, firstResult.getId());
        assertEquals("Hristo Kolev", firstResult.getName());
        assertEquals("hristo", firstResult.getUsername());
        assertEquals("hristo@example.com", firstResult.getEmail());
        assertEquals(
                LocalDate.of(2000, 1, 1),
                firstResult.getDateofbirth()
        );

        User secondResult = response.getUsers().get(1);

        assertEquals(2L, secondResult.getId());
        assertEquals("John Smith", secondResult.getName());
        assertEquals("john", secondResult.getUsername());
        assertEquals("john@example.com", secondResult.getEmail());
        assertEquals(
                LocalDate.of(1998, 5, 20),
                secondResult.getDateofbirth()
        );

        verify(userRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of());

        GetAllUsersResponse response = getAllUsers.getAllUsers();

        assertNotNull(response);
        assertNotNull(response.getUsers());
        assertTrue(response.getUsers().isEmpty());

        verify(userRepository).findAll();
    }

    @Test
    void shouldNotExposePasswordsInReturnedUsers() {
        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .name("Hristo Kolev")
                .username("hristo")
                .email("hristo@example.com")
                .dateofbirth(LocalDate.of(2000, 1, 1))
                .password("sensitive-encoded-password")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(userEntity));

        GetAllUsersResponse response = getAllUsers.getAllUsers();

        assertEquals(1, response.getUsers().size());

        User returnedUser = response.getUsers().get(0);

        assertEquals("hristo", returnedUser.getUsername());
        assertEquals("hristo@example.com", returnedUser.getEmail());

        // The User domain model has no password field,
        // so repository passwords cannot be included in the response.
        verify(userRepository).findAll();
    }

    @Test
    void shouldPreserveRepositoryOrder() {
        UserEntity firstUser = UserEntity.builder()
                .id(2L)
                .username("second")
                .build();

        UserEntity secondUser = UserEntity.builder()
                .id(1L)
                .username("first")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(firstUser, secondUser));

        GetAllUsersResponse response = getAllUsers.getAllUsers();

        assertEquals(2L, response.getUsers().get(0).getId());
        assertEquals("second", response.getUsers().get(0).getUsername());

        assertEquals(1L, response.getUsers().get(1).getId());
        assertEquals("first", response.getUsers().get(1).getUsername());

        verify(userRepository).findAll();
    }
}
