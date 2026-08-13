package com.marcinsielawa.applicationrequestmanager.core;

import java.time.OffsetDateTime;

public record ApplicationAggregate(String id, String name, String body, ApplicationState state, OffsetDateTime createdAt) {

}
