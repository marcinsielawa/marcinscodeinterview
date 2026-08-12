package com.marcinsielawa.applicationrequestmanager.core;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationCreated;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationEntity;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationRepository;

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
                
                return new Result.Success<>(payload);
            }
            default -> throw new RuntimeException();
        }
    }

}
