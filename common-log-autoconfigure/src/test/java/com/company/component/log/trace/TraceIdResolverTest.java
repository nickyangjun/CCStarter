package com.company.component.log.trace;

import com.company.component.log.properties.LogProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdResolverTest {

    private LogProperties properties;
    private TraceIdResolver resolver;

    @BeforeEach
    void setUp() {
        properties = new LogProperties();
        properties.setEnabled(true);
        resolver = new TraceIdResolver(properties);
    }

    @Test
    void usesGatewayHeaderWhenPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "gateway-trace-001");
        assertThat(resolver.resolve(request)).isEqualTo("gateway-trace-001");
    }

    @Test
    void doesNotRegenerateWhenHeaderPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "fixed-id");
        assertThat(resolver.resolve(request)).isEqualTo("fixed-id");
        assertThat(resolver.resolve(request)).isEqualTo("fixed-id");
    }

    @Test
    void generatesWhenNoHeaderAndAllowed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String id = resolver.resolve(request);
        assertThat(id).isNotBlank().doesNotContain("-");
    }

    @Test
    void returnsNullWhenNoHeaderAndGenerateDisabled() {
        properties.getTrace().setAllowLocalGenerate(false);
        resolver = new TraceIdResolver(properties);
        assertThat(resolver.resolve(new MockHttpServletRequest())).isNull();
    }
}
