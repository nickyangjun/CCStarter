package com.company.component.exception.core;

import com.company.component.exception.properties.ExceptionProperties;
import com.company.component.exception.spi.ExceptionErrorCodeResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExceptionMappingSupportTest {

    private ExceptionProperties properties;
    private ExceptionMappingSupport support;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        properties = new ExceptionProperties();
        properties.setEnabled(true);
        properties.setIncludePath(true);
        properties.setDefaultErrorCode("INTERNAL_ERROR");
        support = new ExceptionMappingSupport(properties);
        request = new MockHttpServletRequest("GET", "/api/test");
    }

    @Test
    void mapsRuntimeExceptionTo500() {
        MappedError mapped = support.resolve(new RuntimeException("boom"), request, List.of());
        assertThat(mapped.httpStatus()).isEqualTo(500);
        assertThat(mapped.body().getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(mapped.body().getMessage()).isEqualTo("boom");
        assertThat(mapped.body().getPath()).isEqualTo("/api/test");
        assertThat(mapped.body().getTimestamp()).isNotNull();
    }

    @Test
    void mapsMissingParameterTo400() {
        var ex = new MissingServletRequestParameterException("name", "String");
        MappedError mapped = support.resolve(ex, request, List.of());
        assertThat(mapped.httpStatus()).isEqualTo(400);
        assertThat(mapped.body().getCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void mapsNoHandlerFoundTo404() {
        var ex = new NoHandlerFoundException("GET", "/missing", null);
        MappedError mapped = support.resolve(ex, request, List.of());
        assertThat(mapped.httpStatus()).isEqualTo(404);
        assertThat(mapped.body().getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    void spiResolverTakesPrecedence() {
        ExceptionErrorCodeResolver resolver = mock(ExceptionErrorCodeResolver.class);
        ApiErrorResponse custom = new ApiErrorResponse();
        custom.setCode("BIZ_001");
        custom.setMessage("业务错误");
        when(resolver.resolve(any(), any())).thenReturn(Optional.of(custom));

        MappedError mapped = support.resolve(new RuntimeException("x"), request, List.of(resolver));
        assertThat(mapped.body().getCode()).isEqualTo("BIZ_001");
        assertThat(mapped.body().getMessage()).isEqualTo("业务错误");
        assertThat(mapped.body().getTimestamp()).isNotNull();
    }

    @Test
    void mapsAuthenticationExceptionTo401() {
        MappedError mapped = support.resolve(new BadCredentialsException("bad creds"), request, List.of());
        assertThat(mapped.httpStatus()).isEqualTo(401);
        assertThat(mapped.body().getCode()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    void mapsAccessDeniedTo403() {
        MappedError mapped = support.resolve(new AccessDeniedException("denied"), request, List.of());
        assertThat(mapped.httpStatus()).isEqualTo(403);
        assertThat(mapped.body().getCode()).isEqualTo("FORBIDDEN");
    }

    @Test
    void exposeStackTraceAddsStackTraceField() {
        properties.setExposeStackTrace(true);
        MappedError mapped = support.resolve(new RuntimeException("x"), request, List.of());
        mapped = support.applyStackTraceIfEnabled(mapped, new RuntimeException("x"));
        assertThat(mapped.body().getStackTrace()).contains("RuntimeException");
    }
}
