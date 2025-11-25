package com.practicalddd.cargotracker.bookingms.infrastructure.events.rabbitmq;

import com.practicalddd.cargotracker.bookingms.application.internal.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.events.DomainEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@ApplicationScoped
public class RabbitMQDomainEventPublisher implements DomainEventPublisher {
    
    @Inject
    private Event<com.practicalddd.cargotracker.shareddomain.events.CargoBookedEvent> cargoBookedEventControl;
    
    @Inject
    private Event<com.practicalddd.cargotracker.shareddomain.events.CargoRoutedEvent> cargoRoutedEventControl;

    @Override
    public void publish(DomainEvent event) {
        if (event instanceof CargoBookedEvent) {
            CargoBookedEvent domainEvent = (CargoBookedEvent) event;
            com.practicalddd.cargotracker.shareddomain.events.CargoBookedEvent infraEvent = 
                new com.practicalddd.cargotracker.shareddomain.events.CargoBookedEvent();
            infraEvent.setId(domainEvent.getBookingId());
            cargoBookedEventControl.fire(infraEvent);
        } else if (event instanceof CargoRoutedEvent) {
            CargoRoutedEvent domainEvent = (CargoRoutedEvent) event;
            com.practicalddd.cargotracker.shareddomain.events.CargoRoutedEvent infraEvent = 
                new com.practicalddd.cargotracker.shareddomain.events.CargoRoutedEvent();
            com.practicalddd.cargotracker.shareddomain.events.CargoRoutedEventData eventData = 
                new com.practicalddd.cargotracker.shareddomain.events.CargoRoutedEventData();
            eventData.setBookingId(domainEvent.getBookingId());
            infraEvent.setContent(eventData);
            cargoRoutedEventControl.fire(infraEvent);
        }
    }
}