package com.company.component.auth.autoconfigure;

import com.company.component.auth.core.JwtService;
import com.company.component.auth.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AuthAutoConfigurationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    WebMvcAutoConfiguration.class,
                    SecurityAutoConfiguration.class,
                    AuthAutoConfiguration.class,
                    AuthSecurityAutoConfiguration.class));

    @Test
    void whenEnabledMissing_thenNoAuthBeans() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean("componentJwtService");
            assertThat(context).doesNotHaveBean("componentSecurityFilterChain");
        });
    }

    @Test
    void whenEnabledTrueWithSecret_thenAuthBeansRegistered() {
        contextRunner
                .withPropertyValues(
                        "component.auth.enabled=true",
                        "component.auth.jwt-secret=01234567890123456789012345678901234567890123456789012")
                .run(context -> {
                    assertThat(context).hasBean("componentJwtService");
                    assertThat(context).hasBean("componentJwtAuthenticationFilter");
                    assertThat(context).hasBean("componentSecurityFilterChain");
                    assertThat(context.getBean(JwtService.class)).isNotNull();
                });
    }

    @Test
    void whenEnabledTrueWithoutSecret_thenContextFails() {
        contextRunner
                .withPropertyValues("component.auth.enabled=true")
                .run(context -> assertThat(context).hasFailed());
    }
}
