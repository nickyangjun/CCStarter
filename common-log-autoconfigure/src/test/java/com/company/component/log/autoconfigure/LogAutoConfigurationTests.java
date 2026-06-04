package com.company.component.log.autoconfigure;

import com.company.component.log.trace.TraceIdFilter;
import com.company.component.log.trace.TraceIdResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

class LogAutoConfigurationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    WebMvcAutoConfiguration.class,
                    LogAutoConfiguration.class,
                    LogWebAutoConfiguration.class));

    @Test
    void whenEnabledMissing_thenNoLogBeans() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(TraceIdResolver.class);
            assertThat(context).doesNotHaveBean("componentTraceIdFilter");
        });
    }

    @Test
    void whenEnabledTrue_thenTraceFiltersRegistered() {
        contextRunner
                .withPropertyValues("component.log.enabled=true")
                .run(context -> {
                    assertThat(context).hasBean("componentTraceIdResolver");
                    assertThat(context).getBean(TraceIdResolver.class).isNotNull();
                    assertThat(context).hasBean("componentTraceIdFilter");
                    @SuppressWarnings("unchecked")
                    FilterRegistrationBean<TraceIdFilter> registration =
                            (FilterRegistrationBean<TraceIdFilter>) context.getBean("componentTraceIdFilter");
                    assertThat(registration.getFilter()).isNotNull();
                });
    }
}
