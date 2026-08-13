package com.marcinsielawa.applicationrequestmanager.core;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationCreated;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationEntity;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationRepository;
import com.marcinsielawa.applicationrequestmanager.persistence.EntityMapper;
import com.marcinsielawa.interview.ApplicationResponse;

@Service
public class ApplicationRequestServiceImpl implements ApplicationRequestService {
    
    final ApplicationRepository applicationRepository;
    
    final ApplicationEventPublisher applicationEventPublisher;

    ApplicationRequestServiceImpl(ApplicationRepository store, ApplicationEventPublisher events) {
        this.applicationRepository     = store;
        this.applicationEventPublisher = events;
    } 

    @Override
    @Transactional
    public Result<?> process(Command command) {
        
        TransitionResult<?> r = StateTransition.apply(command);
        
        switch(r) {
            case TransitionResult.Complete(ApplicationCreated payload) -> {
                
                applicationRepository.save(new ApplicationEntity(payload.id(), payload.name(), payload.body(), ApplicationState.CREATED, payload.createdAt()));
                
                applicationEventPublisher.publishEvent(payload);
                
                return new Result.Success<>(payload);
            }
            default -> throw new RuntimeException();
        }
    }

    @Override
    public Optional<ApplicationAggregate> findById(UUID id) {
        return applicationRepository.findById(id.toString())
                .filter(e -> e != null)
                .map(EntityMapper::toDomain);
    }

}
