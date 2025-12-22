package com.practicalddd.cargotracker.bookingms.infrastructure.events.rabbitmq;

import com.practicalddd.cargotracker.bookingms.application.events.DomainEvent;
import com.practicalddd.cargotracker.bookingms.application.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoStatusChangedEvent;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.events.PortCreatedEvent;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.events.PortCapacityUpdatedEvent;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.events.PortCargoMovementRecordedEvent;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.events.CargoBookedIntegrationEvent;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.events.CargoRoutedEventData;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.events.CargoRoutedIntegrationEvent;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.EventStoreRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class RabbitMQDomainEventPublisher implements DomainEventPublisher {
    
    private static final Logger logger = Logger.getLogger(RabbitMQDomainEventPublisher.class.getName());
    
    @Inject
    private Event<CargoBookedIntegrationEvent> cargoBookedEventControl;
    
    @Inject
    private Event<CargoRoutedIntegrationEvent> cargoRoutedEventControl;
    
    @Inject
    private EventStoreRepository eventStoreRepository;

    @Inject
    private Event<CargoBookedEvent> cargoBookedEventAsync;
    
    @Inject
    private Event<CargoRoutedEvent> cargoRoutedEventAsync;
    
    @Inject
    private Event<CargoStatusChangedEvent> cargoStatusChangedEventAsync;

    @Override
    public void publish(DomainEvent event) {
        try {
            // 1. Primeiro armazena no Event Store (se disponível)
            if (eventStoreRepository != null) {
                eventStoreRepository.store(event);
                logger.fine(() -> String.format(
                    "[EVENT] Event stored: %s - %s", 
                    event.eventType(), getEventIdentifier(event)
                ));
            }
            
            // 2. Depois publica para o RabbitMQ (mantém compatibilidade)
            if (event instanceof CargoBookedEvent) {
                publishCargoBookedEvent((CargoBookedEvent) event);
            } else if (event instanceof CargoRoutedEvent) {
                publishCargoRoutedEvent((CargoRoutedEvent) event);
            } else if (event instanceof CargoStatusChangedEvent) {
                publishCargoStatusChangedEvent((CargoStatusChangedEvent) event);
            } else if (event instanceof PortCreatedEvent) {
                publishPortCreatedEvent((PortCreatedEvent) event);
            } else if (event instanceof PortCapacityUpdatedEvent) {
                publishPortCapacityUpdatedEvent((PortCapacityUpdatedEvent) event);
            } else if (event instanceof PortCargoMovementRecordedEvent) {
                publishPortCargoMovementRecordedEvent((PortCargoMovementRecordedEvent) event);
            } else {
                logger.warning("Unknown event type: " + event.eventType());
            }
            
        } catch (Exception e) {
            logger.severe("Error publishing event to RabbitMQ and EventStore: " + e.getMessage());
            // Não propaga exceção para não afetar operação principal
        }
    }
    
    private void publishCargoBookedEvent(CargoBookedEvent domainEvent) {
        // Publica no RabbitMQ (código existente)
        CargoBookedIntegrationEvent infraEvent = new CargoBookedIntegrationEvent();
        infraEvent.setId(domainEvent.getBookingId());
        cargoBookedEventControl.fire(infraEvent);
        
        // Dispara evento assíncrono para projeções
        cargoBookedEventAsync.fireAsync(domainEvent);
        
        logger.fine(() -> String.format(
            "[EVENT] CargoBookedEvent published: %s (async projection triggered)", 
            domainEvent.getBookingId()
        ));
    }
    
    private void publishCargoRoutedEvent(CargoRoutedEvent domainEvent) {
        // Publica no RabbitMQ (código existente)
        CargoRoutedIntegrationEvent infraEvent = new CargoRoutedIntegrationEvent();
        CargoRoutedEventData eventData = new CargoRoutedEventData();
        eventData.setBookingId(domainEvent.getBookingId());
        infraEvent.setContent(eventData);
        cargoRoutedEventControl.fire(infraEvent);
        
        // Dispara evento assíncrono para projeções
        cargoRoutedEventAsync.fireAsync(domainEvent);
        
        logger.fine(() -> String.format(
            "[EVENT] CargoRoutedEvent published: %s (async projection triggered)", 
            domainEvent.getBookingId()
        ));
    }
    
    private void publishCargoStatusChangedEvent(CargoStatusChangedEvent event) {
        // Log existente
        logger.info(String.format(
            "[EVENT] CargoStatusChangedEvent: %s (%s -> %s)",
            event.getBookingId(), event.getOldStatus(), event.getNewStatus()
        ));
        
        // Dispara evento assíncrono para projeções
        cargoStatusChangedEventAsync.fireAsync(event);
        
        logger.fine(() -> String.format(
            "[EVENT] CargoStatusChangedEvent projection triggered for: %s", 
            event.getBookingId()
        ));
    }
    
    private void publishPortCreatedEvent(PortCreatedEvent event) {
        logger.info(String.format(
            "[EVENT] PortCreatedEvent: %s (%s, %s)",
            event.getPortUnLocCode(), event.getPortName(), event.getCountry()
        ));
        // Em produção, publicar para fila de portos
    }
    
    private void publishPortCapacityUpdatedEvent(PortCapacityUpdatedEvent event) {
        logger.info(String.format(
            "[EVENT] PortCapacityUpdatedEvent: %s (%d -> %d)",
            event.getPortUnLocCode(), event.getOldCapacity(), event.getNewCapacity()
        ));
    }
    
    private void publishPortCargoMovementRecordedEvent(PortCargoMovementRecordedEvent event) {
        logger.info(String.format(
            "[EVENT] PortCargoMovementRecordedEvent: %s %s %d units (usage: %d/%d)",
            event.getPortUnLocCode(), event.getMovementType(), 
            event.getCargoAmount(), event.getCurrentUsage(), event.getMaxCapacity()
        ));
    }
    
    private String getEventIdentifier(DomainEvent event) {
        if (event instanceof CargoBookedEvent) {
            return ((CargoBookedEvent) event).getBookingId();
        } else if (event instanceof CargoRoutedEvent) {
            return ((CargoRoutedEvent) event).getBookingId();
        } else if (event instanceof CargoStatusChangedEvent) {
            return ((CargoStatusChangedEvent) event).getBookingId();
        } else if (event instanceof PortCreatedEvent) {
            return ((PortCreatedEvent) event).getPortUnLocCode();
        } else if (event instanceof PortCapacityUpdatedEvent) {
            return ((PortCapacityUpdatedEvent) event).getPortUnLocCode();
        } else if (event instanceof PortCargoMovementRecordedEvent) {
            return ((PortCargoMovementRecordedEvent) event).getPortUnLocCode();
        }
        return "unknown";
    }
}
