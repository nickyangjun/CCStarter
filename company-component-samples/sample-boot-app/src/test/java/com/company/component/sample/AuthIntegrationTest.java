package com.company.component.sample;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

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
    void loginThenAccessSecureEndpoint() throws Exception {
        MvcResult loginResult = mockMvc.perform(get("/api/sample/auth/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        JsonNode json = new ObjectMapper().readTree(loginResult.getResponse().getContentAsString());
        String token = json.get("token").asText();

        mockMvc.perform(get("/api/sample/secure/hello")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("secure-ok"));
    }
}
