package com.company.component.log.support;

import org.springframework.core.Ordered;

/**
 * Servlet Filter 顺序：TraceId 早于 Security（-100），用户 MDC 与请求摘要在其后。
 */
public final class OrderConstants {

    public static final int TRACE_ID_FILTER = Ordered.HIGHEST_PRECEDENCE + 20;

    /** 晚于 {@code SecurityFilterChain}（默认 -100）。 */
    public static final int MDC_USER_CONTEXT_FILTER = -95;

    public static final int REQUEST_LOGGING_FILTER = -90;

    private OrderConstants() {
    }
}
