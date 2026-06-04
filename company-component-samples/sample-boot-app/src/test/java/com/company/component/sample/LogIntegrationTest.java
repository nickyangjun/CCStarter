package com.company.component.sample;

import com.company.component.log.trace.TraceIdFilter;
import com.company.component.sample.config.SampleOperationLogConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LogIntegrationTest {

    private static final String GATEWAY_TRACE = "gateway-trace-integration-001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SampleOperationLogConfiguration operationLogConfiguration;

    @BeforeEach
    void setUp() {
        operationLogConfiguration.clearEntries();
    }

    @Test
    void propagatesGatewayTraceIdInResponseHeader() throws Exception {
        mockMvc.perform(get("/api/sample/ping").header(TraceIdFilter.TRACE_ID_HEADER, GATEWAY_TRACE))
                .andExpect(status().isOk())
                .andExpect(header().string(TraceIdFilter.TRACE_ID_HEADER, GATEWAY_TRACE));
    }

    @Test
    void errorResponseIncludesTraceIdFromGateway() throws Exception {
        mockMvc.perform(get("/api/sample/error/runtime").header(TraceIdFilter.TRACE_ID_HEADER, GATEWAY_TRACE))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.traceId").value(GATEWAY_TRACE));
    }

    @Test
    void unauthorizedIncludesTraceId() throws Exception {
        mockMvc.perform(get("/api/sample/secure/hello").header(TraceIdFilter.TRACE_ID_HEADER, GATEWAY_TRACE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.traceId").value(GATEWAY_TRACE));
    }

    @Test
    void operationLogRecordedAfterLogin() throws Exception {
        MvcResult loginResult = mockMvc.perform(get("/api/sample/auth/login")
                        .header(TraceIdFilter.TRACE_ID_HEADER, GATEWAY_TRACE))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = new ObjectMapper().readTree(loginResult.getResponse().getContentAsString());
        String token = json.get("token").asText();

        mockMvc.perform(post("/api/sample/orders")
                        .header(TraceIdFilter.TRACE_ID_HEADER, GATEWAY_TRACE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(operationLogConfiguration.getEntries()).hasSize(1);
        assertThat(operationLogConfiguration.getEntries().get(0).getTraceId()).isEqualTo(GATEWAY_TRACE);
        assertThat(operationLogConfiguration.getEntries().get(0).getModule()).isEqualTo("order");
        assertThat(operationLogConfiguration.getEntries().get(0).getOperatorId()).isEqualTo("1");
    }
}
