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

import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationCreated;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationDeleted;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationEntity;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationRepository;

@SpringBootTest(classes = ApplicationRequestServiceImpl.class)
@ExtendWith(MockitoExtension.class)
class DeleteUseCaseTests {
    
    ApplicationRequestService service;
    
    @MockitoBean
    ApplicationRepository applicationRepository;
    
    @MockitoBean
    ApplicationEventPublisher applicationEventPublisher;
    
    ApplicationEntity testEntity = new ApplicationEntity(
            UUID.randomUUID().toString(), "name", "body", ApplicationState.CREATED, OffsetDateTime.now());
    
    @BeforeEach
    void before() {
        service = new ApplicationRequestServiceImpl(applicationRepository, applicationEventPublisher);
        
        Optional<ApplicationEntity> foo = Optional.of(testEntity);
        when(applicationRepository.findById(testEntity.getId())).thenReturn(foo);
    }

    @Test
    @DisplayName("Delete application use case - happy path")
    void testDeleteUseCaseHappyPath() {
        
        ArgumentCaptor<ApplicationEntity> entityCaptor = ArgumentCaptor.forClass(testEntity.getClass());
        
        Result result = service.process(new Command.Delete(testEntity.getId(), "not good"));

        verify(applicationRepository).save(entityCaptor.capture());

        ApplicationEntity capturedEntity = entityCaptor.getValue();

        assertEquals(Result.Success.class, result.getClass());
        
        assertEquals(ApplicationState.DELETED, capturedEntity.getState());
        assertEquals("not good"              , capturedEntity.getReason());
        verify(applicationEventPublisher).publishEvent(any(ApplicationDeleted.class));
    }
    
    @Test
    @DisplayName("Delete application use case - wrong status")
    void testDeleteOnlyCreated() {
        
        ApplicationEntity testEntity = new ApplicationEntity(
                UUID.randomUUID().toString(), "name", "body", ApplicationState.DELETED, OffsetDateTime.now());
        
        when(applicationRepository.findById(testEntity.getId())).thenReturn(Optional.of(testEntity));
        
        Result result = service.process(new Command.Delete(testEntity.getId(), "not good"));
        
        assertEquals(Result.BusinessRuleViolation.class, result.getClass());
        verify(applicationRepository, never()).save(any(ApplicationEntity.class));
        verifyNoInteractions(applicationEventPublisher);
    }
    
    @Test
    @DisplayName("Delete application use case - not found")
    void testDeleteNotFound() {
        Result result = service.process(new Command.Delete("doest-exist", "not good"));
        
        assertEquals(Result.NotFound.class, result.getClass());
        verify(applicationRepository, never()).save(any(ApplicationEntity.class));
        verifyNoInteractions(applicationEventPublisher);
    }
    
    @Test
    @DisplayName("Delete application use case - reason is required")
    void testDeleteNoReason() {
        Result result = service.process(new Command.Delete(testEntity.getId(), " "));
        
        assertEquals(Result.BusinessRuleViolation.class, result.getClass());
        verify(applicationRepository, never()).save(any(ApplicationEntity.class));
        verifyNoInteractions(applicationEventPublisher);
    }
}
