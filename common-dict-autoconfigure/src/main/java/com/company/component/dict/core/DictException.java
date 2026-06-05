package com.company.component.dict.core;

import com.company.component.exception.spi.MappedHttpStatusException;

/**
 * 字典领域异常，由 {@link com.company.component.dict.support.DictExceptionErrorCodeResolver} 映射为统一 JSON。
 */
public class DictException extends RuntimeException implements MappedHttpStatusException {

    private final String errorCode;
    private final int httpStatus;

    public DictException(String errorCode, int httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    public static DictException entryNotFound(String dictType, String code) {
        return new DictException("DICT_ENTRY_NOT_FOUND", 404,
                "字典项不存在: type=" + dictType + ", code=" + code);
    }

    public static DictException loadFailed(String dictType, String detail) {
        String message = "字典加载失败: type=" + dictType;
        if (detail != null && !detail.isBlank()) {
            message = message + ", " + detail;
        }
        return new DictException("DICT_LOAD_FAILED", 502, message);
    }

    public static DictException invalidDictType(String dictType) {
        return new DictException("DICT_TYPE_INVALID", 400, "字典类型无效: " + dictType);
    }
}
