package com.company.component.auth.core;

import com.company.component.auth.properties.AuthProperties;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "01234567890123456789012345678901234567890123456789012";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        AuthProperties properties = new AuthProperties();
        properties.setJwtSecret(SECRET);
        properties.setExpireMinutes(60);
        jwtService = new JwtService(properties, List.of());
    }

    @Test
    void createAndParseToken() {
        String token = jwtService.createToken("alice", 100L);
        JwtClaims claims = jwtService.parseToken(token);
        assertThat(claims.username()).isEqualTo("alice");
        assertThat(claims.userId()).isEqualTo(100L);
    }

    @Test
    void parseInvalidTokenFails() {
        assertThatThrownBy(() -> jwtService.parseToken("invalid.token.value"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsShortSecret() {
        AuthProperties properties = new AuthProperties();
        properties.setJwtSecret("short");
        assertThatThrownBy(() -> new JwtService(properties, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
