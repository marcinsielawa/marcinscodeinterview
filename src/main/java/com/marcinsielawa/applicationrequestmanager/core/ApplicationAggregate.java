package com.marcinsielawa.applicationrequestmanager.core;

import java.time.OffsetDateTime;

record ApplicationAggregate(String id, String name, String body, State state, OffsetDateTime createdAt) {

}
