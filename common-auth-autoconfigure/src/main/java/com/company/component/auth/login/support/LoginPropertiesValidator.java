package com.company.component.auth.login.support;

import com.company.component.auth.login.properties.LoginProperties;
import org.springframework.util.StringUtils;

public final class LoginPropertiesValidator {

    private LoginPropertiesValidator() {
    }

    public static void validate(LoginProperties login, boolean authEnabled) {
        if (!login.isEnabled()) {
            return;
        }
        if (!authEnabled) {
            throw new IllegalStateException(
                    "component.auth.login.enabled=true requires component.auth.enabled=true");
        }
        if (!login.isAnyChannelEnabled()) {
            throw new IllegalStateException(
                    "component.auth.login.enabled=true requires login.sms.enabled or login.email.enabled");
        }
        validateCodeLength(login.getSmsLength(), "sms-length");
        validateCodeLength(login.getEmailCodeLength(), "email-code-length");

        if (login.getSms().isEnabled()) {
            validateSmsPaths(login.getSms());
            if (login.getSms().getResendIntervalSeconds() < 30) {
                throw new IllegalStateException(
                        "component.auth.login.sms.resend-interval-seconds must be >= 30");
            }
            if (login.getRegister().isEnabled()) {
                validatePath(login.getSms().getPaths().getRegister(), "sms.register");
            }
        }
        if (login.getEmail().isEnabled()) {
            validateEmailPaths(login.getEmail());
            if (login.getEmail().getResendIntervalSeconds() < 30) {
                throw new IllegalStateException(
                        "component.auth.login.email.resend-interval-seconds must be >= 30");
            }
            if (login.getRegister().isEnabled()) {
                validatePath(login.getEmail().getPaths().getRegister(), "email.register");
            }
        }
        if (login.getTest().isAnyTestActive()) {
            if (!login.getTest().isAllowInProduction()) {
                throw new IllegalStateException(
                        "component.auth.login.test.sms/email is enabled (test verification bypass). "
                                + "Set component.auth.login.test.allow-in-production=true only in local/CI test config, "
                                + "or disable test.sms/test.email. Never enable test channels in production.");
            }
            if (login.getSms().isEnabled() && login.getTest().isSmsTestActive()) {
                validateSmsTest(login);
            }
            if (login.getEmail().isEnabled() && login.getTest().isEmailTestActive()) {
                validateEmailTest(login);
            }
        }
    }

    private static void validateCodeLength(int length, String name) {
        if (length != 4 && length != 6) {
            throw new IllegalStateException("component.auth.login." + name + " must be 4 or 6");
        }
    }

    private static void validateSmsPaths(LoginProperties.Sms sms) {
        validatePath(sms.getPaths().getSendCode(), "sms.send-code");
        validatePath(sms.getPaths().getLogin(), "sms.login");
        if (sms.getCodeTtlSeconds() <= 0) {
            throw new IllegalStateException("component.auth.login.sms.code-ttl-seconds must be > 0");
        }
    }

    private static void validateEmailPaths(LoginProperties.Email email) {
        validatePath(email.getPaths().getSendCode(), "email.send-code");
        validatePath(email.getPaths().getLogin(), "email.login");
        if (email.getCodeTtlSeconds() <= 0) {
            throw new IllegalStateException("component.auth.login.email.code-ttl-seconds must be > 0");
        }
    }

    private static void validateSmsTest(LoginProperties login) {
        LoginProperties.Test test = login.getTest();
        boolean hasFixed = StringUtils.hasText(test.effectiveSmsFixedCode());
        boolean suffix = test.effectiveSmsMobileSuffix();
        if (hasFixed && suffix) {
            throw new IllegalStateException(
                    "component.auth.login.test.sms: fixed-code and mobile-suffix cannot both be active");
        }
        if (!hasFixed && !suffix) {
            throw new IllegalStateException(
                    "component.auth.login.test.sms requires fixed-code or mobile-suffix=true");
        }
        if (hasFixed) {
            validateFixedCode(test.effectiveSmsFixedCode(), login.getSmsLength(), "sms");
        }
    }

    private static void validateEmailTest(LoginProperties login) {
        String fixed = login.getTest().effectiveEmailFixedCode();
        if (!StringUtils.hasText(fixed)) {
            throw new IllegalStateException(
                    "component.auth.login.test.email.enabled=true requires test.email.fixed-code");
        }
        if (login.getTest().getEmail().isMobileSuffix()) {
            throw new IllegalStateException(
                    "component.auth.login.test.email does not support mobile-suffix");
        }
        validateFixedCode(fixed, login.getEmailCodeLength(), "email");
    }

    private static void validateFixedCode(String fixed, int length, String channel) {
        if (fixed.length() != length) {
            throw new IllegalStateException(
                    "component.auth.login.test." + channel + ".fixed-code length must equal code length (" + length + ")");
        }
        if (!fixed.chars().allMatch(Character::isDigit)) {
            throw new IllegalStateException(
                    "component.auth.login.test." + channel + ".fixed-code must be numeric");
        }
    }

    private static void validatePath(String path, String name) {
        if (!StringUtils.hasText(path) || !path.startsWith("/")) {
            throw new IllegalStateException(
                    "component.auth.login path '" + name + "' must be non-empty and start with '/'");
        }
    }
}
