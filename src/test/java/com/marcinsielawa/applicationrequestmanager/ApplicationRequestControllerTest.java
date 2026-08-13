package com.marcinsielawa.applicationrequestmanager;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springdoc.core.service.AbstractRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcinsielawa.applicationrequestmanager.core.ApplicationAggregate;
import com.marcinsielawa.applicationrequestmanager.core.ApplicationRequestService;
import com.marcinsielawa.applicationrequestmanager.core.ApplicationState;
import com.marcinsielawa.applicationrequestmanager.core.Command;
import com.marcinsielawa.applicationrequestmanager.core.Event;
import com.marcinsielawa.applicationrequestmanager.core.Result;
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
    
    @BeforeEach
    void before() {
        
        when(service.process(any(Command.class))).thenAnswer(new Answer<Result>() {
            @Override
            public Result answer(InvocationOnMock invocation) throws Throwable {
                Event e = new Event.ApplicationCreated(UUID.randomUUID().toString(), "name", "body", OffsetDateTime.now());
                Result.Success<Event> res = new Result.Success<>(e);
                return res;
            }});
    }

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
        
        verify(service).process(any(Command.Create.class));
    }

}
