package com.company.component.auth.login.redis.core;

import com.company.component.auth.login.spi.VerificationCodeStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Redis 验证码存储（短信/邮箱共用实现）。
 */
public class RedisVerificationCodeStore implements VerificationCodeStore {

    private final StringRedisTemplate redisTemplate;
    private final String codeKeyPrefix;
    private final String sendKeyPrefix;

    public RedisVerificationCodeStore(StringRedisTemplate redisTemplate, String keyPrefix, String channel) {
        this.redisTemplate = redisTemplate;
        String normalized = normalizePrefix(keyPrefix);
        this.codeKeyPrefix = normalized + ":" + channel + ":code:";
        this.sendKeyPrefix = normalized + ":" + channel + ":send:";
    }

    @Override
    public void save(String identity, String code, Duration ttl) {
        redisTemplate.opsForValue().set(codeKey(identity), code, ttl);
        redisTemplate.opsForValue().set(sendKey(identity), String.valueOf(Instant.now().toEpochMilli()));
    }

    @Override
    public boolean verifyAndConsume(String identity, String code) {
        String key = codeKey(identity);
        String stored = redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(stored)) {
            return false;
        }
        if (!stored.equals(code)) {
            return false;
        }
        redisTemplate.delete(key);
        return true;
    }

    @Override
    public Optional<Duration> timeSinceLastSend(String identity) {
        String raw = redisTemplate.opsForValue().get(sendKey(identity));
        if (!StringUtils.hasText(raw)) {
            return Optional.empty();
        }
        try {
            long epochMilli = Long.parseLong(raw.trim());
            return Optional.of(Duration.between(Instant.ofEpochMilli(epochMilli), Instant.now()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private String codeKey(String identity) {
        return codeKeyPrefix + identity;
    }

    private String sendKey(String identity) {
        return sendKeyPrefix + identity;
    }

    private static String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            throw new IllegalArgumentException("component.auth.login.redis.key-prefix must not be blank");
        }
        String normalized = prefix.trim();
        while (normalized.endsWith(":")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
