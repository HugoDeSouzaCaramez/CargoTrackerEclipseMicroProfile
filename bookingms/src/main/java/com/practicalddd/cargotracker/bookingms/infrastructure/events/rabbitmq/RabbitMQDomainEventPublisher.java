package com.practicalddd.cargotracker.bookingms.infrastructure.events.rabbitmq;

import com.practicalddd.cargotracker.bookingms.application.internal.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.events.CargoBookedIntegrationEvent;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.events.CargoRoutedEventData;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.events.CargoRoutedIntegrationEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.events.DomainEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@ApplicationScoped
public class RabbitMQDomainEventPublisher implements DomainEventPublisher {
    
    @Inject
    private Event<CargoBookedIntegrationEvent> cargoBookedEventControl;
    
    @Inject
    private Event<CargoRoutedIntegrationEvent> cargoRoutedEventControl;

    @Override
    public void publish(DomainEvent event) {
        if (event instanceof CargoBookedEvent) {
            CargoBookedEvent domainEvent = (CargoBookedEvent) event;
            CargoBookedIntegrationEvent infraEvent = 
                new CargoBookedIntegrationEvent();
            infraEvent.setId(domainEvent.getBookingId());
            cargoBookedEventControl.fire(infraEvent);
        } else if (event instanceof CargoRoutedEvent) {
            CargoRoutedEvent domainEvent = (CargoRoutedEvent) event;
            CargoRoutedIntegrationEvent infraEvent = 
                new CargoRoutedIntegrationEvent();
            CargoRoutedEventData eventData = 
                new CargoRoutedEventData();
            eventData.setBookingId(domainEvent.getBookingId());
            infraEvent.setContent(eventData);
            cargoRoutedEventControl.fire(infraEvent);
        }
    }
}
