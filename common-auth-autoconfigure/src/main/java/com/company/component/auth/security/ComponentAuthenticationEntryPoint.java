package com.company.component.auth.security;

import com.company.component.exception.support.ApiErrorResponseHttpWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 未认证时返回统一 JSON（401）。
 */
public class ComponentAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String message = authException.getMessage() != null ? authException.getMessage() : "未认证或认证已失效";
        ApiErrorResponseHttpWriter.write(response, 401, "UNAUTHORIZED", message, request.getRequestURI());
    }
}
