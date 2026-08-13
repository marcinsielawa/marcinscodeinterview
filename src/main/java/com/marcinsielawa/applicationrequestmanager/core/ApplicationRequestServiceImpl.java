package com.marcinsielawa.applicationrequestmanager.core;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcinsielawa.applicationrequestmanager.core.Command.Targetted;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationCreated;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationDeleted;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationRejected;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationEntity;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationRepository;
import com.marcinsielawa.applicationrequestmanager.persistence.EntityMapper;

@Service
class ApplicationRequestServiceImpl implements ApplicationRequestService {
    
    final ApplicationRepository applicationRepository;
    
    final ApplicationEventPublisher applicationEventPublisher;

    ApplicationRequestServiceImpl(ApplicationRepository store, ApplicationEventPublisher events) {
        this.applicationRepository     = store;
        this.applicationEventPublisher = events;
    } 

    @Override
    @Transactional
    public Result process(Command command) {
        
        final Optional<ApplicationEntity> existing;
        
        if(command instanceof Targetted t) {
            existing = applicationRepository.findById(t.id());
        } else existing = Optional.empty();
        
        final OffsetDateTime now = OffsetDateTime.now();
        
        TransitionResult r = StateTransition.apply(EntityMapper.toDomain(existing), command, now);
        
        switch(r) {
        case TransitionResult.Complete(var event) -> {
            switch (event) {
                case ApplicationCreated created -> {
                    applicationRepository.save(new ApplicationEntity(created.eventId(), created.name(), created.body(), ApplicationState.CREATED, created.createdAt()));
                    applicationEventPublisher.publishEvent(created);
                    return new Result.Success<>(created);
                }
                case ApplicationDeleted deleted -> {
                    
                    existing.get().setState(ApplicationState.DELETED);
                    existing.get().setReason(deleted.reason());
                    existing.get().setUpdatedAt(now);
                    
                    applicationRepository.save(existing.get());
                    applicationEventPublisher.publishEvent(deleted);
                    return new Result.Success<>(deleted);
                }
                case ApplicationRejected rejected -> {
                    
                    existing.get().setState(ApplicationState.REJECTED);
                    existing.get().setReason(rejected.reason());
                    existing.get().setUpdatedAt(now);
                    
                    applicationRepository.save(existing.get());
                    applicationEventPublisher.publishEvent(rejected);
                    return new Result.Success<>(rejected);
                }
                default -> throw new RuntimeException("Unknown event type");
            }
        }
        case TransitionResult.InvalidTransition(Object reason) -> {
            return new Result.BusinessRuleViolation<>(reason);
        }        
        case TransitionResult.InvalidInput(Object reason) -> {
            return new Result.ValidationError<>(reason);
        }
        case TransitionResult.NotFound() -> {
            return new Result.NotFound();
        }
        default -> throw new RuntimeException("Unknown transition result");
    }
    }

    @Override
    public Optional<ApplicationAggregate> findById(UUID id) {
        return applicationRepository.findById(id.toString())
                .filter(e -> e != null)
                .map(EntityMapper::toDomain);
    }

}
