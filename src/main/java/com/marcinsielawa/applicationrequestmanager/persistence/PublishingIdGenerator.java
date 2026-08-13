package com.marcinsielawa.applicationrequestmanager.persistence;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class PublishingIdGenerator {
    @PersistenceContext private EntityManager em;

    @Transactional(propagation = Propagation.MANDATORY)
    public Long generateNextPublishingId() {
        return ((Number) em.createNativeQuery("VALUES NEXT VALUE FOR request_publishing_id_seq").getSingleResult()).longValue();
    }
}
