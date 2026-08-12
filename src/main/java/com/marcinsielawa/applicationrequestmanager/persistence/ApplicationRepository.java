package com.marcinsielawa.applicationrequestmanager.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends CrudRepository<ApplicationEntity, String> {

}
