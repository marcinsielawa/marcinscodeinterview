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
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class ApplicationEntity {

    @Id
    @Column(nullable = false)
    String id;
    
    @Column(nullable = false)
    String name;
    
    @Column(nullable = false)
    String body;
    
    @Enumerated(EnumType.STRING)
    ApplicationState state;
    
    @Column(nullable = false)
    OffsetDateTime createdAt;
}
