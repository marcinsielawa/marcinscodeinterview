package com.marcinsielawa.applicationrequestmanager.audit;

import java.time.OffsetDateTime;

import com.marcinsielawa.applicationrequestmanager.core.ApplicationState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_log")
@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class AuditEntity {

    @Id
    @Column(nullable = false)
    String id;
    
    @Column(nullable = false)
    String eventType;
    
    @Column(nullable = false)
    String eventPayload;
    
    @Column(nullable = false)
    OffsetDateTime createdAt;
}
