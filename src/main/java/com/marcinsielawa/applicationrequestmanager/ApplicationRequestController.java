package com.marcinsielawa.applicationrequestmanager;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.marcinsielawa.applicationrequestmanager.core.ApplicationRequestService;
import com.marcinsielawa.applicationrequestmanager.core.Command;
import com.marcinsielawa.applicationrequestmanager.core.Result;
import com.marcinsielawa.interview.CreateApplicationRequest;
import com.marcinsielawa.inverview.DefaultApi;

import jakarta.validation.Valid;

@RestController
public class ApplicationRequestController implements DefaultApi{
    
    final ApplicationRequestService sevice;

    ApplicationRequestController(ApplicationRequestService sevice) {
        this.sevice = sevice;
    }

    @Override
    public ResponseEntity<Void> createApplication(@Valid CreateApplicationRequest req) {
        
        Result<?> result = sevice.process(new Command.CreateApplication(req.getName(), req.getBody()));
        
        switch(result) {
            case Result.Success(_) -> ResponseEntity.created(URI.create("/1")).build();
            case null -> {}
            default -> {}
        }
        
        
        return ResponseEntity.created(URI.create("/1")).build();
    }
}
