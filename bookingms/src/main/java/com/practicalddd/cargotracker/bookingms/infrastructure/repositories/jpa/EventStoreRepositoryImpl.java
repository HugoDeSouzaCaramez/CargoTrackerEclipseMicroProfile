package com.practicalddd.cargotracker.bookingms.infrastructure.repositories.jpa;

import com.practicalddd.cargotracker.bookingms.application.events.DomainEvent;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.EventStoreRepository;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.DomainEventEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class EventStoreRepositoryImpl implements EventStoreRepository {
    
    private static final Logger logger = Logger.getLogger(EventStoreRepositoryImpl.class.getName());
    
    @PersistenceContext(unitName = "bookingms")
    private EntityManager entityManager;
    
    @Override
    @Transactional
    public void store(DomainEvent event) {
        try {
            logger.fine(() -> "Storing domain event: " + event.eventType());
            
            // Converter DomainEvent para DomainEventEntity
            DomainEventEntity entity = new DomainEventEntity(
                event.eventType(),
                getAggregateType(event),
                extractAggregateId(event),
                serializeEventData(event),
                "{}", // Metadata adicional
                1,    // Versão inicial
                event.occurredOn()
            );
            
            entityManager.persist(entity);
            logger.fine(() -> "Domain event stored successfully: " + event.eventType());
            
        } catch (Exception e) {
            logger.severe("Error storing domain event: " + e.getMessage());
            throw new RuntimeException("Failed to store domain event", e);
        }
    }
    
    @Override
    public List<DomainEvent> findByAggregateId(String aggregateId) {
        try {
            logger.fine(() -> "Finding events for aggregate: " + aggregateId);
            
            List<DomainEventEntity> entities = entityManager
                .createNamedQuery("DomainEventEntity.findByAggregate", DomainEventEntity.class)
                .setParameter("aggregateType", "Cargo") // Ajustar conforme o agregado
                .setParameter("aggregateId", aggregateId)
                .getResultList();
            
            logger.fine(() -> "Found " + entities.size() + " events for aggregate: " + aggregateId);
            
            return entities.stream()
                .map(this::toDomainEvent)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.severe("Error finding events by aggregate: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<DomainEvent> findByEventType(String eventType) {
        try {
            logger.fine(() -> "Finding events by type: " + eventType);
            
            List<DomainEventEntity> entities = entityManager
                .createNamedQuery("DomainEventEntity.findByType", DomainEventEntity.class)
                .setParameter("eventType", eventType)
                .getResultList();
            
            logger.fine(() -> "Found " + entities.size() + " events of type: " + eventType);
            
            return entities.stream()
                .map(this::toDomainEvent)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.severe("Error finding events by type: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<DomainEvent> findAllAfter(LocalDateTime timestamp) {
        try {
            logger.fine(() -> "Finding events after: " + timestamp);
            
            List<DomainEventEntity> entities = entityManager
                .createQuery("SELECT e FROM DomainEventEntity e WHERE e.occurredOn > :timestamp ORDER BY e.occurredOn", DomainEventEntity.class)
                .setParameter("timestamp", timestamp)
                .getResultList();
            
            logger.fine(() -> "Found " + entities.size() + " events after: " + timestamp);
            
            return entities.stream()
                .map(this::toDomainEvent)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.severe("Error finding events after timestamp: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private String getAggregateType(DomainEvent event) {
        // Determinar o tipo do agregado baseado no evento
        if (event.eventType().contains("Cargo")) {
            return "Cargo";
        } else if (event.eventType().contains("Port")) {
            return "Port";
        }
        return "Unknown";
    }
    
    private String extractAggregateId(DomainEvent event) {
        try {
            // Extrair ID do agregado baseado no tipo de evento
            // Esta lógica precisa ser implementada para cada tipo de evento
            // Por simplicidade, vamos retornar um identificador genérico
            return "aggregate-" + event.eventType();
        } catch (Exception e) {
            logger.warning("Error extracting aggregate ID: " + e.getMessage());
            return "unknown";
        }
    }
    
    private String serializeEventData(DomainEvent event) {
        try {
            // Serialização simples do evento
            return String.format("{\"eventType\":\"%s\",\"occurredOn\":\"%s\"}",
                event.eventType(), event.occurredOn());
        } catch (Exception e) {
            logger.severe("Error serializing event data: " + e.getMessage());
            return "{\"error\":\"Failed to serialize event\"}";
        }
    }
    
    private DomainEvent toDomainEvent(DomainEventEntity entity) {
        // Implementação simplificada - retorna um DomainEvent básico
        return new DomainEvent() {
            @Override
            public LocalDateTime occurredOn() {
                return entity.getOccurredOn();
            }
            
            @Override
            public String eventType() {
                return entity.getEventType();
            }
        };
    }
}
