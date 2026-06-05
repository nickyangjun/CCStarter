package com.company.component.dict.cache;

import com.company.component.dict.core.DictItem;
import com.company.component.dict.properties.DictProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryDictCacheTest {

    @Test
    void emptyListUsesNullTtl() throws InterruptedException {
        DictProperties properties = new DictProperties();
        properties.getCache().setNullTtlSeconds(1);
        properties.getCache().setTtlSeconds(3600);
        InMemoryDictCache cache = new InMemoryDictCache(properties);

        cache.put("missing", List.of());
        assertThat(cache.get("missing")).isPresent();

        Thread.sleep(1100);
        assertThat(cache.get("missing")).isEmpty();
    }

    @Test
    void evictRemovesEntry() {
        DictProperties properties = new DictProperties();
        InMemoryDictCache cache = new InMemoryDictCache(properties);
        List<DictItem> items = List.of(DictItem.of("gender", "1", "男", "M", 1, null, null));

        cache.put("gender", items);
        assertThat(cache.get("gender")).isPresent();
        cache.evict("gender");
        assertThat(cache.get("gender")).isEmpty();
    }
}
