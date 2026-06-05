package com.company.component.auth.login.autoconfigure;

import com.company.component.auth.autoconfigure.AuthAutoConfiguration;
import com.company.component.auth.autoconfigure.AuthSecurityAutoConfiguration;
import com.company.component.auth.login.spi.LoginUserRegistrar;
import com.company.component.auth.login.spi.LoginUserResolver;
import com.company.component.auth.login.web.SmsLoginController;
import com.company.component.exception.autoconfigure.ExceptionAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAutoConfigurationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withUserConfiguration(SampleLoginSpiConfiguration.class)
            .withConfiguration(AutoConfigurations.of(
                    WebMvcAutoConfiguration.class,
                    SecurityAutoConfiguration.class,
                    ExceptionAutoConfiguration.class,
                    AuthAutoConfiguration.class,
                    AuthSecurityAutoConfiguration.class,
                    LoginAutoConfiguration.class));

    @Test
    void whenLoginDisabled_thenNoLoginController() {
        contextRunner
                .withPropertyValues(
                        "component.exception.enabled=true",
                        "component.auth.enabled=true",
                        "component.auth.jwt-secret=01234567890123456789012345678901234567890123456789012",
                        "component.auth.login.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(SmsLoginController.class));
    }

    @Test
    void whenLoginEnabledWithTestMode_thenLoginControllerRegistered() {
        contextRunner
                .withPropertyValues(
                        "component.exception.enabled=true",
                        "component.auth.enabled=true",
                        "component.auth.jwt-secret=01234567890123456789012345678901234567890123456789012",
                        "component.auth.login.enabled=true",
                        "component.auth.login.sms.enabled=true",
                        "component.auth.login.test.enabled=true",
                        "component.auth.login.test.allow-in-production=true",
                        "component.auth.login.test.fixed-code=123456",
                        "component.auth.login.register.enabled=true",
                        "component.auth.login.register.login-as-register=true")
                .run(context -> assertThat(context).hasBean("componentSmsLoginController"));
    }

    @TestConfiguration
    static class SampleLoginSpiConfiguration {

        @Bean
        LoginUserResolver sampleLoginUserResolver() {
            return mobile -> java.util.Optional.empty();
        }

        @Bean
        LoginUserRegistrar sampleLoginUserRegistrar() {
            return request -> new com.company.component.auth.login.core.LoginPrincipal(
                    request.mobile(), 1L);
        }
    }
}
