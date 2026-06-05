package com.company.component.auth.autoconfigure;

import com.company.component.auth.core.JwtService;
import com.company.component.auth.login.properties.LoginProperties;
import com.company.component.auth.login.support.LoginPathCollector;
import com.company.component.auth.properties.AuthProperties;
import com.company.component.auth.spi.JwtClaimsCustomizer;
import com.company.component.auth.support.AuthPathMatcher;
import com.company.component.dict.properties.DictProperties;
import com.company.component.dict.support.DictPathCollector;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "io.jsonwebtoken.Jwts")
@EnableConfigurationProperties({AuthProperties.class, LoginProperties.class})
@ConditionalOnProperty(prefix = "component.auth", name = "enabled", havingValue = "true", matchIfMissing = false)
public class AuthAutoConfiguration {

    @Bean(name = "componentAuthPathMatcher")
    @ConditionalOnMissingBean(AuthPathMatcher.class)
    public AuthPathMatcher componentAuthPathMatcher(AuthProperties properties,
                                                    LoginProperties loginProperties,
                                                    ObjectProvider<DictProperties> dictPropertiesProvider) {
        List<String> merged = new ArrayList<>(properties.getWhitelist());
        merged.addAll(LoginPathCollector.collect(loginProperties));
        DictProperties dictProperties = dictPropertiesProvider.getIfAvailable();
        if (dictProperties != null) {
            merged.addAll(DictPathCollector.collect(dictProperties));
        }
        return new AuthPathMatcher(merged);
    }

    @Bean(name = "componentJwtService")
    @ConditionalOnMissingBean(JwtService.class)
    public JwtService componentJwtService(AuthProperties properties,
                                          ObjectProvider<JwtClaimsCustomizer> customizerProvider) {
        List<JwtClaimsCustomizer> customizers = customizerProvider.orderedStream().toList();
        return new JwtService(properties, customizers);
    }
}
