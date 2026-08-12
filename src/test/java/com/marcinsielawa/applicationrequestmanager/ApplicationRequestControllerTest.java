package com.marcinsielawa.applicationrequestmanager;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springdoc.core.service.AbstractRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcinsielawa.applicationrequestmanager.core.ApplicationRequestService;
import com.marcinsielawa.applicationrequestmanager.core.Command;
import com.marcinsielawa.interview.CreateApplicationRequest;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(ApplicationRequestController.class)
@ExtendWith(MockitoExtension.class)
class ApplicationRequestControllerTest {
    
    @Autowired
    MockMvc mvc;
    
    ObjectMapper objectMapper = new ObjectMapper();
    
    @MockitoBean
    ApplicationRequestService service;

    @Test
    @DisplayName("We dont accept empty strings in name nor body")
    void testBasicValidation() throws Exception {
        mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "", "body", ""))))
        .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Web passes parameters to the service")
    void passParametersToTheService() throws Exception {
        
        mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "foo", "body", "bar"))))
        .andExpect(status().isCreated());
        
        verify(service).process(any(Command.CreateApplication.class));
    }

}
