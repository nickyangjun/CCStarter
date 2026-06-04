package com.company.component.auth.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 白名单路径匹配（Ant 风格）。
 */
public final class AuthPathMatcher {

    private static final List<String> DEFAULT_WHITELIST = List.of("/error", "/actuator/health");

    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> patterns;

    public AuthPathMatcher(List<String> configured) {
        Set<String> merged = new LinkedHashSet<>(DEFAULT_WHITELIST);
        if (configured != null) {
            merged.addAll(configured);
        }
        this.patterns = new ArrayList<>(merged);
    }

    public boolean matches(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getPatterns() {
        return List.copyOf(patterns);
    }
}
