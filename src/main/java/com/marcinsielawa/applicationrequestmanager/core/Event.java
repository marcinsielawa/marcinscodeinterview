package com.marcinsielawa.applicationrequestmanager.core;

import java.time.OffsetDateTime;

public sealed interface Event {
   public record ApplicationCreated(String id, String name, String body, OffsetDateTime createdAt) implements Event {}
}
