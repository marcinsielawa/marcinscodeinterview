package com.marcinsielawa.applicationrequestmanager.core;

import java.time.OffsetDateTime;

public sealed interface Event {
   String id();
   OffsetDateTime createdAt(); 
   public record ApplicationCreated(String id, String name, String body, OffsetDateTime createdAt) implements Event {}
}
