package com.marcinsielawa.applicationrequestmanager.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.marcinsielawa.applicationrequestmanager.core.ApplicationState;

@DataJpaTest
class ApplicationRepositoryTest {
    
    @Autowired
    ApplicationRepository applicationRepository;
    
    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("Applications are persisted to main store")
    void testPersitingDuringCreation() {
        
        ApplicationEntity entity = new ApplicationEntity(
                UUID.randomUUID().toString(),
                "foo",
                "bar",
                ApplicationState.CREATED,
                OffsetDateTime.now()
                );
        
        entity = applicationRepository.save(entity);
        
        entityManager.flush();
        entityManager.clear();
        
        Optional<ApplicationEntity> retrieved = applicationRepository.findById(entity.getId());
        
        assertTrue(retrieved.isPresent());
        assertEquals(entity.getBody(), retrieved.get().body);
        assertEquals(entity.getName(), retrieved.get().name);
    }

}
