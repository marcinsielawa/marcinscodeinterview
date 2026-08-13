package com.marcinsielawa.applicationrequestmanager.core;

public sealed interface Result {
    public record BusinessRuleViolation<T> (T reason) implements Result{}
    public record ValidationError<T> (T reason)       implements Result{}
    public record Success<T> (T payload)              implements Result{}
    public record NotFound()                          implements Result{}
}
