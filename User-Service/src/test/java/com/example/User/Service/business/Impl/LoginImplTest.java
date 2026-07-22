package com.example.User.Service.business.Impl;

import com.example.User.Service.domain.LoginRequest;
import com.example.User.Service.domain.LoginResponse;
import com.example.User.Service.repository.Role;
import com.example.User.Service.repository.UserEntity;
import com.example.User.Service.repository.UserRepository;
import com.example.User.Service.repository.UserRoleEntity;
import com.example.User.Service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
public class LoginImplTest {
    private static final String USERNAME = "hristo";
    private static final String RAW_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String TOKEN = "generated-jwt-token";
    private static final long USER_ID = 1L;
    private static final long EXPIRATION_MS = 3_600_000L;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    private LoginImpl login;

    private LoginRequest request;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        login = new LoginImpl(
                passwordEncoder,
                userRepository,
                authenticationManager,
                jwtUtil
        );

        this.request = LoginRequest.builder()
                .username(USERNAME)
                .password(RAW_PASSWORD)
                .build();


        user = UserEntity.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .build();

        UserRoleEntity userRole = UserRoleEntity.builder()
                .id(1L)
                .role(Role.CUSTOMER)
                .user(user)
                .build();

        user.setRole(userRole);
    }

    @Test
    void shouldLoginSuccessfully() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        when(jwtUtil.generateToken(USERNAME, USER_ID, "CUSTOMER")).thenReturn(TOKEN);

        when(jwtUtil.getExpirationMs()).thenReturn(EXPIRATION_MS);

        LoginResponse response = login.login(request);

        assertNotNull(response);
        assertEquals(TOKEN, response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(EXPIRATION_MS, response.getExpiresIn());
        assertNull(response.getIdToken());

        verify(userRepository).findByUsername(USERNAME);

        verify(passwordEncoder).matches(RAW_PASSWORD, ENCODED_PASSWORD);

        verify(authenticationManager).authenticate(argThat(authentication ->
                        authentication instanceof UsernamePasswordAuthenticationToken
                                && USERNAME.equals(authentication.getPrincipal())
                                && RAW_PASSWORD.equals(authentication.getCredentials())
                )
        );

        verify(jwtUtil).generateToken(USERNAME, USER_ID, "CUSTOMER");

        verify(jwtUtil).getExpirationMs();
    }

    @Test
    void shouldThrowBadCredentialsWhenUsernameDoesNotExist() {
        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> login.login(request)
        );

        assertEquals("Bad credentials", exception.getMessage());

        verify(userRepository).findByUsername(USERNAME);
        verifyNoInteractions(passwordEncoder, authenticationManager, jwtUtil);
    }

    @Test
    void shouldThrowBadCredentialsWhenPasswordDoesNotMatch() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> login.login(request));

        assertEquals("Bad credentials", exception.getMessage());

        verify(passwordEncoder).matches(RAW_PASSWORD, ENCODED_PASSWORD);

        verifyNoInteractions(authenticationManager, jwtUtil);
    }

    @Test
    void shouldPropagateBadCredentialsFromAuthenticationManager() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> login.login(request));

        assertEquals("Bad credentials", exception.getMessage());

        verify(jwtUtil, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void shouldGenerateTokenWithAdminRole() {
        user.getRole().setRole(Role.ADMIN);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        when(jwtUtil.generateToken(USERNAME, USER_ID, "ADMIN")).thenReturn(TOKEN);

        when(jwtUtil.getExpirationMs()).thenReturn(EXPIRATION_MS);

        LoginResponse response = login.login(request);

        assertEquals(TOKEN, response.getAccessToken());

        verify(jwtUtil).generateToken(USERNAME, USER_ID,"ADMIN" );
    }
}
