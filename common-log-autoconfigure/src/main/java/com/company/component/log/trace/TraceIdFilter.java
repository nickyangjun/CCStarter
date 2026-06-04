package com.company.component.log.trace;

import com.company.component.log.properties.LogProperties;
import com.company.component.log.support.MdcKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 解析/透传 TraceId 写入 MDC {@code tid}，请求结束清理 MDC。
 */
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final TraceIdResolver traceIdResolver;
    private final LogProperties.Trace traceProperties;

    public TraceIdFilter(TraceIdResolver traceIdResolver, LogProperties properties) {
        this.traceIdResolver = traceIdResolver;
        this.traceProperties = properties.getTrace();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String traceId = traceIdResolver.resolve(request);
            if (StringUtils.hasText(traceId)) {
                MDC.put(MdcKeys.TID, traceId);
                if (traceProperties.isResponseHeader()) {
                    response.setHeader(TRACE_ID_HEADER, traceId);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MdcKeys.TID);
            MDC.remove(MdcKeys.USER_ID);
            MDC.remove(MdcKeys.USERNAME);
        }
    }
}
