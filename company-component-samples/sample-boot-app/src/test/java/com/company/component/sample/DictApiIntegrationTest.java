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
class DictApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getGenderDictWithoutToken() throws Exception {
        mockMvc.perform(get("/api/dict/gender"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dictType").value("gender"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].code").value("1"))
                .andExpect(jsonPath("$.items[0].label").value("男"))
                .andExpect(jsonPath("$.items[0].value").value("M"));
    }

    @Test
    void getUnknownDictReturnsEmptyItems() throws Exception {
        mockMvc.perform(get("/api/dict/not_exists_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dictType").value("not_exists_type"))
                .andExpect(jsonPath("$.items.length()").value(0));
    }
}
