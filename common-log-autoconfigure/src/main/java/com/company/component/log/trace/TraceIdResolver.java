package com.company.component.log.trace;

import com.company.component.log.properties.LogProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 从请求头解析 TraceId；无上游头时按配置自建。禁止在已有上游头时重新生成。
 */
public class TraceIdResolver {

    private static final Logger log = LoggerFactory.getLogger(TraceIdResolver.class);

    /** 疑似将 userId 拼进 TraceId 的前缀（仅告警，仍透传）。 */
    private static final Pattern SUSPECT_USER_PREFIX = Pattern.compile("^\\d{4,}-.+");

    private final LogProperties.Trace traceProperties;

    public TraceIdResolver(LogProperties properties) {
        this.traceProperties = properties.getTrace();
    }

    public String resolve(HttpServletRequest request) {
        for (String headerName : traceProperties.getHeaderNames()) {
            String value = request.getHeader(headerName);
            if (StringUtils.hasText(value)) {
                String trimmed = value.trim();
                warnIfSuspectBusinessComposite(trimmed);
                return trimmed;
            }
        }
        if (traceProperties.isAllowLocalGenerate()) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return null;
    }

    private void warnIfSuspectBusinessComposite(String traceId) {
        if (SUSPECT_USER_PREFIX.matcher(traceId).matches()) {
            log.warn("TraceId [{}] may embed business/user prefix; gateway should send opaque id only", traceId);
        }
    }
}
