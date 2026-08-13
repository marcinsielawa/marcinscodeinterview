package com.marcinsielawa.applicationrequestmanager.core;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationAccepted;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationCreated;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationDeleted;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationPublished;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationRejected;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationUpdated;
import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationVerified;


public class StateTransition {

    public static TransitionResult apply(ApplicationAggregate existing, Command command, OffsetDateTime now) {
        if (command instanceof Command.Create create) {
            
            if(create.body() == null || create.body().isBlank()) 
                return new TransitionResult.InvalidInput<>("Body cannot be empty");
            
            if(create.name() == null || create.name().isBlank()) 
                return new TransitionResult.InvalidInput<>("Name cannot be empty");
            
            final String applicationId = UUID.randomUUID().toString();
            final String eventId       = UUID.randomUUID().toString();
            
            return new TransitionResult.Complete<>(new ApplicationCreated(
                    applicationId,
                    eventId,
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
            
            if(delete.reason() == null || delete.reason().isBlank()) 
                return new TransitionResult.InvalidTransition<>("deletion reason is required");
            
            final String eventId = UUID.randomUUID().toString();
            
            return new TransitionResult.Complete<>(new ApplicationDeleted(
                    eventId,
                    existing.id(),
                    delete.reason(),
                    now));
        } else if (command instanceof Command.Update update) {
            if(existing.state() != ApplicationState.VERIFIED &&
               existing.state() != ApplicationState.CREATED ) 
                return new TransitionResult.InvalidTransition<>("only update CREATED and VERIFIED");
            
            final String eventId = UUID.randomUUID().toString();
            
            return new TransitionResult.Complete<>(new ApplicationUpdated(
                    eventId,
                    existing.id(),
                    update.name(),
                    update.body(),
                    existing.state(),
                    now));
        } else if (command instanceof Command.Reject reject) {
            if(existing.state() != ApplicationState.VERIFIED &&
               existing.state() != ApplicationState.ACCEPTED ) 
                return new TransitionResult.InvalidTransition<>("only VERIFIED or ACCEPTED can be REJECTED");
            
            if(reject.reason() == null || reject.reason().isBlank()) 
                return new TransitionResult.InvalidTransition<>("deletion reason is required");
            
            final String eventId = UUID.randomUUID().toString();
            
            return new TransitionResult.Complete<>(new ApplicationRejected(
                    eventId,
                    existing.id(),
                    reject.reason(),
                    now));
        } else if (command instanceof Command.Verify) {
            if(existing.state() != ApplicationState.CREATED) 
                return new TransitionResult.InvalidTransition<>("only CREATED");
            
            final String eventId = UUID.randomUUID().toString();
            
            return new TransitionResult.Complete<>(new ApplicationVerified(
                    eventId,
                    existing.id(),
                    now));
        } else if (command instanceof Command.Accept) {
            if(existing.state() != ApplicationState.VERIFIED) 
                return new TransitionResult.InvalidTransition<>("only VERIFIED");
            
            final String eventId = UUID.randomUUID().toString();
            
            return new TransitionResult.Complete<>(new ApplicationAccepted(
                    eventId,
                    existing.id(),
                    now));
        } else if (command instanceof Command.Publish) {
            if(existing.state() != ApplicationState.ACCEPTED) 
                return new TransitionResult.InvalidTransition<>("only ACCEPTED");
            
            final String eventId = UUID.randomUUID().toString();
            
            return new TransitionResult.Complete<>(new ApplicationPublished(
                    eventId,
                    existing.id(),
                    null,
                    now));
        }
        return null;
    }
    
    

}
