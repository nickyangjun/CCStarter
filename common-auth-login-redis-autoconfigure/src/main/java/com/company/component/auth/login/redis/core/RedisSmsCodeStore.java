package com.company.component.auth.login.redis.core;

import com.company.component.auth.login.spi.SmsCodeStore;

public final class RedisSmsCodeStore extends RedisVerificationCodeStore implements SmsCodeStore {

    public RedisSmsCodeStore(org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
                             String keyPrefix) {
        super(redisTemplate, keyPrefix, "sms");
    }
}
