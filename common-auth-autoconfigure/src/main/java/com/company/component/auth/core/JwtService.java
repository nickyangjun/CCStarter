package com.company.component.auth.core;

import com.company.component.auth.properties.AuthProperties;
import com.company.component.auth.spi.JwtClaimsCustomizer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 签发与校验。
 */
public class JwtService {

    private final AuthProperties properties;
    private final SecretKey secretKey;
    private final List<JwtClaimsCustomizer> customizers;

    public JwtService(AuthProperties properties, List<JwtClaimsCustomizer> customizers) {
        validateSecret(properties.getJwtSecret());
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
        List<JwtClaimsCustomizer> ordered = customizers != null ? customizers : List.of();
        AnnotationAwareOrderComparator.sort(ordered);
        this.customizers = List.copyOf(ordered);
    }

    public String createToken(String username, Long userId) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(properties.getExpireMinutes() * 60L);
        Map<String, Object> context = new HashMap<>();
        context.put("username", username);
        if (userId != null) {
            context.put("userId", userId);
        }
        var builder = Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt));
        if (userId != null) {
            builder.claim("userId", userId);
        }
        for (JwtClaimsCustomizer customizer : customizers) {
            customizer.customize(builder, context);
        }
        return builder.signWith(secretKey).compact();
    }

    public JwtClaims parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String username = claims.getSubject();
            Long userId = null;
            Object userIdClaim = claims.get("userId");
            if (userIdClaim instanceof Number number) {
                userId = number.longValue();
            }
            if (!StringUtils.hasText(username)) {
                throw new JwtException("JWT subject is empty");
            }
            return new JwtClaims(username, userId);
        } catch (JwtException ex) {
            throw new JwtException("Invalid JWT token", ex);
        }
    }

    private static void validateSecret(String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("component.auth.jwt-secret is required when component.auth.enabled=true");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("component.auth.jwt-secret must be at least 32 bytes for HS256");
        }
    }
}
