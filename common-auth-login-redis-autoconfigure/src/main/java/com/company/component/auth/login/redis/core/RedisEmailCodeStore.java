package com.company.component.auth.login.redis.core;

import com.company.component.auth.login.spi.EmailCodeStore;

public final class RedisEmailCodeStore extends RedisVerificationCodeStore implements EmailCodeStore {

    public RedisEmailCodeStore(org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
                               String keyPrefix) {
        super(redisTemplate, keyPrefix, "email");
    }
}
