package com.company.component.exception.autoconfigure;

import com.company.component.exception.core.ExceptionMappingSupport;
import com.company.component.exception.properties.ExceptionProperties;
import com.company.component.exception.spi.ExceptionErrorCodeResolver;
import com.company.component.exception.web.ComponentGlobalExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 统一异常组件自动配置。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ExceptionProperties.class)
@ConditionalOnProperty(
        prefix = "component.exception",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class ExceptionAutoConfiguration {

    @Bean(name = "componentExceptionMappingSupport")
    @ConditionalOnMissingBean(ExceptionMappingSupport.class)
    public ExceptionMappingSupport componentExceptionMappingSupport(ExceptionProperties properties) {
        return new ExceptionMappingSupport(properties);
    }

    @Bean(name = "componentGlobalExceptionHandler")
    @ConditionalOnMissingBean(ComponentGlobalExceptionHandler.class)
    public ComponentGlobalExceptionHandler componentGlobalExceptionHandler(
            ExceptionMappingSupport mappingSupport,
            ObjectProvider<ExceptionErrorCodeResolver> resolverProvider) {
        List<ExceptionErrorCodeResolver> resolvers = resolverProvider.orderedStream().toList();
        return new ComponentGlobalExceptionHandler(mappingSupport, resolvers);
    }
}
