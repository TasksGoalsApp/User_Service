package com.example.User.Service.security;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SecurityConfigTest.TestController.class)
@Import({SecurityConfig.class, SecurityConfigTest.TestController.class})
public class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private JwtUtil jwtUtil;

//    SecurityConfigTest(MockMvc mockMvc) {
//        this.mockMvc = mockMvc;
//    }

    @Test
    void shouldAllowRegistrationWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("register"));
    }

    @Test
    void shouldAllowLoginWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("login"));
    }

    @Test
    void shouldRejectAnonymousRequestToProtectedEndpoint()
            throws Exception {

        mockMvc.perform(get("/secured"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedJwtRequest()
            throws Exception {

        mockMvc.perform(get("/secured")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().string("secured"));
    }


    @Test
    void shouldConfigureCors() {
        SecurityConfig securityConfig = new SecurityConfig();

        CorsConfigurationSource source =
                securityConfig.corsConfigurationSource();

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/secured");

        CorsConfiguration configuration =
                source.getCorsConfiguration(request);

        assertNotNull(configuration);

        assertEquals(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                ),
                configuration.getAllowedMethods()
        );

        assertEquals(
                List.of("*"),
                configuration.getAllowedHeaders()
        );

        assertEquals(
                Boolean.TRUE,
                configuration.getAllowCredentials()
        );
    }

    @Test
    void shouldReturnAuthenticationManagerFromConfiguration()
            throws Exception {

        SecurityConfig securityConfig = new SecurityConfig();

        AuthenticationConfiguration authenticationConfiguration =
                mock(AuthenticationConfiguration.class);

        AuthenticationManager authenticationManager =
                mock(AuthenticationManager.class);

        when(authenticationConfiguration.getAuthenticationManager())
                .thenReturn(authenticationManager);

        AuthenticationManager result =
                securityConfig.authenticationManager(
                        authenticationConfiguration
                );

        assertSame(authenticationManager, result);

        verify(authenticationConfiguration)
                .getAuthenticationManager();
    }

    @RestController
    static class TestController {

        @PostMapping("/user/register")
        String register() {
            return "register";
        }

        @PostMapping("/user/login")
        String login() {
            return "login";
        }

        @GetMapping("/secured")
        String secured() {
            return "secured";
        }

        @PreAuthorize("hasRole('Admin')")
        @GetMapping("/admin")
        String admin() {
            return "admin";
        }
    }

}
