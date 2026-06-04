package com.company.component.exception.autoconfigure;

import com.company.component.exception.core.ExceptionMappingSupport;
import com.company.component.exception.web.ComponentGlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionAutoConfigurationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExceptionAutoConfiguration.class));

    @Test
    void whenEnabledFalse_thenHandlerNotRegistered() {
        contextRunner
                .withPropertyValues("component.exception.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean("componentGlobalExceptionHandler");
                    assertThat(context).doesNotHaveBean("componentExceptionMappingSupport");
                });
    }

    @Test
    void whenEnabledMissing_thenHandlerNotRegistered() {
        contextRunner.run(context ->
                assertThat(context).doesNotHaveBean("componentGlobalExceptionHandler"));
    }

    @Test
    void whenEnabledTrue_thenHandlerAndMappingRegistered() {
        contextRunner
                .withPropertyValues("component.exception.enabled=true")
                .run(context -> {
                    assertThat(context).hasBean("componentGlobalExceptionHandler");
                    assertThat(context).hasBean("componentExceptionMappingSupport");
                    assertThat(context.getBean(ComponentGlobalExceptionHandler.class)).isNotNull();
                    assertThat(context.getBean(ExceptionMappingSupport.class)).isNotNull();
                });
    }

    @Test
    void whenCustomHandlerBeanPresent_thenDefaultHandlerNotRegistered() {
        contextRunner
                .withPropertyValues("component.exception.enabled=true")
                .withBean("customHandler", ComponentGlobalExceptionHandler.class,
                        () -> new ComponentGlobalExceptionHandler(
                                new com.company.component.exception.core.ExceptionMappingSupport(
                                        new com.company.component.exception.properties.ExceptionProperties()),
                                java.util.List.of()))
                .run(context -> assertThat(context).doesNotHaveBean("componentGlobalExceptionHandler"));
    }
}
