package com.company.component.auth.login.web;

import com.company.component.auth.login.core.LoginAuthException;
import com.company.component.auth.login.core.SmsAuthService;
import com.company.component.auth.login.web.dto.MobileCodeRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@ConditionalOnProperty(prefix = "component.auth.login.register", name = "enabled", havingValue = "true")
public class SmsRegisterController {

    private final SmsAuthService smsAuthService;

    public SmsRegisterController(SmsAuthService smsAuthService) {
        this.smsAuthService = smsAuthService;
    }

    @PostMapping("${component.auth.login.sms.paths.register:/api/auth/sms/register}")
    public ResponseEntity<Map<String, Object>> register(@RequestBody MobileCodeRequest request) {
        if (request == null || request.mobile() == null || request.code() == null) {
            throw LoginAuthException.invalidSmsCode();
        }
        return ResponseEntity.ok(smsAuthService.register(request.mobile(), request.code()));
    }
}
