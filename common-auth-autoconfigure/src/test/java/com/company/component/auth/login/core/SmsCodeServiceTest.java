package com.company.component.auth.login.core;

import com.company.component.auth.login.properties.LoginProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmsCodeServiceTest {

    @Test
    void testFixedCodeAcceptsAnyMobile() {
        LoginProperties login = testLoginFixed("123456");
        SmsCodeService service = new SmsCodeService(login, new SmsInMemoryVerificationCodeStore(), new StubSmsCodeSender());
        assertThatCode(() -> service.verifyCode("13800138000", "123456")).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.verifyCode("13800138000", "000000"))
                .isInstanceOf(LoginAuthException.class);
    }

    @Test
    void testMobileSuffixMode() {
        LoginProperties login = testLoginSuffix(4);
        SmsCodeService service = new SmsCodeService(login, new SmsInMemoryVerificationCodeStore(), new StubSmsCodeSender());
        assertThatCode(() -> service.verifyCode("13800138888", "8888")).doesNotThrowAnyException();
    }

    @Test
    void productionStoreVerify() {
        LoginProperties login = new LoginProperties();
        login.getSms().setEnabled(true);
        login.setSmsLength(6);
        SmsInMemoryVerificationCodeStore store = new SmsInMemoryVerificationCodeStore();
        SmsCodeService service = new SmsCodeService(login, store, new StubSmsCodeSender());
        store.save("13800138001", "654321", Duration.ofMinutes(5));
        assertThatCode(() -> service.verifyCode("13800138001", "654321")).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.verifyCode("13800138001", "654321"))
                .isInstanceOf(LoginAuthException.class);
    }

    private static LoginProperties testLoginFixed(String code) {
        LoginProperties login = new LoginProperties();
        login.setSmsLength(code.length());
        login.getTest().getSms().setEnabled(true);
        login.getTest().setAllowInProduction(true);
        login.getTest().getSms().setFixedCode(code);
        return login;
    }

    private static LoginProperties testLoginSuffix(int length) {
        LoginProperties login = new LoginProperties();
        login.setSmsLength(length);
        login.getTest().getSms().setEnabled(true);
        login.getTest().setAllowInProduction(true);
        login.getTest().getSms().setMobileSuffix(true);
        return login;
    }
}
