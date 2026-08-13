package com.marcinsielawa.applicationrequestmanager.core;

import java.time.OffsetDateTime;

public sealed interface Event {
   String eventId();
   String applicationRef();
   OffsetDateTime createdAt(); 
   public record ApplicationCreated(String eventId, String applicationRef, String name  , String body, OffsetDateTime createdAt) implements Event {}
   public record ApplicationDeleted(String eventId, String applicationRef, String reason, OffsetDateTime createdAt) implements Event {}
}
