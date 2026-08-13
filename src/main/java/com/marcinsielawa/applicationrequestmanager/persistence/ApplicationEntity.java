package com.marcinsielawa.applicationrequestmanager.persistence;

import java.time.OffsetDateTime;

import com.marcinsielawa.applicationrequestmanager.core.ApplicationState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "applications")
@lombok.Data
public class ApplicationEntity {
    
    ApplicationEntity() {}

    public ApplicationEntity(String id, String name, String body, ApplicationState state, OffsetDateTime createdAt) {
        super();
        this.id = id;
        this.name = name;
        this.body = body;
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public ApplicationEntity(String id, String name, String body, ApplicationState state, OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
        this(id, name, body, state, createdAt);
        this.updatedAt = createdAt;
    }

    @Id
    @Column(nullable = false)
    String id;
    
    @Column(nullable = false)
    String name;
    
    @Column(nullable = false)
    String body;
    
    @Column(nullable = true)
    String reason;
    
    @Enumerated(EnumType.STRING)
    ApplicationState state;
    
    @Column(nullable = false)
    OffsetDateTime createdAt;
    
    @Column(nullable = false)
    OffsetDateTime updatedAt;
}
