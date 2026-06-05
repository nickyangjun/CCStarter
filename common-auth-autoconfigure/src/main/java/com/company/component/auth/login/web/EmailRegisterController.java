package com.company.component.auth.login.web;

import com.company.component.auth.login.core.EmailAuthService;
import com.company.component.auth.login.core.LoginAuthException;
import com.company.component.auth.login.web.dto.EmailCodeRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@ConditionalOnProperty(prefix = "component.auth.login.register", name = "enabled", havingValue = "true")
public class EmailRegisterController {

    private final EmailAuthService emailAuthService;

    public EmailRegisterController(EmailAuthService emailAuthService) {
        this.emailAuthService = emailAuthService;
    }

    @PostMapping("${component.auth.login.email.paths.register:/api/auth/email/register}")
    public ResponseEntity<Map<String, Object>> register(@RequestBody EmailCodeRequest request) {
        if (request == null || request.email() == null || request.code() == null) {
            throw LoginAuthException.invalidEmailCode();
        }
        return ResponseEntity.ok(emailAuthService.register(request.email(), request.code()));
    }
}
