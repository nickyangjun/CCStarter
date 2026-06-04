package com.company.component.log.autoconfigure;

import com.company.component.log.operation.OperationLogAspect;
import com.company.component.log.properties.LogProperties;
import com.company.component.log.spi.OperationLogRecorder;
import com.company.component.log.trace.TraceIdResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@AutoConfiguration
@EnableConfigurationProperties(LogProperties.class)
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = "component.log", name = "enabled", havingValue = "true", matchIfMissing = false)
public class LogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TraceIdResolver componentTraceIdResolver(LogProperties properties) {
        return new TraceIdResolver(properties);
    }

    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    @ConditionalOnBean(OperationLogRecorder.class)
    @ConditionalOnMissingBean
    public OperationLogAspect componentOperationLogAspect(OperationLogRecorder recorder, LogProperties properties) {
        return new OperationLogAspect(recorder, properties);
    }
}
