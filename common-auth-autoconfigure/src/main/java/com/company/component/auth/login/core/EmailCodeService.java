package com.company.component.auth.login.core;

import com.company.component.auth.login.properties.LoginProperties;
import com.company.component.auth.login.spi.EmailCodeSender;
import com.company.component.auth.login.spi.EmailCodeStore;
import com.company.component.auth.login.support.EmailSupport;
import com.company.component.auth.login.support.VerificationCodeEngine;

public class EmailCodeService {

    private final LoginProperties loginProperties;
    private final VerificationCodeEngine engine = new VerificationCodeEngine();
    private final EmailCodeStore store;
    private final EmailCodeSender sender;

    public EmailCodeService(LoginProperties loginProperties, EmailCodeStore store, EmailCodeSender sender) {
        this.loginProperties = loginProperties;
        this.store = store;
        this.sender = sender;
    }

    public void sendCode(String emailRaw) {
        engine.sendCode(emailRaw, emailSettings(), store, sender);
    }

    public void verifyCode(String emailRaw, String codeRaw) {
        engine.verifyCode(emailRaw, codeRaw, emailSettings(), store);
    }

    public int codeTtlSeconds() {
        return loginProperties.getEmail().getCodeTtlSeconds();
    }

    public int resendIntervalSeconds() {
        return loginProperties.getEmail().getResendIntervalSeconds();
    }

    private VerificationCodeEngine.ChannelSettings emailSettings() {
        LoginProperties.Test test = loginProperties.getTest();
        return new VerificationCodeEngine.ChannelSettings(
                loginProperties.getEmailCodeLength(),
                loginProperties.getEmail().getCodeTtlSeconds(),
                loginProperties.getEmail().getResendIntervalSeconds(),
                test.isEmailTestActive(),
                test.effectiveEmailFixedCode(),
                false,
                this::normalizeEmail,
                EmailSupport::mask,
                email -> "",
                LoginAuthException::invalidEmailCode,
                LoginAuthException::emailSendTooFrequent,
                LoginAuthException::emailSendFailed);
    }

    private String normalizeEmail(String emailRaw) {
        try {
            EmailSupport.requireValid(emailRaw);
            return EmailSupport.normalize(emailRaw);
        } catch (IllegalArgumentException ex) {
            throw LoginAuthException.invalidEmail();
        }
    }
}
