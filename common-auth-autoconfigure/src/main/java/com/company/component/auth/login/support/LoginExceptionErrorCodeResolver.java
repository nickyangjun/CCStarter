package com.company.component.auth.login.support;

import com.company.component.auth.login.core.LoginAuthException;
import com.company.component.exception.core.ApiErrorResponse;
import com.company.component.exception.spi.ExceptionErrorCodeResolver;
import jakarta.servlet.http.HttpServletRequest;

import java.time.OffsetDateTime;
import java.util.Optional;

public class LoginExceptionErrorCodeResolver implements ExceptionErrorCodeResolver {

    @Override
    public Optional<ApiErrorResponse> resolve(Throwable ex, HttpServletRequest request) {
        if (!(ex instanceof LoginAuthException loginEx)) {
            return Optional.empty();
        }
        ApiErrorResponse body = new ApiErrorResponse();
        body.setCode(loginEx.getErrorCode());
        body.setMessage(loginEx.getMessage());
        body.setTimestamp(OffsetDateTime.now());
        if (request != null) {
            body.setPath(request.getRequestURI());
        }
        return Optional.of(body);
    }

    public static int httpStatus(Throwable ex) {
        if (ex instanceof LoginAuthException loginEx) {
            return loginEx.getHttpStatus();
        }
        return 500;
    }
}
