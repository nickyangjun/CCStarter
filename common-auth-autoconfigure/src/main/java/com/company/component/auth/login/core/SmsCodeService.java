package com.company.component.auth.login.core;

import com.company.component.auth.login.properties.LoginProperties;
import com.company.component.auth.login.spi.SmsCodeSender;
import com.company.component.auth.login.spi.SmsCodeStore;
import com.company.component.auth.login.support.MobileSupport;
import com.company.component.auth.login.support.VerificationCodeEngine;

public class SmsCodeService {

    private final LoginProperties loginProperties;
    private final VerificationCodeEngine engine = new VerificationCodeEngine();
    private final SmsCodeStore store;
    private final SmsCodeSender sender;

    public SmsCodeService(LoginProperties loginProperties, SmsCodeStore store, SmsCodeSender sender) {
        this.loginProperties = loginProperties;
        this.store = store;
        this.sender = sender;
    }

    public void sendCode(String mobileRaw) {
        engine.sendCode(mobileRaw, smsSettings(), store, sender);
    }

    public void verifyCode(String mobileRaw, String codeRaw) {
        engine.verifyCode(mobileRaw, codeRaw, smsSettings(), store);
    }

    public int codeTtlSeconds() {
        return loginProperties.getSms().getCodeTtlSeconds();
    }

    public int resendIntervalSeconds() {
        return loginProperties.getSms().getResendIntervalSeconds();
    }

    private VerificationCodeEngine.ChannelSettings smsSettings() {
        LoginProperties.Test test = loginProperties.getTest();
        return new VerificationCodeEngine.ChannelSettings(
                loginProperties.getSmsLength(),
                loginProperties.getSms().getCodeTtlSeconds(),
                loginProperties.getSms().getResendIntervalSeconds(),
                test.isSmsTestActive(),
                test.effectiveSmsFixedCode(),
                test.effectiveSmsMobileSuffix(),
                this::normalizeMobile,
                MobileSupport::mask,
                mobile -> MobileSupport.mobileSuffix(mobile, loginProperties.getSmsLength()),
                LoginAuthException::invalidSmsCode,
                LoginAuthException::sendTooFrequent,
                LoginAuthException::sendFailed);
    }

    private String normalizeMobile(String mobileRaw) {
        try {
            MobileSupport.requireValid(mobileRaw);
            return MobileSupport.normalize(mobileRaw);
        } catch (IllegalArgumentException ex) {
            throw LoginAuthException.invalidMobile();
        }
    }
}
