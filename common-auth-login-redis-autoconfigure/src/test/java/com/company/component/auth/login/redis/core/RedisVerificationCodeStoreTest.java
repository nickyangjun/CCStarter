package com.company.component.auth.login.redis.core;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.DockerClientFactory;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIf("dockerAvailable")
class RedisVerificationCodeStoreTest {

    static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception ex) {
            return false;
        }
    }

    private static RedisContainer redisContainer;
    private static StringRedisTemplate redisTemplate;

    @BeforeAll
    static void startRedis() {
        redisContainer = new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME);
        redisContainer.start();
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(
                redisContainer.getHost(),
                redisContainer.getFirstMappedPort());
        connectionFactory.afterPropertiesSet();
        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
    }

    @AfterAll
    static void stopRedis() {
        if (redisContainer != null) {
            redisContainer.stop();
        }
    }

    @Test
    void saveVerifyAndRateLimit() {
        RedisSmsCodeStore store = new RedisSmsCodeStore(redisTemplate, "test:login");
        store.save("13800000000", "123456", Duration.ofMinutes(5));
        assertThat(store.verifyAndConsume("13800000000", "123456")).isTrue();
        assertThat(store.verifyAndConsume("13800000000", "123456")).isFalse();
        assertThat(store.timeSinceLastSend("13800000000")).isPresent();
    }

    @Test
    void smsAndEmailKeysAreIsolated() {
        RedisSmsCodeStore smsStore = new RedisSmsCodeStore(redisTemplate, "test:login");
        RedisEmailCodeStore emailStore = new RedisEmailCodeStore(redisTemplate, "test:login");
        smsStore.save("13800000000", "111111", Duration.ofMinutes(5));
        emailStore.save("user@example.com", "222222", Duration.ofMinutes(5));
        assertThat(smsStore.verifyAndConsume("13800000000", "111111")).isTrue();
        assertThat(emailStore.verifyAndConsume("user@example.com", "222222")).isTrue();
    }
}
