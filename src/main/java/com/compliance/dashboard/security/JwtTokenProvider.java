package com.compliance.dashboard.security;

import com.compliance.dashboard.config.ApplicationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtTokenProvider(ApplicationProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = properties.jwt().expirationMillis();
    }

    public String generateToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .subject(principal.email())
                .claim("userId", principal.id())
                .claim("role", principal.role().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        parseClaims(token);
        return true;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
