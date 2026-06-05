package com.company.component.dict.autoconfigure;

import com.company.component.dict.core.DictItem;
import com.company.component.dict.spi.DictDataProvider;
import com.company.component.dict.web.DictController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DictWebAutoConfigurationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    WebMvcAutoConfiguration.class,
                    DictAutoConfiguration.class,
                    DictWebAutoConfiguration.class))
            .withUserConfiguration(TestDictProviderConfiguration.class);

    @Test
    void whenApiDisabled_thenNoController() {
        contextRunner
                .withPropertyValues(
                        "component.dict.enabled=true",
                        "component.dict.api.enabled=false",
                        "component.dict.cache.type=memory")
                .run(context -> assertThat(context).doesNotHaveBean(DictController.class));
    }

    @Test
    void whenApiEnabled_thenControllerRegistered() {
        contextRunner
                .withPropertyValues(
                        "component.dict.enabled=true",
                        "component.dict.api.enabled=true",
                        "component.dict.cache.type=memory")
                .run(context -> assertThat(context).hasSingleBean(DictController.class));
    }

    @Configuration
    static class TestDictProviderConfiguration {

        @Bean
        DictDataProvider testDictDataProvider() {
            return dictType -> List.of(DictItem.of("gender", "1", "男", "M", 1, null, null));
        }
    }
}
