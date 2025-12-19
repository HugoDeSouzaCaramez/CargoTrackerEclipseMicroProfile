package com.practicalddd.cargotracker.bookingms.infrastructure.persistence.mappers;

import com.practicalddd.cargotracker.bookingms.domain.portaggregate.Port;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortId;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortLocation;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortCapacity;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortStatus;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.PortEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@ApplicationScoped
public class PortMapper {
    
    private static final Logger logger = Logger.getLogger(PortMapper.class.getName());
    
    @PersistenceContext(unitName = "bookingms")
    private EntityManager entityManager;
    
    public PortEntity toEntity(Port port) {
        if (port == null) return null;
        
        try {
            logger.fine(() -> "Mapping Port to Entity: " + port.getUnLocCode());
            
            // Primeiro, tenta encontrar a entidade existente
            PortEntity existingEntity = null;
            try {
                existingEntity = entityManager.find(PortEntity.class, port.getUnLocCode());
            } catch (Exception e) {
                logger.fine("Port entity not found, creating new: " + port.getUnLocCode());
            }
            
            PortEntity entity;
            if (existingEntity != null) {
                // Atualiza a entidade existente
                entity = existingEntity;
                entity.setName(port.getLocation().getName());
                entity.setCountry(port.getLocation().getCountry());
                entity.setTimeZone(port.getLocation().getTimeZone());
                entity.setCurrentUsage(port.getCapacity().getCurrentUsage());
                entity.setMaxCapacity(port.getCapacity().getMaxCapacity());
                
                // Atualiza status baseado no agregado
                String status = port.getStatus() != null ? port.getStatus().name() : "OPERATIONAL";
                entity.setStatus(status);
                
                // Mantém o createdAt original
                logger.fine(() -> "Updated existing port entity: " + port.getUnLocCode());
            } else {
                // Cria nova entidade
                entity = new PortEntity(
                    port.getUnLocCode(),
                    port.getLocation().getName(),
                    port.getLocation().getCountry(),
                    port.getLocation().getTimeZone(),
                    port.getCapacity().getCurrentUsage(),
                    port.getCapacity().getMaxCapacity(),
                    port.getStatus() != null ? port.getStatus().name() : "OPERATIONAL"
                );
                logger.fine(() -> "Created new port entity: " + port.getUnLocCode());
            }
            
            return entity;
            
        } catch (Exception e) {
            logger.severe("Error mapping Port to Entity: " + e.getMessage());
            throw new RuntimeException("Mapping error: " + e.getMessage(), e);
        }
    }
    
    public Port toDomain(PortEntity entity) {
        if (entity == null) return null;
        
        try {
            logger.fine(() -> "Mapping Entity to Port: " + entity.getUnLocCode());
            
            PortId portId = new PortId(entity.getUnLocCode());
            PortLocation location = new PortLocation(
                entity.getUnLocCode(),
                entity.getName(),
                entity.getCountry(),
                entity.getTimeZone()
            );
            PortCapacity capacity = new PortCapacity(
                entity.getCurrentUsage(),
                entity.getMaxCapacity()
            );
            
            Port port = new Port(portId, location, capacity);
            
            // Definir status se disponível na entidade
            try {
                if (entity.getStatus() != null) {
                    // Note: Esta é uma simplificação. Em produção, você precisaria
                    // de métodos no agregado Port para definir o status baseado no estado
                    // Por enquanto, apenas registramos o status
                    logger.fine(() -> "Port " + entity.getUnLocCode() + " has status: " + entity.getStatus());
                }
            } catch (Exception e) {
                logger.warning("Error setting port status: " + e.getMessage());
            }
            
            logger.fine(() -> "Successfully mapped Entity to Port: " + entity.getUnLocCode());
            return port;
            
        } catch (Exception e) {
            logger.severe("Error mapping Entity to Port: " + e.getMessage());
            throw new RuntimeException("Mapping error: " + e.getMessage(), e);
        }
    }
}
