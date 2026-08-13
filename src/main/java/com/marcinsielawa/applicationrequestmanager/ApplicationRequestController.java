package com.marcinsielawa.applicationrequestmanager;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.marcinsielawa.applicationrequestmanager.core.ApplicationAggregate;
import com.marcinsielawa.applicationrequestmanager.core.ApplicationRequestService;
import com.marcinsielawa.applicationrequestmanager.core.Command;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationCreated;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationDeleted;
import com.marcinsielawa.applicationrequestmanager.core.Result;
import com.marcinsielawa.interview.ApplicationResponse;
import com.marcinsielawa.interview.ApplicationState;
import com.marcinsielawa.interview.CreateApplicationRequest;
import com.marcinsielawa.interview.DeleteApplicationRequest;
import com.marcinsielawa.inverview.DefaultApi;

import jakarta.validation.Valid;

@RestController
public class ApplicationRequestController implements DefaultApi {
    
    final ApplicationRequestService sevice;

    ApplicationRequestController(ApplicationRequestService sevice) {
        this.sevice = sevice;
    }

    @Override
    public ResponseEntity<Void> createApplication(@Valid CreateApplicationRequest req) {
        
        Result result = sevice.process(new Command.Create(req.getName(), req.getBody()));
        
        return switch(result) {
            case Result.Success(ApplicationCreated evt) -> ResponseEntity.created(URI.create("/api/applications/" + evt.eventId())).build();
            default -> throw new RuntimeException();
        };
    }
    
    @Override
    public ResponseEntity<Void> deleteApplication(UUID id, @Valid DeleteApplicationRequest req) {
        Result result = sevice.process(new Command.Delete(id.toString(), req.getReason()));
        
        return switch(result) {
            case Result.Success(ApplicationDeleted evt) -> ResponseEntity.noContent().build();
            default -> throw new RuntimeException();
        };
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
