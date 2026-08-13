package com.marcinsielawa.applicationrequestmanager.core;

public sealed interface TransitionResult {
    public record Complete<T>(T payload)         implements TransitionResult{}
    public record InvalidInput<T>(T reason)      implements TransitionResult{}
    public record InvalidTransition<T>(T reason) implements TransitionResult{}
    public record NotFound()                     implements TransitionResult{}
    
}
