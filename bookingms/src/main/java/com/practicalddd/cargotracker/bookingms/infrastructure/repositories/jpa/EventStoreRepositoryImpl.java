package com.practicalddd.cargotracker.bookingms.infrastructure.repositories.jpa;

import com.practicalddd.cargotracker.bookingms.domain.model.events.DomainEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.EventStoreRepository;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.DomainEventEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
                "Cargo", // Tipo do agregado
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
                .setParameter("aggregateType", "Cargo")
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
            
            // Implementação com JPQL para Java 8
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
    
    private String extractAggregateId(DomainEvent event) {
        try {
            // Extrair ID do agregado baseado no tipo de evento
            if (event instanceof com.practicalddd.cargotracker.bookingms.domain.model.events.CargoBookedEvent) {
                return ((com.practicalddd.cargotracker.bookingms.domain.model.events.CargoBookedEvent) event).getBookingId();
            } else if (event instanceof com.practicalddd.cargotracker.bookingms.domain.model.events.CargoRoutedEvent) {
                return ((com.practicalddd.cargotracker.bookingms.domain.model.events.CargoRoutedEvent) event).getBookingId();
            } else if (event instanceof com.practicalddd.cargotracker.bookingms.domain.model.events.CargoStatusChangedEvent) {
                return ((com.practicalddd.cargotracker.bookingms.domain.model.events.CargoStatusChangedEvent) event).getBookingId();
            }
            return "unknown";
        } catch (Exception e) {
            logger.warning("Error extracting aggregate ID: " + e.getMessage());
            return "unknown";
        }
    }
    
    private String serializeEventData(DomainEvent event) {
        try {
            // Serialização simples do evento (em produção usar JSON com Jackson ou Gson)
            StringBuilder json = new StringBuilder("{");
            json.append("\"eventType\":\"").append(event.eventType()).append("\"");
            json.append(",\"occurredOn\":\"").append(event.occurredOn()).append("\"");
            
            // Adicionar dados específicos do evento
            if (event instanceof com.practicalddd.cargotracker.bookingms.domain.model.events.CargoBookedEvent) {
                com.practicalddd.cargotracker.bookingms.domain.model.events.CargoBookedEvent bookedEvent = 
                    (com.practicalddd.cargotracker.bookingms.domain.model.events.CargoBookedEvent) event;
                json.append(",\"bookingId\":\"").append(bookedEvent.getBookingId()).append("\"");
                json.append(",\"bookingAmount\":").append(bookedEvent.getBookingAmount());
                json.append(",\"originLocation\":\"").append(bookedEvent.getOriginLocation()).append("\"");
                json.append(",\"destLocation\":\"").append(bookedEvent.getDestLocation()).append("\"");
                json.append(",\"destArrivalDeadline\":\"").append(bookedEvent.getDestArrivalDeadline()).append("\"");
            } else if (event instanceof com.practicalddd.cargotracker.bookingms.domain.model.events.CargoRoutedEvent) {
                com.practicalddd.cargotracker.bookingms.domain.model.events.CargoRoutedEvent routedEvent = 
                    (com.practicalddd.cargotracker.bookingms.domain.model.events.CargoRoutedEvent) event;
                json.append(",\"bookingId\":\"").append(routedEvent.getBookingId()).append("\"");
                json.append(",\"legCount\":").append(routedEvent.getLegCount());
                json.append(",\"routingDate\":\"").append(routedEvent.getRoutingDate()).append("\"");
            } else if (event instanceof com.practicalddd.cargotracker.bookingms.domain.model.events.CargoStatusChangedEvent) {
                com.practicalddd.cargotracker.bookingms.domain.model.events.CargoStatusChangedEvent statusEvent = 
                    (com.practicalddd.cargotracker.bookingms.domain.model.events.CargoStatusChangedEvent) event;
                json.append(",\"bookingId\":\"").append(statusEvent.getBookingId()).append("\"");
                json.append(",\"oldStatus\":\"").append(statusEvent.getOldStatus()).append("\"");
                json.append(",\"newStatus\":\"").append(statusEvent.getNewStatus()).append("\"");
                json.append(",\"reason\":\"").append(statusEvent.getReason()).append("\"");
            }
            
            json.append("}");
            return json.toString();
            
        } catch (Exception e) {
            logger.severe("Error serializing event data: " + e.getMessage());
            return "{\"error\":\"Failed to serialize event\"}";
        }
    }
    
    private DomainEvent toDomainEvent(DomainEventEntity entity) {
        // Desserialização básica - em produção, implementar lógica para criar o DomainEvent correto
        // Por enquanto, retornamos um DomainEvent anônimo com os dados básicos
        return new DomainEvent() {
            @Override
            public LocalDateTime occurredOn() {
                return entity.getOccurredOn();
            }
            
            @Override
            public String eventType() {
                return entity.getEventType();
            }
            
            @Override
            public String toString() {
                return "DomainEvent{" +
                       "eventType='" + entity.getEventType() + '\'' +
                       ", aggregateId='" + entity.getAggregateId() + '\'' +
                       ", occurredOn=" + entity.getOccurredOn() +
                       ", data=" + entity.getEventData() +
                       '}';
            }
        };
    }
}
