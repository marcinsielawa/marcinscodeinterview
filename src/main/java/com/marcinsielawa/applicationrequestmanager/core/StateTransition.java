package com.marcinsielawa.applicationrequestmanager.core;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.marcinsielawa.applicationrequestmanager.core.Event.ApplicationCreated;


public class StateTransition {

    public static TransitionResult<?> apply(Command command) {
        if (command instanceof Command.CreateApplication create) {
            return new TransitionResult.Complete<>(new ApplicationCreated(
                    UUID.randomUUID().toString(),
                    create.name(),
                    create.body(),
                    OffsetDateTime.now()));
        }
        return null;
    }
    
    

}
