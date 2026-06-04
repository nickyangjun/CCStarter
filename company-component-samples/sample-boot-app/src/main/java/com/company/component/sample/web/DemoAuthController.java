package com.company.component.sample.web;

import com.company.component.auth.core.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sample/auth")
public class DemoAuthController {

    private final JwtService jwtService;

    public DemoAuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/login")
    public Map<String, String> login() {
        String token = jwtService.createToken("demo-user", 1L);
        return Map.of("token", token);
    }

    @GetMapping("/me")
    public Map<String, String> me(Authentication authentication) {
        return Map.of("username", authentication.getName());
    }
}
