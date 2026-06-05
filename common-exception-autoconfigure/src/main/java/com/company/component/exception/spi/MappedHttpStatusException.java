package com.company.component.exception.spi;

/**
 * 领域异常可指定 HTTP 状态，供全局异常映射使用。
 */
public interface MappedHttpStatusException {

    int getHttpStatus();
}
