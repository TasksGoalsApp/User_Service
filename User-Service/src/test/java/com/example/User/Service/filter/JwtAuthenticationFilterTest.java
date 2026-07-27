package com.example.User.Service.filter;

import com.example.User.Service.security.JwtUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;




@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderIsMissing() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderIsNotBearer() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Basic credentials"
        );

        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid()
            throws Exception {

        String token = "valid-token";
        String username = "hristo";

        UserDetails userDetails = User
                .withUsername(username)
                .password("encoded-password")
                .authorities("ROLE_CUSTOMER")
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractUsername(token)).thenReturn(username);

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication = SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(username, authentication.getName());
        assertEquals(userDetails, authentication.getPrincipal());

        assertTrue(authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_CUSTOMER"))
        );

        verify(jwtUtil).extractUsername(token);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtUtil).validateToken(token, userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateUserWhenTokenIsInvalid() throws Exception {

        String token = "invalid-token";
        String username = "hristo";

        UserDetails userDetails = User
                .withUsername(username)
                .password("encoded-password")
                .authorities("ROLE_CUSTOMER")
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractUsername(token)).thenReturn(username);

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        when(jwtUtil.validateToken(token, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(jwtUtil).extractUsername(token);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtUtil).validateToken(token, userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenUsernameCannotBeExtracted() throws Exception {

        String token = "token-without-username";

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractUsername(token)).thenReturn(null);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(jwtUtil).extractUsername(token);
        verifyNoInteractions(userDetailsService);
        verify(jwtUtil, never()).validateToken(anyString(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotReplaceExistingAuthentication() throws Exception {

        String token = "valid-token";
        String username = "hristo";

        UserDetails existingUser = User
                .withUsername("existing-user")
                .password("encoded-password")
                .authorities("ROLE_CUSTOMER")
                .build();

        Authentication existingAuthentication = new org.springframework.security.authentication
                        .UsernamePasswordAuthenticationToken(
                        existingUser,
                        null,
                        existingUser.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractUsername(token)).thenReturn(username);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication result = SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertSame(existingAuthentication, result);
        assertEquals("existing-user", result.getName());

        verify(jwtUtil).extractUsername(token);
        verifyNoInteractions(userDetailsService);
        verify(jwtUtil, never()).validateToken(anyString(), any());
        verify(filterChain).doFilter(request, response);
    }

}
