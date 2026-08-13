package com.marcinsielawa.applicationrequestmanager.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.marcinsielawa.applicationrequestmanager.core.ApplicationState;


@Repository
public interface ApplicationRepository extends CrudRepository<ApplicationEntity, String> {
    
    @Query( "Select e FROM ApplicationEntity e " +
            "WHERE (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:state IS NULL OR e.state = :state)")
     Page<ApplicationEntity> findByNameAndStateOptional(
         @Param("name") String name, 
         @Param("state") ApplicationState state, 
         Pageable pageable
     );
}
