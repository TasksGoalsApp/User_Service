package com.example.User.Service.security;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordEncoderConfigTest {
    private final PasswordEncoderConfig config = new PasswordEncoderConfig();

    @Test
    void shouldCreateBCryptPasswordEncoder() {
        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    void shouldEncodeAndMatchPassword() {
        PasswordEncoder encoder = config.passwordEncoder();

        String rawPassword = "StrongPassword123!";
        String encodedPassword = encoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
        assertFalse(encoder.matches("wrong-password", encodedPassword));
    }


}
