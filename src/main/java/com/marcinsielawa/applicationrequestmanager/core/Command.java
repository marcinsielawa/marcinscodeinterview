package com.marcinsielawa.applicationrequestmanager.core;

public sealed interface Command {
   public record CreateApplication(String name, String body) implements Command {}
}
