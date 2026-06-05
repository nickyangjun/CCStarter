package com.company.component.sample;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    private static final String TEST_MOBILE = "13800138000";
    private static final String TEST_CODE = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whitelistPingWithoutToken() throws Exception {
        mockMvc.perform(get("/api/sample/ping"))
                .andExpect(status().isOk());
    }

    @Test
    void secureEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/sample/secure/hello"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void smsLoginThenAccessSecureEndpoint() throws Exception {
        String body = """
                {"mobile":"%s","code":"%s"}
                """.formatted(TEST_MOBILE, TEST_CODE);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/sms/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();
        JsonNode json = new ObjectMapper().readTree(loginResult.getResponse().getContentAsString());
        String token = json.get("accessToken").asText();

        mockMvc.perform(get("/api/sample/secure/hello")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("secure-ok"));
    }

    @Test
    void emailLoginThenAccessSecureEndpoint() throws Exception {
        String body = """
                {"email":"demo@example.com","code":"123456"}
                """;
        MvcResult loginResult = mockMvc.perform(post("/api/auth/email/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();
        JsonNode json = new ObjectMapper().readTree(loginResult.getResponse().getContentAsString());
        String token = json.get("accessToken").asText();

        mockMvc.perform(get("/api/sample/secure/hello")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void invalidSmsCodeReturns400() throws Exception {
        String body = """
                {"mobile":"%s","code":"000000"}
                """.formatted(TEST_MOBILE);
        mockMvc.perform(post("/api/auth/sms/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SMS_CODE"));
    }
}
