package com.example.User.Service.security;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expirationMs;
    private final String issuer;
    private final String audience;

    public JwtUtil(@Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs,
            @Value("${jwt.issuer}") String issuer, @Value("${jwt.audience}") String audience) {
        if (expirationMs < 1000) throw new IllegalArgumentException("JWT expiration must be at least one second");
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
        this.issuer = issuer;
        this.audience = audience;
    }

    public long getExpiresInSeconds() { return expirationMs / 1000; }

    public String generateToken(String username, Long userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder().subject(username).issuer(issuer).audience().add(audience).and()
                .claim("id", userId).claim("roles", List.of(role))
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(getExpiresInSeconds())))
                .signWith(key, Jwts.SIG.HS256).compact();
    }
}
