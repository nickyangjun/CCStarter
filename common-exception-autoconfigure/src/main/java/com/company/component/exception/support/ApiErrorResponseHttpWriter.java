package com.company.component.exception.support;

import com.company.component.exception.core.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

/**
 * 将 {@link ApiErrorResponse} 写入 HTTP 响应（供 Security 入口等 Filter 链场景使用）。
 */
public final class ApiErrorResponseHttpWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private ApiErrorResponseHttpWriter() {
    }

    public static void write(HttpServletResponse response, int httpStatus, String code, String message, String path)
            throws IOException {
        ApiErrorResponse body = new ApiErrorResponse();
        body.setCode(code);
        body.setMessage(message);
        body.setTimestamp(OffsetDateTime.now());
        body.setPath(path);
        TraceIdMdcSupport.applyTraceId(body, true);
        write(response, httpStatus, body);
    }

    public static void write(HttpServletResponse response, int httpStatus, ApiErrorResponse body) throws IOException {
        response.setStatus(httpStatus);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        MAPPER.writeValue(response.getOutputStream(), body);
    }
}
