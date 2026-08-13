package com.marcinsielawa.applicationrequestmanager.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationAccepted;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationPublished;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationVerified;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationEntity;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationRepository;
import com.marcinsielawa.applicationrequestmanager.persistence.PublishingIdGenerator;

@SpringBootTest(classes = ApplicationRequestServiceImpl.class)
@ExtendWith(MockitoExtension.class)
class PublishUseCaseTests {
    
    ApplicationRequestService service;
    
    @MockitoBean
    ApplicationRepository applicationRepository;
    
    @MockitoBean
    ApplicationEventPublisher applicationEventPublisher;
    
    @MockitoBean
    PublishingIdGenerator publishingIdGenerator;
    
    @BeforeEach
    void before() {
        service = new ApplicationRequestServiceImpl(applicationRepository, applicationEventPublisher, publishingIdGenerator);
    }

    @Test
    @DisplayName("Publish application use case - happy path ACCEPTED > PUBLISHED")
    void testPublishUseCaseHappyPath() {
        
        ApplicationEntity testEntity = new ApplicationEntity(
                UUID.randomUUID().toString(), "name", "body", ApplicationState.ACCEPTED, OffsetDateTime.now());
        
        when(applicationRepository.findById(testEntity.getId())).thenReturn(Optional.of(testEntity));
        
        ArgumentCaptor<ApplicationEntity> entityCaptor = ArgumentCaptor.forClass(testEntity.getClass());
        
        Result result = service.process(new Command.Publish(testEntity.getId()));

        verify(applicationRepository).save(entityCaptor.capture());

        ApplicationEntity capturedEntity = entityCaptor.getValue();

        assertEquals(Result.Success.class, result.getClass());
        
        assertEquals(ApplicationState.PUBLISHED, capturedEntity.getState());
        assertNotNull(capturedEntity.getPublishingId());
        verify(applicationEventPublisher).publishEvent(any(ApplicationPublished.class));
    }
    
    @Test
    @DisplayName("Publish application use case - wrong status (expect ACCEPTED)")
    void testPublishOnlyCreated() {
        
        ApplicationEntity testEntity = new ApplicationEntity(
                UUID.randomUUID().toString(), "name", "body", ApplicationState.CREATED, OffsetDateTime.now());
        
        when(applicationRepository.findById(testEntity.getId())).thenReturn(Optional.of(testEntity));
        
        Result result = service.process(new Command.Accept(testEntity.getId()));
        
        assertEquals(Result.BusinessRuleViolation.class, result.getClass());
        verify(applicationRepository, never()).save(any(ApplicationEntity.class));
        verifyNoInteractions(applicationEventPublisher);
    }
    
    @Test
    @DisplayName("Publish application use case - not found")
    void testPublishNotFound() {
        ApplicationEntity testEntity = new ApplicationEntity(
                UUID.randomUUID().toString(), "name", "body", ApplicationState.ACCEPTED, OffsetDateTime.now());
        when(applicationRepository.findById(testEntity.getId())).thenReturn(Optional.of(testEntity));
        
        Result result = service.process(new Command.Accept("doest-exist"));
        
        assertEquals(Result.NotFound.class, result.getClass());
        verify(applicationRepository, never()).save(any(ApplicationEntity.class));
        verifyNoInteractions(applicationEventPublisher);
    }
}
