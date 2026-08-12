package com.marcinsielawa.applicationrequestmanager.core;

public interface ApplicationRequestService {
    Result<?> process(Command command);
}
