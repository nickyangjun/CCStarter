package com.company.component.exception.spi;

import com.company.component.exception.core.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * 业务扩展：将领域异常解析为统一 {@link ApiErrorResponse}。
 */
public interface ExceptionErrorCodeResolver {

    Optional<ApiErrorResponse> resolve(Throwable ex, HttpServletRequest request);
}
