package com.example.User.Service.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



public class JwtUtilTest {
    /*
     * Base64 representation of a 32-byte secret.
     * HS256 requires a key of at least 256 bits.
     */
    private static final String BASE64_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private static final long EXPIRATION_MS = 60_000L;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(
                jwtUtil,
                "base64Secret",
                BASE64_SECRET
        );

        ReflectionTestUtils.setField(
                jwtUtil,
                "expirationMs",
                EXPIRATION_MS
        );
    }

    @Test
    void shouldGenerateTokenWithCorrectClaims() {
        String token = jwtUtil.generateToken(
                "hristo",
                1L,
                "CUSTOMER"
        );

        Claims claims = parseClaims(token);

        assertEquals("hristo", claims.getSubject());
        assertEquals(1L, claims.get("userId", Long.class));

        List<?> roles = claims.get("roles", List.class);

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("CUSTOMER", roles.get(0));

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void shouldSetCorrectExpirationTime() {
        String token = jwtUtil.generateToken(
                "hristo",
                1L,
                "CUSTOMER"
        );

        Claims claims = parseClaims(token);

        long tokenLifetime =
                claims.getExpiration().getTime()
                        - claims.getIssuedAt().getTime();

        assertEquals(EXPIRATION_MS, tokenLifetime);
    }

    @Test
    void shouldExtractUsernameFromValidToken() {
        String token = jwtUtil.generateToken(
                "hristo",
                1L,
                "CUSTOMER"
        );

        String username = jwtUtil.extractUsername(token);

        assertEquals("hristo", username);
    }

    @Test
    void shouldValidateTokenWhenUsernameMatchesAndTokenIsNotExpired() {
        String token = jwtUtil.generateToken(
                "hristo",
                1L,
                "CUSTOMER"
        );

        UserDetails userDetails = User
                .withUsername("hristo")
                .password("encoded-password")
                .authorities("ROLE_CUSTOMER")
                .build();

        boolean valid = jwtUtil.validateToken(token, userDetails);

        assertTrue(valid);
    }

    @Test
    void shouldRejectTokenWhenUsernameDoesNotMatch() {
        String token = jwtUtil.generateToken(
                "hristo",
                1L,
                "CUSTOMER"
        );

        UserDetails userDetails = User
                .withUsername("another-user")
                .password("encoded-password")
                .authorities("ROLE_CUSTOMER")
                .build();

        boolean valid = jwtUtil.validateToken(token, userDetails);

        assertFalse(valid);
    }

    @Test
    void shouldRejectExpiredToken() {
        ReflectionTestUtils.setField(
                jwtUtil,
                "expirationMs",
                -1_000L
        );

        String token = jwtUtil.generateToken(
                "hristo",
                1L,
                "CUSTOMER"
        );

        UserDetails userDetails = User
                .withUsername("hristo")
                .password("encoded-password")
                .authorities("ROLE_CUSTOMER")
                .build();

        boolean valid = jwtUtil.validateToken(token, userDetails);

        assertFalse(valid);
    }

    @Test
    void shouldRejectMalformedToken() {
        UserDetails userDetails = User
                .withUsername("hristo")
                .password("encoded-password")
                .authorities("ROLE_CUSTOMER")
                .build();

        boolean valid = jwtUtil.validateToken(
                "not-a-valid-jwt-token",
                userDetails
        );

        assertFalse(valid);
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        JwtUtil otherJwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(
                otherJwtUtil,
                "base64Secret",
                "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY="
        );

        ReflectionTestUtils.setField(
                otherJwtUtil,
                "expirationMs",
                EXPIRATION_MS
        );

        String token = otherJwtUtil.generateToken(
                "hristo",
                1L,
                "CUSTOMER"
        );

        UserDetails userDetails = User
                .withUsername("hristo")
                .password("encoded-password")
                .authorities("ROLE_CUSTOMER")
                .build();

        boolean valid = jwtUtil.validateToken(token, userDetails);

        assertFalse(valid);
    }

    @Test
    void shouldReturnConfiguredExpirationTime() {
        assertEquals(
                EXPIRATION_MS,
                jwtUtil.getExpirationMs()
        );
    }

    private Claims parseClaims(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(BASE64_SECRET);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
