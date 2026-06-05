package com.company.component.dict.support;

import com.company.component.dict.properties.DictProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DictPropertiesValidatorTest {

    @Test
    void validMemoryConfigPasses() {
        DictProperties properties = new DictProperties();
        properties.getCache().setType("memory");
        assertThatCode(() -> DictPropertiesValidator.validate(properties)).doesNotThrowAnyException();
    }

    @Test
    void invalidCacheTypeFails() {
        DictProperties properties = new DictProperties();
        properties.getCache().setType("local");
        assertThatThrownBy(() -> DictPropertiesValidator.validate(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("memory");
    }

    @Test
    void nonPositiveTtlFails() {
        DictProperties properties = new DictProperties();
        properties.getCache().setTtlSeconds(0);
        assertThatThrownBy(() -> DictPropertiesValidator.validate(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ttl-seconds");
    }
}
