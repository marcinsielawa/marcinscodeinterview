package com.marcinsielawa.applicationrequestmanager;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ApplicationRequestController.class)
class ApplicationRequestControllerTest {
    
    @Autowired
    MockMvc mvc;
    
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("We dont accept empty strings in name nor body")
    void testBasicValidation() throws Exception {
        mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "", "body", ""))))
        .andExpect(status().isBadRequest());
    }

}
