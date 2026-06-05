package com.company.component.dict.autoconfigure;

import com.company.component.dict.cache.DictCache;
import com.company.component.dict.cache.InMemoryDictCache;
import com.company.component.dict.cache.RedisDictCache;
import com.company.component.dict.core.DictService;
import com.company.component.dict.properties.DictProperties;
import com.company.component.dict.spi.DictDataProvider;
import com.company.component.dict.support.DictExceptionErrorCodeResolver;
import com.company.component.dict.support.DictPropertiesValidator;
import com.company.component.exception.spi.ExceptionErrorCodeResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.util.ClassUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

@AutoConfiguration
@EnableConfigurationProperties(DictProperties.class)
@ConditionalOnProperty(prefix = "component.dict", name = "enabled", havingValue = "true", matchIfMissing = false)
public class DictAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DictAutoConfiguration.class);

    @Bean
    public Object componentDictStartupGuard(DictProperties properties,
                                            ObjectProvider<DictDataProvider> providerProvider,
                                            ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        DictPropertiesValidator.validate(properties);
        if (providerProvider.getIfAvailable() == null) {
            throw new IllegalStateException(
                    "DictDataProvider bean is required when component.dict.enabled=true");
        }
        if (properties.getCache().isRedis()) {
            if (!ClassUtils.isPresent("org.springframework.data.redis.core.StringRedisTemplate", null)) {
                throw new IllegalStateException(
                        "component.dict.cache.type=redis requires spring-boot-starter-data-redis on classpath");
            }
            if (redisTemplateProvider.getIfAvailable() == null) {
                throw new IllegalStateException(
                        "component.dict.cache.type=redis requires a StringRedisTemplate bean");
            }
            log.info("component.dict cache.type=redis (key-prefix={})", properties.getCache().getKeyPrefix());
        } else {
            log.info("component.dict cache.type=memory");
        }
        return new Object();
    }

    @Bean(name = "componentDictExceptionErrorCodeResolver")
    @ConditionalOnClass(ExceptionErrorCodeResolver.class)
    @ConditionalOnMissingBean(name = "componentDictExceptionErrorCodeResolver")
    public ExceptionErrorCodeResolver componentDictExceptionErrorCodeResolver() {
        return new DictExceptionErrorCodeResolver();
    }

    @Bean(name = "componentDictCache")
    @ConditionalOnMissingBean(DictCache.class)
    @ConditionalOnProperty(prefix = "component.dict.cache", name = "type", havingValue = "memory", matchIfMissing = true)
    public DictCache componentInMemoryDictCache(DictProperties properties) {
        log.debug("Using InMemoryDictCache for component.dict");
        return new InMemoryDictCache(properties);
    }

    @Bean(name = "componentDictCache")
    @ConditionalOnMissingBean(DictCache.class)
    @ConditionalOnProperty(prefix = "component.dict.cache", name = "type", havingValue = "redis")
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnBean(StringRedisTemplate.class)
    public DictCache componentRedisDictCache(StringRedisTemplate redisTemplate,
                                             ObjectMapper objectMapper,
                                             DictProperties properties) {
        log.info("Using RedisDictCache for component.dict (key-prefix={})",
                properties.getCache().getKeyPrefix());
        return new RedisDictCache(redisTemplate, objectMapper, properties);
    }

    @Bean
    @ConditionalOnBean({DictCache.class, DictDataProvider.class})
    public DictService componentDictService(DictProperties properties,
                                            DictCache dictCache,
                                            DictDataProvider dataProvider) {
        return new DictService(properties, dictCache, dataProvider);
    }
}
