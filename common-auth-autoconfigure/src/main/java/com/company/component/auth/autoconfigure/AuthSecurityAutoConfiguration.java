package com.company.component.auth.autoconfigure;

import com.company.component.auth.core.JwtService;
import com.company.component.auth.security.ComponentAccessDeniedHandler;
import com.company.component.auth.security.ComponentAuthenticationEntryPoint;
import com.company.component.auth.security.JwtAuthenticationFilter;
import com.company.component.auth.support.AuthPathMatcher;
import com.company.component.auth.support.OrderConstants;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@AutoConfiguration(after = SecurityAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({SecurityFilterChain.class, JwtService.class})
@ConditionalOnBean(JwtService.class)
@ConditionalOnProperty(prefix = "component.auth", name = "enabled", havingValue = "true", matchIfMissing = false)
public class AuthSecurityAutoConfiguration {

    @Bean(name = "componentJwtAuthenticationFilter")
    @ConditionalOnMissingBean(JwtAuthenticationFilter.class)
    public JwtAuthenticationFilter componentJwtAuthenticationFilter(JwtService jwtService,
                                                                    com.company.component.auth.properties.AuthProperties properties,
                                                                    AuthPathMatcher pathMatcher) {
        return new JwtAuthenticationFilter(jwtService, properties, pathMatcher);
    }

    @Bean(name = "componentAuthenticationEntryPoint")
    @ConditionalOnMissingBean(ComponentAuthenticationEntryPoint.class)
    public ComponentAuthenticationEntryPoint componentAuthenticationEntryPoint() {
        return new ComponentAuthenticationEntryPoint();
    }

    @Bean(name = "componentAccessDeniedHandler")
    @ConditionalOnMissingBean(ComponentAccessDeniedHandler.class)
    public ComponentAccessDeniedHandler componentAccessDeniedHandler() {
        return new ComponentAccessDeniedHandler();
    }

    @Bean(name = "componentSecurityFilterChain")
    @Order(OrderConstants.SECURITY_FILTER_CHAIN)
    @ConditionalOnMissingBean(name = "componentSecurityFilterChain")
    public SecurityFilterChain componentSecurityFilterChain(HttpSecurity http,
                                                            JwtAuthenticationFilter jwtAuthenticationFilter,
                                                            AuthPathMatcher pathMatcher,
                                                            ComponentAuthenticationEntryPoint authenticationEntryPoint,
                                                            ComponentAccessDeniedHandler accessDeniedHandler)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    for (String pattern : pathMatcher.getPatterns()) {
                        auth.requestMatchers(AntPathRequestMatcher.antMatcher(pattern)).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
