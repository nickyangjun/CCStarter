package com.company.component.dict.support;

import com.company.component.dict.properties.DictProperties;
import org.springframework.util.StringUtils;

public final class DictPropertiesValidator {

    private DictPropertiesValidator() {
    }

    public static void validate(DictProperties properties) {
        DictProperties.Cache cache = properties.getCache();
        if (!cache.isMemory() && !cache.isRedis()) {
            throw new IllegalStateException(
                    "component.dict.cache.type must be 'memory' or 'redis', got: " + cache.getType());
        }
        if (cache.getTtlSeconds() <= 0) {
            throw new IllegalStateException("component.dict.cache.ttl-seconds must be > 0");
        }
        if (cache.getNullTtlSeconds() < 0) {
            throw new IllegalStateException("component.dict.cache.null-ttl-seconds must be >= 0");
        }
        if (!StringUtils.hasText(cache.getKeyPrefix())) {
            throw new IllegalStateException("component.dict.cache.key-prefix must not be blank");
        }
    }
}
