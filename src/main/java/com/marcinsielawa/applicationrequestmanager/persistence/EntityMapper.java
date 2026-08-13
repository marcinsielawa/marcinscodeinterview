package com.marcinsielawa.applicationrequestmanager.persistence;

import com.marcinsielawa.applicationrequestmanager.core.ApplicationAggregate;

public class EntityMapper {

    public static ApplicationEntity toEntity(ApplicationAggregate domain) {
        if (domain == null) return null;
        return new ApplicationEntity(
            domain.id(),
            domain.name(),
            domain.body(),
            domain.state(),
            domain.createdAt()
        );
    }
    
    
    public static ApplicationAggregate toDomain(ApplicationEntity entity) {
        if (entity == null) return null;
        return new ApplicationAggregate(
            entity.getId(),
            entity.getName(),
            entity.getBody(),
            entity.getState(),
            entity.getCreatedAt()
        );
    }
    
}
