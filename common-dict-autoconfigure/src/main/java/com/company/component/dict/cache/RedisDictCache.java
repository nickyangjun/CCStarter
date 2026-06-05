package com.company.component.dict.cache;

import com.company.component.dict.core.DictItem;
import com.company.component.dict.properties.DictProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class RedisDictCache implements DictCache {

    private static final Logger log = LoggerFactory.getLogger(RedisDictCache.class);
    private static final String EMPTY_MARKER = "__EMPTY__";
    private static final TypeReference<List<DictItemSnapshot>> LIST_TYPE = new TypeReference<>() {
    };

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final DictProperties.Cache cacheProperties;
    private final String keyPrefix;

    public RedisDictCache(StringRedisTemplate redisTemplate,
                          ObjectMapper objectMapper,
                          DictProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.cacheProperties = properties.getCache();
        this.keyPrefix = normalizePrefix(cacheProperties.getKeyPrefix());
    }

    @Override
    public Optional<List<DictItem>> get(String dictType) {
        String dataKey = dataKey(dictType);
        String json = redisTemplate.opsForValue().get(dataKey);
        if (StringUtils.hasText(json)) {
            return Optional.of(deserialize(json));
        }
        String emptyKey = emptyKey(dictType);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(emptyKey))) {
            return Optional.of(List.of());
        }
        return Optional.empty();
    }

    @Override
    public void put(String dictType, List<DictItem> items) {
        evict(dictType);
        if (items.isEmpty()) {
            int nullTtl = cacheProperties.getNullTtlSeconds();
            if (nullTtl > 0) {
                redisTemplate.opsForValue().set(emptyKey(dictType), "1", Duration.ofSeconds(nullTtl));
            }
            return;
        }
        redisTemplate.opsForValue().set(
                dataKey(dictType),
                serialize(items),
                Duration.ofSeconds(cacheProperties.getTtlSeconds()));
    }

    @Override
    public void evict(String dictType) {
        redisTemplate.delete(dataKey(dictType));
        redisTemplate.delete(emptyKey(dictType));
    }

    @Override
    public void evictAll() {
        String pattern = keyPrefix + ":*";
        Set<String> keys = scanKeys(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("dict cache evictAll removed {} keys under prefix={}", keys.size(), keyPrefix);
        }
    }

    private Set<String> scanKeys(String pattern) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new java.util.LinkedHashSet<>();
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(200).build();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            }
            return keys;
        });
    }

    private String dataKey(String dictType) {
        return keyPrefix + ":" + dictType;
    }

    private String emptyKey(String dictType) {
        return keyPrefix + ":" + EMPTY_MARKER + ":" + dictType;
    }

    private static String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            throw new IllegalArgumentException("component.dict.cache.key-prefix must not be blank");
        }
        String normalized = prefix.trim();
        while (normalized.endsWith(":")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String serialize(List<DictItem> items) {
        List<DictItemSnapshot> snapshots = new ArrayList<>(items.size());
        for (DictItem item : items) {
            snapshots.add(DictItemSnapshot.from(item));
        }
        try {
            return objectMapper.writeValueAsString(snapshots);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize dict cache payload for Redis", ex);
        }
    }

    private List<DictItem> deserialize(String json) {
        try {
            List<DictItemSnapshot> snapshots = objectMapper.readValue(json, LIST_TYPE);
            List<DictItem> items = new ArrayList<>(snapshots.size());
            for (DictItemSnapshot snapshot : snapshots) {
                items.add(snapshot.toDictItem());
            }
            return List.copyOf(items);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize dict cache payload from Redis", ex);
        }
    }

    private record DictItemSnapshot(
            String dictType,
            String code,
            String label,
            String value,
            int sortOrder,
            String cssClass,
            java.util.Map<String, Object> extra) {

        static DictItemSnapshot from(DictItem item) {
            return new DictItemSnapshot(
                    item.dictType(),
                    item.code(),
                    item.label(),
                    item.value(),
                    item.sortOrder(),
                    item.cssClass(),
                    item.extra());
        }

        DictItem toDictItem() {
            return DictItem.of(dictType, code, label, value, sortOrder, cssClass, extra);
        }
    }
}
