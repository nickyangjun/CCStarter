package com.company.component.auth.login.core;

import java.util.Map;

public record LoginPrincipal(String username, Long userId, Map<String, Object> attributes) {

    public LoginPrincipal(String username, Long userId) {
        this(username, userId, Map.of());
    }
}
