package com.example.User.Service.security;

import com.example.User.Service.repository.Role;
import com.example.User.Service.repository.UserEntity;
import com.example.User.Service.repository.UserRepository;
import com.example.User.Service.repository.UserRoleEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        String username = "hristo";
        String password = "encoded-password";

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setRole(Role.CUSTOMER);

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(userRole);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(password, result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void shouldConvertUserRoleToGrantedAuthority() {
        String username = "admin";

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setRole(Role.ADMIN);

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword("encoded-password");
        user.setRole(userRole);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername(username);

        assertEquals(1, result.getAuthorities().size());

        assertTrue(result.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN"))
        );

        verify(userRepository).findByUsername(username);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        String username = "missing-user";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                        UsernameNotFoundException.class,
                        () -> userDetailsService
                                .loadUserByUsername(username)
                );

        assertEquals("User not found: " + username, exception.getMessage());

        verify(userRepository).findByUsername(username);
    }


}
