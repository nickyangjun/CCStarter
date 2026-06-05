package com.company.component.auth.login.core;

import com.company.component.auth.core.JwtService;
import com.company.component.auth.properties.AuthProperties;

import java.util.LinkedHashMap;
import java.util.Map;

public class LoginTokenIssuer {

    private final JwtService jwtService;
    private final AuthProperties authProperties;

    public LoginTokenIssuer(JwtService jwtService, AuthProperties authProperties) {
        this.jwtService = jwtService;
        this.authProperties = authProperties;
    }

    public Map<String, Object> issueTokenResponse(LoginPrincipal principal) {
        String token = jwtService.createToken(principal.username(), principal.userId());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("accessToken", token);
        body.put("tokenType", "Bearer");
        body.put("expireMinutes", authProperties.getExpireMinutes());
        body.put("userId", principal.userId());
        body.put("username", principal.username());
        return body;
    }
}
