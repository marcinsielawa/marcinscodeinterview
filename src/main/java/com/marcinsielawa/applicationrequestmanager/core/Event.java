package com.marcinsielawa.applicationrequestmanager.core;

import java.time.OffsetDateTime;

public sealed interface Event {
   String eventId();
   String applicationRef();
   OffsetDateTime eventTimestamp(); 
   public record ApplicationCreated  (String eventId, String applicationRef, String name   , String body, OffsetDateTime eventTimestamp) implements Event {}
   public record ApplicationUpdated  (String eventId, String applicationRef, String name   , String body, ApplicationState state, OffsetDateTime eventTimestamp) implements Event {}
   public record ApplicationDeleted  (String eventId, String applicationRef, String reason , OffsetDateTime eventTimestamp) implements Event {}
   public record ApplicationRejected (String eventId, String applicationRef, String reason , OffsetDateTime eventTimestamp) implements Event {}
   
   public record ApplicationAccepted (String eventId, String applicationRef,                 OffsetDateTime eventTimestamp) implements Event {}
   public record ApplicationVerified (String eventId, String applicationRef,                 OffsetDateTime eventTimestamp) implements Event {}
   public record ApplicationPublished(String eventId, String applicationRef, Long publishingId, OffsetDateTime eventTimestamp) implements Event {}
}
