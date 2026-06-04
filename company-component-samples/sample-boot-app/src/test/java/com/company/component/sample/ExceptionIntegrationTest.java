package com.company.component.sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExceptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void runtimeErrorReturnsUnifiedJson() throws Exception {
        mockMvc.perform(get("/api/sample/error/runtime"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("sample runtime error"))
                .andExpect(jsonPath("$.path").value("/api/sample/error/runtime"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void missingParameterReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sample/error/missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.path").value("/api/sample/error/missing-param"));
    }

    @Test
    void notFoundReturnsUnifiedJson() throws Exception {
        mockMvc.perform(get("/api/sample/not-exists"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
