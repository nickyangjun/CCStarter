package com.company.component.exception.core;

import com.company.component.exception.properties.ExceptionProperties;
import com.company.component.exception.spi.ExceptionErrorCodeResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 内置异常 → {@link ApiErrorResponse} 映射；优先尝试 SPI 解析器。
 */
public class ExceptionMappingSupport {

    private static final String CODE_VALIDATION = "VALIDATION_ERROR";
    private static final String CODE_BAD_REQUEST = "BAD_REQUEST";
    private static final String CODE_NOT_FOUND = "NOT_FOUND";
    private static final String CODE_METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
    private static final String CODE_UNAUTHORIZED = "UNAUTHORIZED";
    private static final String CODE_FORBIDDEN = "FORBIDDEN";

    private final ExceptionProperties properties;

    public ExceptionMappingSupport(ExceptionProperties properties) {
        this.properties = properties;
    }

    public MappedError resolve(Throwable ex, HttpServletRequest request, List<ExceptionErrorCodeResolver> resolvers) {
        List<ExceptionErrorCodeResolver> ordered = new ArrayList<>(resolvers != null ? resolvers : Collections.emptyList());
        AnnotationAwareOrderComparator.sort(ordered);

        for (ExceptionErrorCodeResolver resolver : ordered) {
            Optional<ApiErrorResponse> resolved = resolver.resolve(ex, request);
            if (resolved.isPresent()) {
                return toMappedError(enrich(resolved.get(), ex, request), resolveHttpStatusForSpi(ex));
            }
        }

        return mapBuiltIn(ex, request);
    }

    private MappedError mapBuiltIn(Throwable ex, HttpServletRequest request) {
        if (ex instanceof MethodArgumentNotValidException valid) {
            return build(400, CODE_VALIDATION, "参数校验失败", request, toFieldErrors(valid.getBindingResult().getFieldErrors()));
        }
        if (ex instanceof BindException bind) {
            return build(400, CODE_VALIDATION, "参数绑定失败", request, toFieldErrors(bind.getBindingResult().getFieldErrors()));
        }
        if (ex instanceof MissingServletRequestParameterException missing) {
            return build(400, CODE_BAD_REQUEST, "缺少必填参数: " + missing.getParameterName(), request, Collections.emptyList());
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return build(400, CODE_BAD_REQUEST, "请求体格式错误", request, Collections.emptyList());
        }
        if (ex instanceof HttpRequestMethodNotSupportedException unsupported) {
            return build(405, CODE_METHOD_NOT_ALLOWED, "请求方法不支持: " + unsupported.getMethod(), request, Collections.emptyList());
        }
        if (ex instanceof NoHandlerFoundException notFound) {
            return build(404, CODE_NOT_FOUND, "资源不存在: " + notFound.getRequestURL(), request, Collections.emptyList());
        }
        if (ex instanceof AccessDeniedException) {
            return build(403, CODE_FORBIDDEN, "无访问权限", request, Collections.emptyList());
        }
        if (ex instanceof AuthenticationException auth) {
            String message = auth.getMessage() != null ? auth.getMessage() : "未认证或认证已失效";
            return build(401, CODE_UNAUTHORIZED, message, request, Collections.emptyList());
        }
        String code = properties.getDefaultErrorCode() != null ? properties.getDefaultErrorCode() : "INTERNAL_ERROR";
        String message = ex.getMessage() != null ? ex.getMessage() : "服务器内部错误";
        return build(500, code, message, request, Collections.emptyList());
    }

    private MappedError build(int status, String code, String message, HttpServletRequest request,
                              List<ApiErrorResponse.FieldError> errors) {
        ApiErrorResponse body = new ApiErrorResponse();
        body.setCode(code);
        body.setMessage(message);
        body.setTimestamp(OffsetDateTime.now());
        body.setErrors(errors);
        if (properties.isIncludePath() && request != null) {
            body.setPath(request.getRequestURI());
        }
        return new MappedError(status, body);
    }

    private ApiErrorResponse enrich(ApiErrorResponse body, Throwable ex, HttpServletRequest request) {
        if (body.getTimestamp() == null) {
            body.setTimestamp(OffsetDateTime.now());
        }
        if (properties.isIncludePath() && request != null && body.getPath() == null) {
            body.setPath(request.getRequestURI());
        }
        if (properties.isExposeStackTrace() && body.getStackTrace() == null) {
            body.setStackTrace(stackTraceOf(ex));
        }
        return body;
    }

    private MappedError toMappedError(ApiErrorResponse body, int httpStatus) {
        return new MappedError(httpStatus, body);
    }

    /**
     * SPI 未指定状态时，按内置规则推断 HTTP 状态。
     */
    private int resolveHttpStatusForSpi(Throwable ex) {
        if (ex instanceof MethodArgumentNotValidException || ex instanceof BindException
                || ex instanceof MissingServletRequestParameterException
                || ex instanceof HttpMessageNotReadableException) {
            return 400;
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return 405;
        }
        if (ex instanceof NoHandlerFoundException) {
            return 404;
        }
        if (ex instanceof AccessDeniedException) {
            return 403;
        }
        if (ex instanceof AuthenticationException) {
            return 401;
        }
        return 500;
    }

    public MappedError applyStackTraceIfEnabled(MappedError mapped, Throwable ex) {
        if (!properties.isExposeStackTrace()) {
            return mapped;
        }
        mapped.body().setStackTrace(stackTraceOf(ex));
        return mapped;
    }

    private static List<ApiErrorResponse.FieldError> toFieldErrors(List<FieldError> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return Collections.emptyList();
        }
        List<ApiErrorResponse.FieldError> result = new ArrayList<>(fieldErrors.size());
        for (FieldError fieldError : fieldErrors) {
            String field = fieldError.getField();
            String msg = fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "invalid";
            result.add(new ApiErrorResponse.FieldError(field, msg));
        }
        return result;
    }

    private static String stackTraceOf(Throwable ex) {
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
