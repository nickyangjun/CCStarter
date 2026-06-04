package com.company.component.log.request;

import com.company.component.log.properties.LogProperties;
import com.company.component.log.support.LogPathMatcher;
import com.company.component.log.support.MdcKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 请求结束打一条 SLF4J 摘要（不落库）。
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private final LogProperties.Request requestProperties;
    private final LogPathMatcher pathMatcher;
    private final Level logLevel;

    public RequestLoggingFilter(LogProperties properties, LogPathMatcher pathMatcher) {
        this.requestProperties = properties.getRequest();
        this.pathMatcher = pathMatcher;
        this.logLevel = parseLevel(requestProperties.getLogLevel());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !requestProperties.isEnabled() || pathMatcher.matchesExclude(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (shouldLog()) {
                long durationMs = System.currentTimeMillis() - start;
                String uri = buildUri(request);
                logAtLevel(logLevel,
                        "request method={} uri={} status={} durationMs={} traceId={} userId={}",
                        request.getMethod(),
                        uri,
                        response.getStatus(),
                        durationMs,
                        nullToDash(MDC.get(MdcKeys.TID)),
                        nullToDash(MDC.get(MdcKeys.USER_ID)));
            }
        }
    }

    private boolean shouldLog() {
        if (logLevel == Level.DEBUG) {
            return log.isDebugEnabled();
        }
        if (logLevel == Level.INFO) {
            if (!log.isInfoEnabled()) {
                return false;
            }
            double rate = requestProperties.getSampleRate();
            return rate >= 1.0d || ThreadLocalRandom.current().nextDouble() < rate;
        }
        if (logLevel == Level.WARN) {
            return log.isWarnEnabled();
        }
        if (logLevel == Level.ERROR) {
            return log.isErrorEnabled();
        }
        return log.isTraceEnabled();
    }

    private void logAtLevel(Level level, String format, Object... args) {
        switch (level) {
            case DEBUG -> log.debug(format, args);
            case INFO -> log.info(format, args);
            case WARN -> log.warn(format, args);
            case ERROR -> log.error(format, args);
            default -> log.trace(format, args);
        }
    }

    private String buildUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (requestProperties.isIncludeQuery() && request.getQueryString() != null) {
            return uri + "?" + request.getQueryString();
        }
        return uri;
    }

    private static Level parseLevel(String configured) {
        if (configured == null) {
            return Level.DEBUG;
        }
        try {
            return Level.valueOf(configured.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Level.DEBUG;
        }
    }

    private static String nullToDash(String value) {
        return value != null ? value : "-";
    }
}
