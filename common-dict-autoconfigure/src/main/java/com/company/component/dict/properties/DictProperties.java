package com.company.component.dict.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "component.dict")
public class DictProperties {

    private boolean enabled = false;

    private final Cache cache = new Cache();

    private final Api api = new Api();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Cache getCache() {
        return cache;
    }

    public Api getApi() {
        return api;
    }

    public static class Api {

        private boolean enabled = false;

        private String basePath = "/api/dict";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }

    public static class Cache {

        private String type = "memory";

        private int ttlSeconds = 3600;

        private String keyPrefix = "app:dict";

        private int nullTtlSeconds = 60;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(int ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        public int getNullTtlSeconds() {
            return nullTtlSeconds;
        }

        public void setNullTtlSeconds(int nullTtlSeconds) {
            this.nullTtlSeconds = nullTtlSeconds;
        }

        public boolean isRedis() {
            return "redis".equalsIgnoreCase(type);
        }

        public boolean isMemory() {
            return "memory".equalsIgnoreCase(type);
        }
    }
}
