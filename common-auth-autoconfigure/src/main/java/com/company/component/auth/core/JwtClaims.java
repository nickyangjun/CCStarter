package com.company.component.auth.core;

/**
 * 从 JWT 解析出的主体信息。
 */
public record JwtClaims(String username, Long userId) {
}
