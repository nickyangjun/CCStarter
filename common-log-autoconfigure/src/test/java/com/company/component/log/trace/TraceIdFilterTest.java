package com.company.component.log.trace;

import com.company.component.log.properties.LogProperties;
import com.company.component.log.support.MdcKeys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdFilterTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void putsTidInMdcAndResponseHeader() throws Exception {
        LogProperties properties = new LogProperties();
        properties.setEnabled(true);
        TraceIdResolver resolver = new TraceIdResolver(properties);
        TraceIdFilter filter = new TraceIdFilter(resolver, properties);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "trace-abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String[] capturedTid = new String[1];
        FilterChain chain = (req, res) -> capturedTid[0] = MDC.get(MdcKeys.TID);

        filter.doFilter(request, response, chain);

        assertThat(capturedTid[0]).isEqualTo("trace-abc");
        assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isEqualTo("trace-abc");
        assertThat(MDC.get(MdcKeys.TID)).isNull();
    }
}
