package com.marcinsielawa.applicationrequestmanager.core;

public sealed interface Result<T> {
    public record Success<T>(T payload) implements Result<T>{}
}
