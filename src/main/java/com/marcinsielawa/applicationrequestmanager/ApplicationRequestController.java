package com.marcinsielawa.applicationrequestmanager;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.marcinsielawa.applicationrequestmanager.core.ApplicationAggregate;
import com.marcinsielawa.applicationrequestmanager.core.ApplicationRequestService;
import com.marcinsielawa.applicationrequestmanager.core.Command;
import com.marcinsielawa.applicationrequestmanager.core.Event;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationCreated;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationPublished;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationUpdated;
import com.marcinsielawa.applicationrequestmanager.core.Result;
import com.marcinsielawa.applicationrequestmanager.persistence.ApplicationEntity;
import com.marcinsielawa.interview.ApplicationResponse;
import com.marcinsielawa.interview.ApplicationState;
import com.marcinsielawa.interview.ApplicationUpdateResponse;
import com.marcinsielawa.interview.CreateApplicationRequest;
import com.marcinsielawa.interview.DeleteApplicationRequest;
import com.marcinsielawa.interview.EditApplicationRequest;
import com.marcinsielawa.interview.PagedApplicationsResponse;
import com.marcinsielawa.interview.PublishingResponse;
import com.marcinsielawa.interview.RejectApplicationRequest;
import com.marcinsielawa.inverview.DefaultApi;

import jakarta.validation.Valid;

@RestController
public class ApplicationRequestController implements DefaultApi {
    
    final ApplicationRequestService sevice;

    ApplicationRequestController(ApplicationRequestService sevice) {
        this.sevice = sevice;
    }
    
    private <T, R> ResponseEntity<R> handleCommand(
            java.util.function.Supplier<Result> commandExecution, 
            java.util.function.Function<T, ResponseEntity<R>> successMapper
        ) {
            Result result = commandExecution.get();
            
            return switch (result) {
                case Result.Success(Event payload) -> successMapper.apply((T) payload);
                case Result.NotFound() -> ResponseEntity.notFound().build();
                case Result.ValidationError(var reason) -> ResponseEntity.badRequest()
                        .body((R) reason); 
                case Result.BusinessRuleViolation(var reason) -> ResponseEntity.status(422)
                        .body((R) reason);
                default -> throw new RuntimeException("Unable to process command");
            };
    }

    @Override
    public ResponseEntity<Void> createApplication(@Valid CreateApplicationRequest req) {
        return handleCommand(
            () -> sevice.process(new Command.Create(req.getName(), req.getBody())),
            (ApplicationCreated evt) -> ResponseEntity
                .created(URI.create("/api/applications/" + evt.eventId()))
                .build()
        );
    }

    @Override
    public ResponseEntity<Void> deleteApplication(UUID id, @Valid DeleteApplicationRequest req) {
        return handleCommand(
            () -> sevice.process(new Command.Delete(id.toString(), req.getReason())),
            _ -> ResponseEntity.noContent().build()
        );
    }

    @Override
    public ResponseEntity<Void> rejectApplication(UUID id, @Valid RejectApplicationRequest req) {
        return handleCommand(
            () -> sevice.process(new Command.Reject(id.toString(), req.getReason())),
            _  -> ResponseEntity.ok().build()
        );
    }
    

    @Override
    public ResponseEntity<Void> acceptApplication(UUID id) {
        return handleCommand(
                () -> sevice.process(new Command.Accept(id.toString())),
                _ -> ResponseEntity.ok().build()
        );
    }

    @Override
    public ResponseEntity<PublishingResponse> publishApplication(UUID id) {
        return handleCommand(
                () -> sevice.process(new Command.Publish(id.toString())),
                (ApplicationPublished evt) -> ResponseEntity.ok().body(new PublishingResponse(evt.publishingId()))
        );
    }

    @Override
    public ResponseEntity<Void> verifyApplication(UUID id) {
        return handleCommand(
                () -> sevice.process(new Command.Verify(id.toString())),
                _ -> ResponseEntity.ok().build()
        );
    }

    @Override
    public ResponseEntity<ApplicationResponse> retrieveApplication(UUID id) {
        Optional<ApplicationAggregate> data = sevice.findById(id);
        
        if(data.isPresent()) {
            ApplicationAggregate application = data.get();
            
            return ResponseEntity.ok(new ApplicationResponse(
                    UUID.fromString(application.id()),
                    ApplicationState.valueOf(application.state().toString()),
                    application.body(),
                    application.name(),
                    application.createdAt(),
                    application.updatedAt()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<PagedApplicationsResponse> browseApplications(Integer page, Integer size,
            String name, ApplicationState state) {
        
        Page<ApplicationAggregate> pageResult = sevice.listApplications(page, size, name, state == null ? null : state.toString());
        
        PagedApplicationsResponse response = new PagedApplicationsResponse();
        
        response.setApplications(pageResult.getContent().stream().map(
                e -> new ApplicationResponse(
                        UUID.fromString(e.id()),
                        ApplicationState.valueOf(e.state().toString()), 
                        e.body(),
                        e.name(),
                        e.createdAt(),
                        e.updatedAt()
                )).toList()); 
        response.setCurrentPage(pageResult.getNumber());
        response.setPageSize(pageResult.getSize());
        response.setTotalElements(pageResult.getTotalElements());
        response.setTotalPages(pageResult.getTotalPages());
        response.setHasNext(pageResult.hasNext());
        
        return ResponseEntity.ok(response);
    }

    
    @Override
    public ResponseEntity<ApplicationUpdateResponse> editApplication(UUID id,
            @Valid EditApplicationRequest req) {
        
        return handleCommand(
            () -> sevice.process(new Command.Update(id.toString(), req.getName(), req.getBody())),
            (ApplicationUpdated evt) -> ResponseEntity.ok(new ApplicationUpdateResponse(
                    UUID.fromString(evt.applicationRef()),
                    ApplicationState.valueOf(evt.state().toString()),
                    evt.name(),
                    evt.body(),
                    evt.eventTimestamp()
            ))
        );
    }
    
  
}
