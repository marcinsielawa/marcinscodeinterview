package com.marcinsielawa.applicationrequestmanager.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationDeleted;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationRejected;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationEntity;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationRepository;
import com.marcinsielawa.applicationrequestmanager.persistence.PublishingIdGenerator;

@SpringBootTest(classes = ApplicationRequestServiceImpl.class)
@ExtendWith(MockitoExtension.class)
class RejectUseCaseTests {
    
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
    @DisplayName("Reject application use case - happy path - VERIFIED")
    void testRejectUseCaseHappyPathVerified() {
        
        ApplicationEntity testEntity = new ApplicationEntity(
                UUID.randomUUID().toString(), "name", "body", ApplicationState.VERIFIED, OffsetDateTime.now());
        
        when(applicationRepository.findById(testEntity.getId())).thenReturn(Optional.of(testEntity));
        
        ArgumentCaptor<ApplicationEntity> entityCaptor = ArgumentCaptor.forClass(testEntity.getClass());
        
        Result result = service.process(new Command.Reject(testEntity.getId(), "insufficient"));

        verify(applicationRepository).save(entityCaptor.capture());

        ApplicationEntity capturedEntity = entityCaptor.getValue();

        assertEquals(Result.Success.class, result.getClass());
        
        assertEquals(ApplicationState.REJECTED, capturedEntity.getState());
        assertEquals("insufficient"           , capturedEntity.getReason());
        verify(applicationEventPublisher).publishEvent(any(ApplicationRejected.class));
    }
    
    @Test
    @DisplayName("Reject application use case - happy path - ACCEPTED")
    void testRejectUseCaseHappyPathAccepted() {
        
        ApplicationEntity testEntity = new ApplicationEntity(
                UUID.randomUUID().toString(), "name", "body", ApplicationState.ACCEPTED, OffsetDateTime.now());
        
        when(applicationRepository.findById(testEntity.getId())).thenReturn(Optional.of(testEntity));
        
        ArgumentCaptor<ApplicationEntity> entityCaptor = ArgumentCaptor.forClass(testEntity.getClass());
        
        Result result = service.process(new Command.Reject(testEntity.getId(), "insufficient"));

        verify(applicationRepository).save(entityCaptor.capture());

        ApplicationEntity capturedEntity = entityCaptor.getValue();

        assertEquals(Result.Success.class, result.getClass());
        
        assertEquals(ApplicationState.REJECTED, capturedEntity.getState());
        assertEquals("insufficient"           , capturedEntity.getReason());
        verify(applicationEventPublisher).publishEvent(any(ApplicationRejected.class));
    }
    
    @Test
    @DisplayName("Reject application use case - wrong status (expect VERIFIED or ACCEPTED)")
    void testDeleteOnlyCreated() {
        
        ApplicationEntity testEntity = new ApplicationEntity(
                UUID.randomUUID().toString(), "name", "body", ApplicationState.CREATED, OffsetDateTime.now());
        
        when(applicationRepository.findById(testEntity.getId())).thenReturn(Optional.of(testEntity));
        
        Result result = service.process(new Command.Reject(testEntity.getId(), "insufficient"));
        
        assertEquals(Result.BusinessRuleViolation.class, result.getClass());
        verify(applicationRepository, never()).save(any(ApplicationEntity.class));
        verifyNoInteractions(applicationEventPublisher);
    }
    
    @Test
    @DisplayName("Reject application use case - not found")
    void testDeleteNotFound() {
        ApplicationEntity testEntity = new ApplicationEntity(
                UUID.randomUUID().toString(), "name", "body", ApplicationState.VERIFIED, OffsetDateTime.now());
        when(applicationRepository.findById(testEntity.getId())).thenReturn(Optional.of(testEntity));
        
        Result result = service.process(new Command.Reject("doest-exist", "insufficient"));
        
        assertEquals(Result.NotFound.class, result.getClass());
        verify(applicationRepository, never()).save(any(ApplicationEntity.class));
        verifyNoInteractions(applicationEventPublisher);
    }
    
    @Test
    @DisplayName("Reject application use case - reason is required")
    void testDeleteNoReason() {
        ApplicationEntity testEntity = new ApplicationEntity(
                UUID.randomUUID().toString(), "name", "body", ApplicationState.VERIFIED, OffsetDateTime.now());
        when(applicationRepository.findById(testEntity.getId())).thenReturn(Optional.of(testEntity));
        
        Result result = service.process(new Command.Reject(testEntity.getId(), " "));
        
        assertEquals(Result.BusinessRuleViolation.class, result.getClass());
        verify(applicationRepository, never()).save(any(ApplicationEntity.class));
        verifyNoInteractions(applicationEventPublisher);
    }
}
