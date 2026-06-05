package com.company.component.dict.autoconfigure;

import com.company.component.dict.cache.DictCache;
import com.company.component.dict.core.DictItem;
import com.company.component.dict.core.DictService;
import com.company.component.dict.spi.DictDataProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DictAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class, DictAutoConfiguration.class))
            .withUserConfiguration(TestDictProviderConfiguration.class);

    @Test
    void whenEnabledMissing_thenNoDictBeans() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(DictService.class);
            assertThat(context).doesNotHaveBean(DictCache.class);
        });
    }

    @Test
    void whenEnabledTrue_thenDictServiceRegistered() {
        contextRunner
                .withPropertyValues(
                        "component.dict.enabled=true",
                        "component.dict.cache.type=memory")
                .run(context -> {
                    assertThat(context).hasSingleBean(DictService.class);
                    assertThat(context).hasSingleBean(DictCache.class);
                    DictService dictService = context.getBean(DictService.class);
                    assertThat(dictService.getItems("gender")).hasSize(1);
                });
    }

    @Test
    void whenEnabledTrueWithoutProvider_thenStartupFails() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(DictAutoConfiguration.class))
                .withPropertyValues("component.dict.enabled=true")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void whenRedisWithoutTemplate_thenStartupFails() {
        contextRunner
                .withPropertyValues(
                        "component.dict.enabled=true",
                        "component.dict.cache.type=redis")
                .run(context -> assertThat(context).hasFailed());
    }

    @Configuration
    static class TestDictProviderConfiguration {

        @Bean
        DictDataProvider testDictDataProvider() {
            return dictType -> List.of(DictItem.of("gender", "1", "男", "M", 1, null, null));
        }
    }
}
