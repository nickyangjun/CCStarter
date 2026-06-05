package com.company.component.dict.core;

import com.company.component.dict.cache.InMemoryDictCache;
import com.company.component.dict.properties.DictProperties;
import com.company.component.dict.spi.DictDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DictServiceTest {

    private final AtomicInteger loadCount = new AtomicInteger();
    private DictService dictService;

    @BeforeEach
    void setUp() {
        loadCount.set(0);
        DictProperties properties = new DictProperties();
        properties.setEnabled(true);
        DictDataProvider provider = dictType -> {
            loadCount.incrementAndGet();
            if ("gender".equals(dictType)) {
                return List.of(
                        DictItem.of("gender", "1", "男", "M", 1, null, null),
                        DictItem.of("gender", "2", "女", null, 2, null, null));
            }
            return List.of();
        };
        dictService = new DictService(properties, new InMemoryDictCache(properties), provider);
    }

    @Test
    void getItemsUsesCacheAfterFirstLoad() {
        assertThat(dictService.getItems("gender")).hasSize(2);
        assertThat(dictService.getItems("gender")).hasSize(2);
        assertThat(loadCount.get()).isEqualTo(1);
    }

    @Test
    void getLabelAndValue() {
        assertThat(dictService.getLabel("gender", "1")).contains("男");
        assertThat(dictService.getValue("gender", "1")).contains("M");
        assertThat(dictService.getValue("gender", "2")).contains("2");
    }

    @Test
    void requireLabelThrowsWhenMissing() {
        assertThatThrownBy(() -> dictService.requireLabel("gender", "9"))
                .isInstanceOf(DictException.class)
                .hasFieldOrPropertyWithValue("errorCode", "DICT_ENTRY_NOT_FOUND");
    }

    @Test
    void refreshForcesReload() {
        dictService.getItems("gender");
        dictService.refresh("gender");
        dictService.getItems("gender");
        assertThat(loadCount.get()).isEqualTo(2);
    }

    @Test
    void providerFailureMapsToLoadFailed() {
        DictProperties properties = new DictProperties();
        DictDataProvider failing = type -> {
            throw new IllegalStateException("db down");
        };
        DictService service = new DictService(properties, new InMemoryDictCache(properties), failing);
        assertThatThrownBy(() -> service.getItems("any"))
                .isInstanceOf(DictException.class)
                .hasFieldOrPropertyWithValue("errorCode", "DICT_LOAD_FAILED");
    }
}
