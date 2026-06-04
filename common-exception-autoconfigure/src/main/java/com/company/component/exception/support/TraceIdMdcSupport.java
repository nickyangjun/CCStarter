package com.company.component.exception.support;

import com.company.component.exception.core.ApiErrorResponse;
import org.slf4j.MDC;

/**
 * 从 MDC 读取链路 ID，与 {@code docs/architecture/logging.md} 键名 {@code tid} 一致。
 */
public final class TraceIdMdcSupport {

    public static final String MDC_KEY = "tid";

    private TraceIdMdcSupport() {
    }

    public static String currentTraceId() {
        return MDC.get(MDC_KEY);
    }

    public static void applyTraceId(ApiErrorResponse body, boolean includeTraceId) {
        if (includeTraceId && body != null) {
            body.setTraceId(currentTraceId());
        }
    }
}
