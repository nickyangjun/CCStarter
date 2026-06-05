package com.company.component.auth.login.support;

import com.company.component.auth.login.properties.LoginProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginPropertiesValidatorTest {

    @Test
    void testModeSmsFixedCodeValid() {
        LoginProperties login = baseLogin();
        login.getTest().setAllowInProduction(true);
        login.getTest().getSms().setEnabled(true);
        login.getTest().getSms().setFixedCode("123456");
        assertThatCode(() -> LoginPropertiesValidator.validate(login, true)).doesNotThrowAnyException();
    }

    @Test
    void testModeEmailFixedCodeValid() {
        LoginProperties login = baseLogin();
        login.getEmail().setEnabled(true);
        login.getTest().setAllowInProduction(true);
        login.getTest().getEmail().setEnabled(true);
        login.getTest().getEmail().setFixedCode("123456");
        assertThatCode(() -> LoginPropertiesValidator.validate(login, true)).doesNotThrowAnyException();
    }

    @Test
    void requiresAtLeastOneChannel() {
        LoginProperties login = new LoginProperties();
        login.setEnabled(true);
        assertThatThrownBy(() -> LoginPropertiesValidator.validate(login, true))
                .isInstanceOf(IllegalStateException.class);
    }

    private static LoginProperties baseLogin() {
        LoginProperties login = new LoginProperties();
        login.setEnabled(true);
        login.getSms().setEnabled(true);
        return login;
    }
}
