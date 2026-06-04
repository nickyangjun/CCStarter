package com.company.component.exception.core;

/**
 * 异常映射结果：HTTP 状态 + 统一响应体。
 */
public record MappedError(int httpStatus, ApiErrorResponse body) {
}
