package com.marcinsielawa.applicationrequestmanager.core;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;


public interface ApplicationRequestService {
    Result process(Command command);

    Optional<ApplicationAggregate> findById(UUID id);

    Page<ApplicationAggregate> listApplications(Integer page, Integer pageSize, String name,
            String state);
}
