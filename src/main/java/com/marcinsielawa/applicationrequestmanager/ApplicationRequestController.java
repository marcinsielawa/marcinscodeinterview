package com.marcinsielawa.applicationrequestmanager;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.marcinsielawa.applicationrequestmanager.core.ApplicationAggregate;
import com.marcinsielawa.applicationrequestmanager.core.ApplicationRequestService;
import com.marcinsielawa.applicationrequestmanager.core.Command;
import com.marcinsielawa.applicationrequestmanager.core.Event;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationAccepted;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationCreated;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationPublished;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationVerified;
import com.marcinsielawa.applicationrequestmanager.core.Result;
import com.marcinsielawa.interview.ApplicationResponse;
import com.marcinsielawa.interview.ApplicationState;
import com.marcinsielawa.interview.CreateApplicationRequest;
import com.marcinsielawa.interview.DeleteApplicationRequest;
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
                    application.createdAt()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
