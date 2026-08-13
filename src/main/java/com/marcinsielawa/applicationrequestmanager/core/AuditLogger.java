package com.marcinsielawa.applicationrequestmanager.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marcinsielawa.applicationrequestmanager.audit.AuditEntity;
import com.marcinsielawa.applicationrequestmanager.audit.AuditRepository;

@Component
public class AuditLogger {

    @Autowired
    AuditRepository auditRepository;

    final ObjectMapper objectMapper;
    
    private final static String REMOVE_ID_AND_TIMESTAMP = "(?i)\"(id|createdAt)\"\\s*:\\s*(\"(?:\\\\.|[^\"\\\\])*\"|[^,}]+)\\s*,?|,\\s*(?i)\"(id|createdAt)\"\\s*:\\s*(\"(?:\\\\.|[^\"\\\\])*\"|[^,}]+)";
    
    public AuditLogger() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void onEvent(Event evt) {

        AuditEntity saved = new AuditEntity();

        saved.setId(evt.id());
        saved.setCreatedAt(evt.createdAt());
        saved.setEventType(evt.getClass().getSimpleName());

        try {
            saved.setEventPayload(objectMapper.writeValueAsString(evt).replaceAll(REMOVE_ID_AND_TIMESTAMP, ""));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        auditRepository.save(saved);
    }

}
