package com.company.component.dict.support;

import com.company.component.dict.core.DictException;
import com.company.component.exception.core.ApiErrorResponse;
import com.company.component.exception.spi.ExceptionErrorCodeResolver;
import jakarta.servlet.http.HttpServletRequest;

import java.time.OffsetDateTime;
import java.util.Optional;

public class DictExceptionErrorCodeResolver implements ExceptionErrorCodeResolver {

    @Override
    public Optional<ApiErrorResponse> resolve(Throwable ex, HttpServletRequest request) {
        if (!(ex instanceof DictException dictEx)) {
            return Optional.empty();
        }
        ApiErrorResponse body = new ApiErrorResponse();
        body.setCode(dictEx.getErrorCode());
        body.setMessage(dictEx.getMessage());
        body.setTimestamp(OffsetDateTime.now());
        if (request != null) {
            body.setPath(request.getRequestURI());
        }
        return Optional.of(body);
    }

    public static int httpStatus(Throwable ex) {
        if (ex instanceof DictException dictEx) {
            return dictEx.getHttpStatus();
        }
        return 500;
    }
}
