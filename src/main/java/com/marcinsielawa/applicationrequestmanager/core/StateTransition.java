package com.marcinsielawa.applicationrequestmanager.core;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationCreated;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationDeleted;


public class StateTransition {

    public static TransitionResult apply(ApplicationAggregate existing, Command command, OffsetDateTime now) {
        if (command instanceof Command.Create create) {
            
            if(create.body() == null || create.body().isBlank()) 
                return new TransitionResult.InvalidInput<>("Body cannot be empty");
            
            if(create.name() == null || create.name().isBlank()) 
                return new TransitionResult.InvalidInput<>("Name cannot be empty");
            
            return new TransitionResult.Complete<>(new ApplicationCreated(
                    UUID.randomUUID().toString(),
                    create.name(),
                    create.body(),
                    now));
        } 
        
        if(existing == null) {
            return new TransitionResult.NotFound();
        }
        
        if (command instanceof Command.Delete delete) {
            if(existing.state() != ApplicationState.CREATED) 
                return new TransitionResult.InvalidTransition<>("only CREATED can be DELETED");
            
            return new TransitionResult.Complete<>(new ApplicationDeleted(
                    delete.id(),
                    now));
        }
        return null;
    }
    
    

}
