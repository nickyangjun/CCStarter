package com.company.component.dict.support;

import com.company.component.dict.properties.DictProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DictPathCollectorTest {

    @Test
    void collectWhenApiDisabledReturnsEmpty() {
        DictProperties properties = new DictProperties();
        properties.setEnabled(true);
        assertThat(DictPathCollector.collect(properties)).isEmpty();
    }

    @Test
    void collectWhenApiEnabledReturnsAntPattern() {
        DictProperties properties = new DictProperties();
        properties.setEnabled(true);
        properties.getApi().setEnabled(true);
        properties.getApi().setBasePath("/api/dict");
        assertThat(DictPathCollector.collect(properties)).containsExactly("/api/dict/**");
    }
}
