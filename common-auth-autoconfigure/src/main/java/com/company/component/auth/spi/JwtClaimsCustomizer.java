package com.company.component.auth.spi;

import io.jsonwebtoken.JwtBuilder;

import java.util.Map;

/**
 * 签发 JWT 时扩展 Claims。
 */
public interface JwtClaimsCustomizer {

    void customize(JwtBuilder builder, Map<String, Object> context);
}
