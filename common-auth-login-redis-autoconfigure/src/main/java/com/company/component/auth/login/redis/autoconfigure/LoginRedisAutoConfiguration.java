package com.company.component.auth.login.redis.autoconfigure;

import com.company.component.auth.login.autoconfigure.LoginAutoConfiguration;
import com.company.component.auth.login.properties.LoginProperties;
import com.company.component.auth.login.redis.core.RedisEmailCodeStore;
import com.company.component.auth.login.redis.core.RedisSmsCodeStore;
import com.company.component.auth.login.redis.properties.LoginRedisProperties;
import com.company.component.auth.login.spi.EmailCodeStore;
import com.company.component.auth.login.spi.SmsCodeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

@AutoConfiguration(before = LoginAutoConfiguration.class,
        after = org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
@EnableConfigurationProperties(LoginRedisProperties.class)
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnBean(StringRedisTemplate.class)
@ConditionalOnProperty(prefix = "component.auth.login.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoginRedisAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LoginRedisAutoConfiguration.class);

    @Bean
    public Object componentLoginRedisStartupGuard(LoginRedisProperties redisProperties,
                                                  LoginProperties loginProperties,
                                                  ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        if (!loginProperties.isEnabled()) {
            return new Object();
        }
        if (!ClassUtils.isPresent("org.springframework.data.redis.core.StringRedisTemplate", null)) {
            throw new IllegalStateException(
                    "component.auth.login.redis.enabled requires spring-boot-starter-data-redis on classpath");
        }
        if (redisTemplateProvider.getIfAvailable() == null) {
            throw new IllegalStateException(
                    "component.auth.login.redis.enabled requires a StringRedisTemplate bean");
        }
        if (!StringUtils.hasText(redisProperties.getKeyPrefix())) {
            throw new IllegalStateException("component.auth.login.redis.key-prefix must not be blank");
        }
        log.info("component.auth.login.redis enabled (key-prefix={})", redisProperties.getKeyPrefix());
        return new Object();
    }

    @Bean(name = "componentSmsCodeStore")
    @ConditionalOnMissingBean(SmsCodeStore.class)
    @ConditionalOnProperty(prefix = "component.auth.login.sms", name = "enabled", havingValue = "true")
    public SmsCodeStore componentRedisSmsCodeStore(StringRedisTemplate redisTemplate,
                                                   LoginRedisProperties redisProperties) {
        log.info("Using RedisSmsCodeStore for login SMS codes");
        return new RedisSmsCodeStore(redisTemplate, redisProperties.getKeyPrefix());
    }

    @Bean(name = "componentEmailCodeStore")
    @ConditionalOnMissingBean(EmailCodeStore.class)
    @ConditionalOnProperty(prefix = "component.auth.login.email", name = "enabled", havingValue = "true")
    public EmailCodeStore componentRedisEmailCodeStore(StringRedisTemplate redisTemplate,
                                                       LoginRedisProperties redisProperties) {
        log.info("Using RedisEmailCodeStore for login email codes");
        return new RedisEmailCodeStore(redisTemplate, redisProperties.getKeyPrefix());
    }
}
