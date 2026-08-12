package com.marcinsielawa.applicationrequestmanager.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationEntity;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationRepository;

@SpringBootTest(classes = ApplicationRequestServiceImpl.class)
class UseCaseTests {
    
    ApplicationRequestService service;
    
    @MockitoBean
    ApplicationRepository applicationRepository;
    
    @MockitoBean
    ApplicationEventPublisher applicationEventPublisher;
    
    @BeforeEach
    void before() {
        service = new ApplicationRequestServiceImpl(applicationRepository, applicationEventPublisher);
    }
    
    @Test
    @DisplayName("Create application use case - happy path")
    void test() {
        Result<?> result = service.process(new Command.CreateApplication("foo", "bar"));
        
        assertEquals(Result.Success.class, result.getClass());
        verify(applicationRepository).save(any(ApplicationEntity.class));
    }

}
