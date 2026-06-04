package com.company.component.log.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Collections;
import java.util.List;

/**
 * Ant 路径匹配，用于请求日志等排除路径。
 */
public final class LogPathMatcher {

    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> excludePatterns;

    public LogPathMatcher(List<String> excludePatterns) {
        this.excludePatterns = excludePatterns != null ? List.copyOf(excludePatterns) : Collections.emptyList();
    }

    public boolean matchesExclude(HttpServletRequest request) {
        if (excludePatterns.isEmpty()) {
            return false;
        }
        String path = request.getRequestURI();
        for (String pattern : excludePatterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
