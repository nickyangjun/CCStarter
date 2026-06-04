package com.company.component.exception.web;

import com.company.component.exception.core.ApiErrorResponse;
import com.company.component.exception.core.ExceptionMappingSupport;
import com.company.component.exception.core.MappedError;
import com.company.component.exception.spi.ExceptionErrorCodeResolver;
import com.company.component.exception.support.OrderConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;

/**
 * 全局异常处理，统一返回 {@link ApiErrorResponse} JSON。
 */
@RestControllerAdvice
@Order(OrderConstants.EXCEPTION_ADVICE)
public class ComponentGlobalExceptionHandler {

    private final ExceptionMappingSupport mappingSupport;
    private final List<ExceptionErrorCodeResolver> resolvers;

    public ComponentGlobalExceptionHandler(ExceptionMappingSupport mappingSupport,
                                         List<ExceptionErrorCodeResolver> resolvers) {
        this.mappingSupport = mappingSupport;
        this.resolvers = resolvers != null ? resolvers : Collections.emptyList();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        MappedError mapped = mappingSupport.resolve(ex, request, resolvers);
        mapped = mappingSupport.applyStackTraceIfEnabled(mapped, ex);
        return ResponseEntity.status(mapped.httpStatus()).body(mapped.body());
    }
}
