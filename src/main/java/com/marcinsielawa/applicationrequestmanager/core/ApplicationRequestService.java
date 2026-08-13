package com.marcinsielawa.applicationrequestmanager.core;

import java.util.*;
import com.marcinsielawa.interview.ApplicationResponse;

public interface ApplicationRequestService {
    Result<?> process(Command command);

    Optional<ApplicationAggregate> findById(UUID id);
}
