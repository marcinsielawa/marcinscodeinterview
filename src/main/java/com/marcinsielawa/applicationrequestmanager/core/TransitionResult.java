package com.marcinsielawa.applicationrequestmanager.core;

public sealed interface TransitionResult<T> {
    public record Complete<T>(T payload) implements TransitionResult<T>{}
}
