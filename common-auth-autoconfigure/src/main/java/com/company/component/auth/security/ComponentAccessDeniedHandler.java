package com.company.component.auth.security;

import com.company.component.exception.support.ApiErrorResponseHttpWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * 无权限时返回统一 JSON（403）。
 */
public class ComponentAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ApiErrorResponseHttpWriter.write(response, 403, "FORBIDDEN", "无访问权限", request.getRequestURI());
    }
}
