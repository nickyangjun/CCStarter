package com.company.component.log.trace;

import com.company.component.log.support.MdcKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 认证完成后将用户标识写入独立 MDC 键（不与 {@code tid} 合并）。
 */
public class MdcUserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                Object principal = authentication.getPrincipal();
                if (principal != null) {
                    String username = authentication.getName();
                    if (StringUtils.hasText(username)) {
                        MDC.put(MdcKeys.USERNAME, username);
                    }
                }
                Object details = authentication.getDetails();
                if (details != null) {
                    String userId = details.toString();
                    if (StringUtils.hasText(userId)) {
                        MDC.put(MdcKeys.USER_ID, userId);
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MdcKeys.USER_ID);
            MDC.remove(MdcKeys.USERNAME);
        }
    }
}
