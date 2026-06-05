package com.company.component.auth.login.web;

import com.company.component.auth.login.core.EmailAuthService;
import com.company.component.auth.login.core.EmailCodeService;
import com.company.component.auth.login.core.LoginAuthException;
import com.company.component.auth.login.web.dto.EmailCodeRequest;
import com.company.component.auth.login.web.dto.EmailRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@ConditionalOnProperty(prefix = "component.auth.login.email", name = "enabled", havingValue = "true")
public class EmailLoginController {

    private final EmailCodeService emailCodeService;
    private final EmailAuthService emailAuthService;

    public EmailLoginController(EmailCodeService emailCodeService, EmailAuthService emailAuthService) {
        this.emailCodeService = emailCodeService;
        this.emailAuthService = emailAuthService;
    }

    @PostMapping("${component.auth.login.email.paths.send-code:/api/auth/email/send-code}")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestBody EmailRequest request) {
        if (request == null || request.email() == null) {
            throw LoginAuthException.invalidEmail();
        }
        emailCodeService.sendCode(request.email());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "ttlSeconds", emailCodeService.codeTtlSeconds(),
                "resendAfterSeconds", emailCodeService.resendIntervalSeconds()));
    }

    @PostMapping("${component.auth.login.email.paths.login:/api/auth/email/login}")
    public ResponseEntity<Map<String, Object>> login(@RequestBody EmailCodeRequest request) {
        if (request == null || request.email() == null || request.code() == null) {
            throw LoginAuthException.invalidEmailCode();
        }
        return ResponseEntity.ok(emailAuthService.login(request.email(), request.code()));
    }
}
