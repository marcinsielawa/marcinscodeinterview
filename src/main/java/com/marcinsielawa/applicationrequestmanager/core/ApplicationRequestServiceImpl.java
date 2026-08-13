package com.marcinsielawa.applicationrequestmanager.core;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcinsielawa.applicationrequestmanager.core.Command.Targetted;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationAccepted;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationCreated;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationDeleted;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationPublished;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationRejected;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationUpdated;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationVerified;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationEntity;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationRepository;
import com.marcinsielawa.applicationrequestmanager.persistence.EntityMapper;
import com.marcinsielawa.applicationrequestmanager.persistence.PublishingIdGenerator;

@Service
class ApplicationRequestServiceImpl implements ApplicationRequestService {

    final ApplicationRepository applicationRepository;
    
    final ApplicationEventPublisher applicationEventPublisher;
    
    final PublishingIdGenerator idgen;

    ApplicationRequestServiceImpl(ApplicationRepository store, ApplicationEventPublisher events, PublishingIdGenerator idgen) {
        this.applicationRepository     = store;
        this.applicationEventPublisher = events;
        this.idgen = idgen;
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
                    applicationRepository.save(new ApplicationEntity(created.eventId(), created.name(), created.body(), ApplicationState.CREATED, created.eventTimestamp()));
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
                case ApplicationUpdated updated -> {
                    
                    existing.get().setName(updated.name());
                    existing.get().setBody(updated.body());
                    existing.get().setUpdatedAt(now);
                    
                    applicationRepository.save(existing.get());
                    applicationEventPublisher.publishEvent(updated);
                    return new Result.Success<>(updated);
                }
                case ApplicationRejected rejected -> {
                    
                    existing.get().setState(ApplicationState.REJECTED);
                    existing.get().setReason(rejected.reason());
                    existing.get().setUpdatedAt(now);
                    
                    applicationRepository.save(existing.get());
                    applicationEventPublisher.publishEvent(rejected);
                    return new Result.Success<>(rejected);
                }
                case ApplicationVerified verified -> {
                    
                    existing.get().setState(ApplicationState.VERIFIED);
                    existing.get().setUpdatedAt(now);
                    
                    applicationRepository.save(existing.get());
                    applicationEventPublisher.publishEvent(verified);
                    return new Result.Success<>(verified);
                }
                case ApplicationAccepted accepted -> {
                    
                    existing.get().setState(ApplicationState.ACCEPTED);
                    existing.get().setUpdatedAt(now);
                    
                    applicationRepository.save(existing.get());
                    applicationEventPublisher.publishEvent(accepted);
                    return new Result.Success<>(accepted);
                }
                case ApplicationPublished published -> {
                    
                    long id = idgen.generateNextPublishingId();
                    
                    existing.get().setState(ApplicationState.PUBLISHED);
                    existing.get().setPublishingId(id);
                    existing.get().setUpdatedAt(now);
                    
                    ApplicationPublished publishedWithId = new ApplicationPublished(published.eventId(), published.applicationRef(), id, published.eventTimestamp());
                    
                    applicationRepository.save(existing.get());
                    applicationEventPublisher.publishEvent(publishedWithId);
                    return new Result.Success<>(publishedWithId);
                }
                default -> throw new RuntimeException("Unknown event type" + event.getClass().getSimpleName());
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

    @Override
    public Page<ApplicationAggregate> listApplications(Integer page, Integer pageSize, String name,
            String stateInput) {

        int pageNumber = (page != null && page >= 0) ? page : 0;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;
        
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("createdAt").descending());
        
        Page<ApplicationAggregate> pageResult = applicationRepository.findByNameAndStateOptional(name, stateInput == null ? null : ApplicationState.valueOf(stateInput), pageable)
                .map(EntityMapper::toDomain);
        
        return pageResult;
    }

}
