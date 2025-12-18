package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories;

import com.practicalddd.cargotracker.bookingms.application.events.DomainEvent;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para armazenamento de eventos (Event Store)
 */
public interface EventStoreRepository {
    
    /**
     * Armazena um evento de domínio
     */
    void store(DomainEvent event);
    
    /**
     * Busca eventos por ID do agregado
     */
    List<DomainEvent> findByAggregateId(String aggregateId);
    
    /**
     * Busca eventos por tipo
     */
    List<DomainEvent> findByEventType(String eventType);
    
    /**
     * Busca todos os eventos após um timestamp
     */
    List<DomainEvent> findAllAfter(LocalDateTime timestamp);
}
