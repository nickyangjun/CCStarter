package com.company.component.auth.login.core;

import com.company.component.auth.login.properties.LoginProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailCodeServiceTest {

    @Test
    void testFixedCodeAcceptsAnyEmail() {
        LoginProperties login = new LoginProperties();
        login.setEmailCodeLength(6);
        login.getTest().setAllowInProduction(true);
        login.getTest().getEmail().setEnabled(true);
        login.getTest().getEmail().setFixedCode("123456");
        EmailCodeService service = new EmailCodeService(login, new EmailInMemoryVerificationCodeStore(), new StubEmailCodeSender());
        assertThatCode(() -> service.verifyCode("user@example.com", "123456")).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.verifyCode("user@example.com", "000000"))
                .isInstanceOf(LoginAuthException.class);
    }
}
