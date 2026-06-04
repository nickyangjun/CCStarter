package com.company.component.log.autoconfigure;

import com.company.component.log.properties.LogProperties;
import com.company.component.log.request.RequestLoggingFilter;
import com.company.component.log.support.LogPathMatcher;
import com.company.component.log.support.OrderConstants;
import com.company.component.log.trace.MdcUserContextFilter;
import com.company.component.log.trace.TraceIdFilter;
import com.company.component.log.trace.TraceIdResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = LogAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "component.log", name = "enabled", havingValue = "true", matchIfMissing = false)
public class LogWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LogPathMatcher componentLogPathMatcher(LogProperties properties) {
        return new LogPathMatcher(properties.getRequest().getExcludePaths());
    }

    @Bean
    @ConditionalOnMissingBean(name = "componentTraceIdFilter")
    public FilterRegistrationBean<TraceIdFilter> componentTraceIdFilter(TraceIdResolver traceIdResolver,
                                                                        LogProperties properties) {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter(traceIdResolver, properties));
        registration.setOrder(OrderConstants.TRACE_ID_FILTER);
        registration.setName("componentTraceIdFilter");
        return registration;
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
    @ConditionalOnMissingBean(name = "componentMdcUserContextFilter")
    public FilterRegistrationBean<MdcUserContextFilter> componentMdcUserContextFilter() {
        FilterRegistrationBean<MdcUserContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MdcUserContextFilter());
        registration.setOrder(OrderConstants.MDC_USER_CONTEXT_FILTER);
        registration.setName("componentMdcUserContextFilter");
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(name = "componentRequestLoggingFilter")
    public FilterRegistrationBean<RequestLoggingFilter> componentRequestLoggingFilter(LogProperties properties,
                                                                                      LogPathMatcher pathMatcher) {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLoggingFilter(properties, pathMatcher));
        registration.setOrder(OrderConstants.REQUEST_LOGGING_FILTER);
        registration.setName("componentRequestLoggingFilter");
        return registration;
    }
}
