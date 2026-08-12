package com.marcinsielawa.applicationrequestmanager;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.marcinsielawa.interview.CreateApplicationRequest;
import com.marcinsielawa.inverview.DefaultApi;

import jakarta.validation.Valid;

@RestController
public class ApplicationRequestController implements DefaultApi{

    @Override
    public ResponseEntity<Void> createApplication(@Valid CreateApplicationRequest createApplicationRequest) {
        return ResponseEntity.created(URI.create("/1")).build();
    }

 

}
