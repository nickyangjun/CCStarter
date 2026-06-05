package com.company.component.dict.cache;

import com.company.component.dict.core.DictItem;
import com.company.component.dict.properties.DictProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.testcontainers.DockerClientFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIf("dockerAvailable")
class RedisDictCacheTest {

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
    void putGetAndEvict() {
        DictProperties properties = new DictProperties();
        properties.getCache().setKeyPrefix("test:dict");
        properties.getCache().setTtlSeconds(300);
        RedisDictCache cache = new RedisDictCache(redisTemplate, new ObjectMapper(), properties);

        List<DictItem> items = List.of(DictItem.of("gender", "1", "男", "M", 1, null, null));
        cache.put("gender", items);
        assertThat(cache.get("gender")).isPresent().get().asList().hasSize(1);

        cache.evict("gender");
        assertThat(cache.get("gender")).isEmpty();
    }

    @Test
    void emptyListCachedWithMarker() {
        DictProperties properties = new DictProperties();
        properties.getCache().setKeyPrefix("test:dict-empty");
        properties.getCache().setNullTtlSeconds(60);
        RedisDictCache cache = new RedisDictCache(redisTemplate, new ObjectMapper(), properties);

        cache.put("unknown", List.of());
        assertThat(cache.get("unknown")).isPresent().get().asList().isEmpty();
    }
}
