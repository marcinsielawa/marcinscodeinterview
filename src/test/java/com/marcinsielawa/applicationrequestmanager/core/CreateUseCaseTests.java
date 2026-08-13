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
class CreateUseCaseTests {
    
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
    @DisplayName("Create application use case - happy path")
    void testHappyPath() {
        Result result = service.process(new Command.Create("foo", "bar"));
        
        assertEquals(Result.Success.class, result.getClass());
        verify(applicationRepository).save(any(ApplicationEntity.class));
        verify(applicationEventPublisher).publishEvent(any(ApplicationCreated.class));

    }
    
    @Test
    @DisplayName("Create application use case - name and body are obligatory")
    void testRequiredFields() {
        Result result = service.process(new Command.Create("", ""));
        
        assertEquals(Result.ValidationError.class, result.getClass());
        verifyNoInteractions(applicationRepository);
        verifyNoInteractions(applicationEventPublisher);
    }
}
