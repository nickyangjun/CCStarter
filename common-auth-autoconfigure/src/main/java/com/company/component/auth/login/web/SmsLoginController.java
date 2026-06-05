package com.company.component.auth.login.web;

import com.company.component.auth.login.core.LoginAuthException;
import com.company.component.auth.login.core.SmsAuthService;
import com.company.component.auth.login.core.SmsCodeService;
import com.company.component.auth.login.web.dto.MobileCodeRequest;
import com.company.component.auth.login.web.dto.MobileRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@ConditionalOnProperty(prefix = "component.auth.login.sms", name = "enabled", havingValue = "true")
public class SmsLoginController {

    private final SmsCodeService smsCodeService;
    private final SmsAuthService smsAuthService;

    public SmsLoginController(SmsCodeService smsCodeService, SmsAuthService smsAuthService) {
        this.smsCodeService = smsCodeService;
        this.smsAuthService = smsAuthService;
    }

    @PostMapping("${component.auth.login.sms.paths.send-code:/api/auth/sms/send-code}")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestBody MobileRequest request) {
        if (request == null || request.mobile() == null) {
            throw LoginAuthException.invalidMobile();
        }
        smsCodeService.sendCode(request.mobile());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "ttlSeconds", smsCodeService.codeTtlSeconds(),
                "resendAfterSeconds", smsCodeService.resendIntervalSeconds()));
    }

    @PostMapping("${component.auth.login.sms.paths.login:/api/auth/sms/login}")
    public ResponseEntity<Map<String, Object>> login(@RequestBody MobileCodeRequest request) {
        if (request == null || request.mobile() == null || request.code() == null) {
            throw LoginAuthException.invalidSmsCode();
        }
        return ResponseEntity.ok(smsAuthService.login(request.mobile(), request.code()));
    }
}
