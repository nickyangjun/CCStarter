package com.company.component.exception.core;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 统一 API 错误响应体（Phase 3 由全局异常处理器填充）。
 */
public class ApiErrorResponse {

    private String code;
    private String message;
    private OffsetDateTime timestamp;
    private String path;
    private String traceId;
    private List<FieldError> errors = Collections.emptyList();
    private String stackTrace;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldError> errors) {
        this.errors = errors != null ? errors : Collections.emptyList();
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public static class FieldError {

        private String field;
        private String message;

        public FieldError() {
        }

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
