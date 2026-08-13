package com.marcinsielawa.applicationrequestmanager.audit;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRepository extends CrudRepository<AuditEntity, String> {

}
