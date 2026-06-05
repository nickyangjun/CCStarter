package com.company.component.dict.cache;

import com.company.component.dict.core.DictItem;
import com.company.component.dict.properties.DictProperties;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryDictCache implements DictCache {

    private final DictProperties.Cache cacheProperties;
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    public InMemoryDictCache(DictProperties properties) {
        this.cacheProperties = properties.getCache();
    }

    @Override
    public Optional<List<DictItem>> get(String dictType) {
        Entry entry = entries.get(dictType);
        if (entry == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(entry.expireAt)) {
            entries.remove(dictType);
            return Optional.empty();
        }
        return Optional.of(entry.items);
    }

    @Override
    public void put(String dictType, List<DictItem> items) {
        int ttlSeconds = resolveTtlSeconds(items);
        Instant expireAt = Instant.now().plusSeconds(ttlSeconds);
        entries.put(dictType, new Entry(List.copyOf(items), expireAt));
    }

    @Override
    public void evict(String dictType) {
        entries.remove(dictType);
    }

    @Override
    public void evictAll() {
        entries.clear();
    }

    private int resolveTtlSeconds(List<DictItem> items) {
        if (items.isEmpty() && cacheProperties.getNullTtlSeconds() > 0) {
            return cacheProperties.getNullTtlSeconds();
        }
        return cacheProperties.getTtlSeconds();
    }

    private record Entry(List<DictItem> items, Instant expireAt) {
    }
}
