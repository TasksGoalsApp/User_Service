package com.example.User.Service.security;

import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JwtDecoderConfigTest {
    @Test
    void shouldCreateJwtDecoderFromBase64Secret() {
        SecretKey secretKey = Keys.hmacShaKeyFor(
                "01234567890123456789012345678901".getBytes()
        );

        String base64Secret =
                Encoders.BASE64.encode(secretKey.getEncoded());

        JwtDecoderConfig config = new JwtDecoderConfig();

        ReflectionTestUtils.setField(
                config,
                "base64Secret",
                base64Secret
        );

        JwtDecoder decoder = config.jwtDecoder();

        assertNotNull(decoder);
    }
}
