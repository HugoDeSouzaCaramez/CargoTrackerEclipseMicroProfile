package com.practicalddd.cargotracker.bookingms.infrastructure.events.rabbitmq;

import com.practicalddd.cargotracker.bookingms.application.internal.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.events.CargoBookedIntegrationEvent;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.events.CargoRoutedEventData;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.events.CargoRoutedIntegrationEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.events.DomainEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.EventStoreRepository;

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

    @Override
    public void publish(DomainEvent event) {
        try {
            // 1. Primeiro armazena no Event Store
            eventStoreRepository.store(event);
            
            // 2. Depois publica para o RabbitMQ (mantém compatibilidade)
            if (event instanceof CargoBookedEvent) {
                CargoBookedEvent domainEvent = (CargoBookedEvent) event;
                CargoBookedIntegrationEvent infraEvent = 
                    new CargoBookedIntegrationEvent();
                infraEvent.setId(domainEvent.getBookingId());
                cargoBookedEventControl.fire(infraEvent);
                
                logger.fine(() -> String.format(
                    "[EVENT] CargoBookedEvent stored and published: %s", 
                    domainEvent.getBookingId()
                ));
                
            } else if (event instanceof CargoRoutedEvent) {
                CargoRoutedEvent domainEvent = (CargoRoutedEvent) event;
                CargoRoutedIntegrationEvent infraEvent = 
                    new CargoRoutedIntegrationEvent();
                CargoRoutedEventData eventData = 
                    new CargoRoutedEventData();
                eventData.setBookingId(domainEvent.getBookingId());
                infraEvent.setContent(eventData);
                cargoRoutedEventControl.fire(infraEvent);
                
                logger.fine(() -> String.format(
                    "[EVENT] CargoRoutedEvent stored and published: %s", 
                    domainEvent.getBookingId()
                ));
            }
            
        } catch (Exception e) {
            logger.severe("Error publishing event to RabbitMQ and EventStore: " + e.getMessage());
            // Não propaga exceção para não afetar operação principal
        }
    }
}
